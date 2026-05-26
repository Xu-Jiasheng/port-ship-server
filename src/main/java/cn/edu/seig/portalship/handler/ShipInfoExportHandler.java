package cn.edu.seig.portalship.handler;

import cn.edu.seig.portalship.model.entity.ShipInfo;
import cn.edu.seig.portalship.model.excel.ShipInfoExcelDTO;
import cn.edu.seig.portalship.model.vo.ImportProgressVO;
import cn.edu.seig.portalship.service.IShipInfoService;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 船舶数据高性能导出处理器
 * <p>
 * 核心策略：
 * 1. 先 COUNT 总行数，计算总页数
 * 2. 使用 CompletableFuture 预取下一页（滑动窗口 = 1 页），始终保持最多 2 页数据在内存
 * 3. 主线程拿到当前页后直接写入 EasyExcel（EasyExcel 内部流式写盘，不占内存）
 * 4. 避免一次性加载全表，百万级数据只占用常量级内存
 */
@Component
public class ShipInfoExportHandler {

    private static final Logger log = LoggerFactory.getLogger(ShipInfoExportHandler.class);
    private static final int PAGE_SIZE = 10000;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 异步导出到临时文件，完成后返回文件路径
     * <p>
     * 适用于"异步生成 → 轮询进度 → 下载"的模式，避免长时间占用 HTTP 连接。
     */
    public void exportToFile(QueryWrapper<ShipInfo> queryWrapper,
                             IShipInfoService shipInfoService,
                             ThreadPoolExecutor executor,
                             String taskId,
                             ConcurrentHashMap<String, ImportProgressVO> progressMap) {

        long startTime = System.currentTimeMillis();

        // 创建临时文件
        Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "excel-export");
        File tempFile;
        try {
            Files.createDirectories(tempDir);
            tempFile = tempDir.resolve(taskId + ".xlsx").toFile();
        } catch (Exception e) {
            log.error("Export task [{}]: failed to create temp file", taskId, e);
            progressMap.put(taskId, ImportProgressVO.failed(taskId, "export", "无法创建临时文件"));
            return;
        }

        long total = shipInfoService.count(queryWrapper);
        long totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        log.info("Export task [{}]: total={}, pages={}", taskId, total, totalPages);

        if (total == 0) {
            EasyExcel.write(tempFile, ShipInfoExcelDTO.class)
                    .sheet("船舶数据").doWrite(new ArrayList<>());
            markComplete(taskId, progressMap, 0, startTime, tempFile.getAbsolutePath());
            return;
        }

        // 预取第一页
        CompletableFuture<List<ShipInfoExcelDTO>> nextPageFuture = CompletableFuture.supplyAsync(
                () -> queryPage(queryWrapper, 1, PAGE_SIZE, shipInfoService), executor);

        try (ExcelWriter writer = EasyExcel.write(tempFile, ShipInfoExcelDTO.class).build()) {
            WriteSheet sheet = EasyExcel.writerSheet("船舶数据").build();

            for (int page = 1; page <= totalPages; page++) {
                List<ShipInfoExcelDTO> rows = nextPageFuture.join();

                if (page < totalPages) {
                    final int nextPage = page + 1;
                    nextPageFuture = CompletableFuture.supplyAsync(
                            () -> queryPage(queryWrapper, nextPage, PAGE_SIZE, shipInfoService), executor);
                }

                writer.write(rows, sheet);
                updateProgress(taskId, progressMap, (int) total, page, PAGE_SIZE, startTime);

                if (page % 10 == 0) {
                    log.info("Export task [{}]: {} / {} pages", taskId, page, totalPages);
                }
            }
        }

        markComplete(taskId, progressMap, (int) total, startTime, tempFile.getAbsolutePath());
        log.info("Export task [{}] finished: {} rows, {}s, file={}",
                taskId, total, (System.currentTimeMillis() - startTime) / 1000, tempFile.getName());
    }

    /**
     * 多线程分页导出到输出流
     *
     * @param outputStream 输出流（通常是 HttpServletResponse.getOutputStream()）
     * @param queryWrapper 查询条件（可空，表示导出全表）
     * @param shipInfoService 船舶服务
     * @param executor 导出专用线程池
     * @param taskId 任务 ID
     * @param progressMap 进度 Map
     */
    public void export(OutputStream outputStream,
                       QueryWrapper<ShipInfo> queryWrapper,
                       IShipInfoService shipInfoService,
                       ThreadPoolExecutor executor,
                       String taskId,
                       ConcurrentHashMap<String, ImportProgressVO> progressMap) {

        long startTime = System.currentTimeMillis();

        // 1. 计算总行数
        long total = shipInfoService.count(queryWrapper);
        long totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        log.info("Export task [{}]: total={}, pages={}, pageSize={}", taskId, total, totalPages, PAGE_SIZE);

        if (total == 0) {
            // 空表也导出带表头的空 Excel
            EasyExcel.write(outputStream, ShipInfoExcelDTO.class)
                    .sheet("船舶数据").doWrite(new ArrayList<>());
            markSuccess(taskId, progressMap, 0, startTime);
            return;
        }

        // 2. 预取第一页
        CompletableFuture<List<ShipInfoExcelDTO>> nextPageFuture = CompletableFuture.supplyAsync(
                () -> queryPage(queryWrapper, 1, PAGE_SIZE, shipInfoService), executor);

        int processedPages = 0;

        try (ExcelWriter writer = EasyExcel.write(outputStream, ShipInfoExcelDTO.class).build()) {
            WriteSheet sheet = EasyExcel.writerSheet("船舶数据").build();

            for (int page = 1; page <= totalPages; page++) {
                // 获取当前页数据
                List<ShipInfoExcelDTO> rows = nextPageFuture.join();
                processedPages++;

                // 预取下一页（滑动窗口关键：不等待当前页写完就开始查下一页）
                if (page < totalPages) {
                    final int nextPage = page + 1;
                    nextPageFuture = CompletableFuture.supplyAsync(
                            () -> queryPage(queryWrapper, nextPage, PAGE_SIZE, shipInfoService), executor);
                }

                // 流式写入当前页（EasyExcel 内部会分批刷盘）
                writer.write(rows, sheet);

                // 更新进度
                ImportProgressVO progress = ImportProgressVO.running(taskId, "export");
                progress.setTotalRows((int) total);
                progress.setProcessedRows((int) Math.min((long) page * PAGE_SIZE, total));
                progress.setElapsedSeconds((System.currentTimeMillis() - startTime) / 1000);
                progressMap.put(taskId, progress);

                if (page % 10 == 0) {
                    log.info("Export task [{}]: {} / {} pages written", taskId, page, totalPages);
                }
            }
        }

        // 3. 标记完成
        markSuccess(taskId, progressMap, (int) total, startTime);
        log.info("Export task [{}] finished: {} rows in {} pages, {}s",
                taskId, total, totalPages, (System.currentTimeMillis() - startTime) / 1000);
    }

    // ==================== 内部方法 ====================

    /**
     * 分页查询一页数据并转换为 Excel DTO 列表
     */
    private List<ShipInfoExcelDTO> queryPage(QueryWrapper<ShipInfo> queryWrapper,
                                             int pageNum, int pageSize,
                                             IShipInfoService shipInfoService) {
        Page<ShipInfo> page = new Page<>(pageNum, pageSize);
        // clone 查询条件，避免线程间干扰
        QueryWrapper<ShipInfo> wrapper = queryWrapper != null ? queryWrapper.clone() : new QueryWrapper<>();
        wrapper.orderByAsc("id");

        Page<ShipInfo> result = shipInfoService.page(page, wrapper);
        return result.getRecords().stream().map(this::convert).toList();
    }

    /**
     * 实体 → Excel DTO
     */
    private ShipInfoExcelDTO convert(ShipInfo entity) {
        ShipInfoExcelDTO dto = new ShipInfoExcelDTO();
        dto.setShipName(entity.getShipName());
        dto.setNationality(entity.getNationality());
        dto.setImoNo(entity.getImoNo());
        dto.setMmsiNo(entity.getMmsiNo());
        dto.setShipType(entity.getShipType());
        dto.setLength(entity.getLength());
        dto.setWidth(entity.getWidth());
        dto.setDraft(entity.getDraft());
        dto.setDeadweight(entity.getDeadweight());
        dto.setCompany(entity.getCompany());
        dto.setVoyageNo(entity.getVoyageNo());
        dto.setCargoType(entity.getCargoType());
        dto.setCargoAmount(entity.getCargoAmount());
        dto.setBerthNo(entity.getBerthNo());
        dto.setArriveTime(entity.getArriveTime() != null ? entity.getArriveTime().format(DATE_FMT) : null);
        dto.setLeaveTime(entity.getLeaveTime() != null ? entity.getLeaveTime().format(DATE_FMT) : null);
        dto.setStatus(entity.getStatus());
        return dto;
    }

    private void markSuccess(String taskId, ConcurrentHashMap<String, ImportProgressVO> map,
                             int total, long startTime) {
        ImportProgressVO progress = ImportProgressVO.success(taskId, "export");
        progress.setTotalRows(total);
        progress.setProcessedRows(total);
        progress.setSuccessRows(total);
        progress.setElapsedSeconds((System.currentTimeMillis() - startTime) / 1000);
        map.put(taskId, progress);
    }

    private void markComplete(String taskId, ConcurrentHashMap<String, ImportProgressVO> map,
                              int total, long startTime, String filePath) {
        ImportProgressVO progress = ImportProgressVO.success(taskId, "export");
        progress.setTotalRows(total);
        progress.setProcessedRows(total);
        progress.setSuccessRows(total);
        progress.setElapsedSeconds((System.currentTimeMillis() - startTime) / 1000);
        progress.setDownloadUrl(filePath);
        map.put(taskId, progress);
    }

    private void updateProgress(String taskId, ConcurrentHashMap<String, ImportProgressVO> map,
                                int total, int page, int pageSize, long startTime) {
        ImportProgressVO progress = ImportProgressVO.running(taskId, "export");
        progress.setTotalRows(total);
        progress.setProcessedRows((int) Math.min((long) page * pageSize, total));
        progress.setElapsedSeconds((System.currentTimeMillis() - startTime) / 1000);
        map.put(taskId, progress);
    }
}
