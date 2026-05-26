package cn.edu.seig.portalship.listener;

import cn.edu.seig.portalship.model.entity.ShipInfo;
import cn.edu.seig.portalship.model.excel.ShipInfoExcelDTO;
import cn.edu.seig.portalship.model.vo.ImportProgressVO;
import cn.edu.seig.portalship.service.IShipInfoService;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 船舶数据 Excel 导入监听器
 * <p>
 * 核心策略：
 * 1. EasyExcel 逐行回调 {@link #invoke}，将 DTO 转为实体后放入缓冲区
 * 2. 缓冲区达到 5000 行时，拷贝一份提交到线程池异步批量写入数据库
 * 3. 文件解析完成后，flush 剩余行并等待所有写入任务结束
 * 4. 使用 AtomicInteger 追踪处理进度，外部通过 progressMap 轮询
 * <p>
 * 线程安全：缓冲区通过 synchronized 保护，计数器使用 AtomicInteger
 */
public class ShipInfoImportListener implements ReadListener<ShipInfoExcelDTO> {

    private static final Logger log = LoggerFactory.getLogger(ShipInfoImportListener.class);
    private static final int BATCH_SIZE = 5000;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IShipInfoService shipInfoService;
    private final ThreadPoolExecutor executor;
    private final String taskId;
    private final ConcurrentHashMap<String, ImportProgressVO> progressMap;
    private final long startTime;

    private final List<ShipInfo> buffer = new ArrayList<>(BATCH_SIZE);
    private final List<CompletableFuture<Void>> futures = new ArrayList<>();

    private final AtomicInteger totalRows = new AtomicInteger(0);
    private final AtomicInteger successRows = new AtomicInteger(0);
    private final AtomicInteger failedRows = new AtomicInteger(0);

    public ShipInfoImportListener(IShipInfoService shipInfoService,
                                  ThreadPoolExecutor executor,
                                  String taskId,
                                  ConcurrentHashMap<String, ImportProgressVO> progressMap) {
        this.shipInfoService = shipInfoService;
        this.executor = executor;
        this.taskId = taskId;
        this.progressMap = progressMap;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void invoke(ShipInfoExcelDTO dto, AnalysisContext context) {
        totalRows.incrementAndGet();
        ShipInfo entity = convert(dto);

        synchronized (buffer) {
            buffer.add(entity);
            if (buffer.size() >= BATCH_SIZE) {
                // 拷贝当前批次，清空缓冲区，释放锁后再提交
                List<ShipInfo> batch = new ArrayList<>(buffer);
                buffer.clear();
                submitBatch(batch);
            }
        }

        // 每 50000 行输出一次日志
        int current = totalRows.get();
        if (current % 50000 == 0) {
            log.info("Import task [{}]: read {} rows, submitted {} batches",
                    taskId, current, futures.size());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 1. flush 缓冲区剩余数据
        synchronized (buffer) {
            if (!buffer.isEmpty()) {
                List<ShipInfo> batch = new ArrayList<>(buffer);
                buffer.clear();
                submitBatch(batch);
            }
        }

        // 2. 等待所有写入任务完成
        log.info("Import task [{}]: all rows read ({}), waiting for {} batch tasks to finish...",
                taskId, totalRows.get(), futures.size());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 3. 更新最终进度
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        ImportProgressVO progress = ImportProgressVO.success(taskId, "import");
        progress.setTotalRows(totalRows.get());
        progress.setProcessedRows(totalRows.get());
        progress.setSuccessRows(successRows.get());
        progress.setFailedRows(failedRows.get());
        progress.setElapsedSeconds(elapsed);
        progressMap.put(taskId, progress);

        log.info("Import task [{}] finished: total={}, success={}, failed={}, elapsed={}s",
                taskId, totalRows.get(), successRows.get(), failedRows.get(), elapsed);
    }

    @Override
    public void onException(Exception e, AnalysisContext context) throws Exception {
        log.error("Import task [{}]: row parse error at line {}", taskId,
                context.readRowHolder().getRowIndex(), e);
        failedRows.incrementAndGet();

        // 单行解析失败不中断整个导入，继续下一行
        if (failedRows.get() > 10000) {
            throw new RuntimeException("导入失败行数超过 10000，终止导入", e);
        }
    }

    // ==================== 内部方法 ====================

    /**
     * 将 EasyExcel DTO 转换为数据库实体
     */
    private ShipInfo convert(ShipInfoExcelDTO dto) {
        ShipInfo entity = new ShipInfo();
        entity.setShipName(dto.getShipName());
        entity.setNationality(dto.getNationality());
        entity.setImoNo(dto.getImoNo());
        entity.setMmsiNo(dto.getMmsiNo());
        entity.setShipType(dto.getShipType());
        entity.setLength(dto.getLength());
        entity.setWidth(dto.getWidth());
        entity.setDraft(dto.getDraft());
        entity.setDeadweight(dto.getDeadweight());
        entity.setCompany(dto.getCompany());
        entity.setVoyageNo(dto.getVoyageNo());
        entity.setCargoType(dto.getCargoType());
        entity.setCargoAmount(dto.getCargoAmount());
        entity.setBerthNo(dto.getBerthNo());
        entity.setArriveTime(parseDateTime(dto.getArriveTime()));
        entity.setLeaveTime(parseDateTime(dto.getLeaveTime()));
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "待靠泊");
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return entity;
    }

    private LocalDateTime parseDateTime(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            return LocalDateTime.parse(str, DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 提交一批数据到线程池异步写入
     */
    private void submitBatch(List<ShipInfo> batch) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                // MyBatis-Plus saveBatch 内部分批提交，每 1000 条一条 SQL
                shipInfoService.saveBatch(batch, 1000);
                successRows.addAndGet(batch.size());
            } catch (Exception e) {
                log.error("Import task [{}]: batch insert failed (size={})", taskId, batch.size(), e);
                failedRows.addAndGet(batch.size());
            }
        }, executor);
        futures.add(future);
    }
}
