package cn.edu.seig.portalship.service.impl;

import cn.edu.seig.portalship.handler.ShipInfoExportHandler;
import cn.edu.seig.portalship.listener.ShipInfoImportListener;
import cn.edu.seig.portalship.model.vo.ImportProgressVO;
import cn.edu.seig.portalship.service.IExcelService;
import cn.edu.seig.portalship.service.IShipInfoService;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Excel 导入/导出服务实现
 * <p>
 * 导入流程：
 * 1. 上传文件 → 保存到临时目录 → 返回 taskId
 * 2. 后台异步：EasyExcel.read() + ShipInfoImportListener 分批写入
 * 3. 前端轮询 /excel/progress/{taskId} 获取进度
 * <p>
 * 导出流程：
 * 1. 请求导出 → 返回 taskId
 * 2. 后台异步：ShipInfoExportHandler 分页查询 + 流式写入 response
 * 3. 前端轮询进度，完成后自动触发浏览器下载
 */
@Service
public class ExcelServiceImpl implements IExcelService {

    private static final Logger log = LoggerFactory.getLogger(ExcelServiceImpl.class);

    @Autowired
    private IShipInfoService shipInfoService;

    @Autowired
    @Qualifier("excelImportExecutor")
    private ThreadPoolExecutor importExecutor;

    @Autowired
    @Qualifier("excelExportExecutor")
    private ThreadPoolExecutor exportExecutor;

    @Autowired
    private ShipInfoExportHandler exportHandler;

    /** 全局任务进度表：taskId → 进度 VO，支持前端轮询 */
    private final ConcurrentHashMap<String, ImportProgressVO> progressMap = new ConcurrentHashMap<>();

    // ==================== 导入 ====================

    @Override
    public String importShipData(MultipartFile file) {
        String taskId = UUID.randomUUID().toString().substring(0, 8);
        ImportProgressVO progress = ImportProgressVO.running(taskId, "import");
        progressMap.put(taskId, progress);

        // 保存临时文件（EasyExcel 需要 File 或 InputStream）
        Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "excel-import");
        try {
            Files.createDirectories(tempDir);
        } catch (IOException e) {
            progressMap.put(taskId, ImportProgressVO.failed(taskId, "import", "无法创建临时目录"));
            return taskId;
        }

        File tempFile = tempDir.resolve(taskId + "-" + file.getOriginalFilename()).toFile();

        CompletableFuture.runAsync(() -> {
            try {
                file.transferTo(tempFile);

                EasyExcel.read(tempFile, new ShipInfoImportListener(
                        shipInfoService, importExecutor, taskId, progressMap
                )).sheet().doRead();

            } catch (Exception e) {
                log.error("Import task [{}] failed", taskId, e);
                ImportProgressVO fail = ImportProgressVO.failed(taskId, "import", e.getMessage());
                fail.setProcessedRows(progressMap.get(taskId) != null
                        ? progressMap.get(taskId).getProcessedRows() : 0);
                progressMap.put(taskId, fail);
            } finally {
                tempFile.delete();
            }
        }, importExecutor);

        log.info("Import task [{}] started: {}", taskId, file.getOriginalFilename());
        return taskId;
    }

    // ==================== 导出 ====================

    @Override
    public String exportShipData() {
        String taskId = UUID.randomUUID().toString().substring(0, 8);
        ImportProgressVO progress = ImportProgressVO.running(taskId, "export");
        progressMap.put(taskId, progress);

        CompletableFuture.runAsync(() -> {
            try {
                exportHandler.exportToFile(new QueryWrapper<>(),
                        shipInfoService, exportExecutor, taskId, progressMap);
            } catch (Exception e) {
                log.error("Export task [{}] failed", taskId, e);
                progressMap.put(taskId, ImportProgressVO.failed(taskId, "export", e.getMessage()));
            }
        }, exportExecutor);

        log.info("Export task [{}] started", taskId);
        return taskId;
    }

    @Override
    public void downloadFile(String taskId, HttpServletResponse response) {
        ImportProgressVO progress = progressMap.get(taskId);
        if (progress == null || !"SUCCESS".equals(progress.getStatus())) {
            throw new RuntimeException("导出任务未完成或不存在");
        }

        File file = new File(progress.getDownloadUrl());
        if (!file.exists()) {
            throw new RuntimeException("导出文件已被清理，请重新导出");
        }

        try {
            String fileName = URLEncoder.encode("船舶数据导出.xlsx", StandardCharsets.UTF_8)
                    .replace("+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

            try (OutputStream os = response.getOutputStream()) {
                Files.copy(file.toPath(), os);
                os.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    // ==================== 进度查询 ====================

    @Override
    public ImportProgressVO getProgress(String taskId) {
        return progressMap.get(taskId);
    }

    // ==================== 定时清理过期任务 ====================

    /**
     * 每 10 分钟清理一次已完成/失败超过 30 分钟的任务记录，防止内存泄漏
     */
    @Scheduled(fixedRate = 600000)
    public void cleanExpiredTasks() {
        progressMap.entrySet().removeIf(entry -> {
            ImportProgressVO vo = entry.getValue();
            if (vo == null) return true;
            if ("SUCCESS".equals(vo.getStatus()) || "FAILED".equals(vo.getStatus())) {
                // 删除关联的临时文件
                if (vo.getDownloadUrl() != null) {
                    try {
                        File tempFile = new File(vo.getDownloadUrl());
                        if (tempFile.exists()) {
                            tempFile.delete();
                        }
                    } catch (Exception ignored) {}
                }
                return true; // 完成/失败的任务直接清理
            }
            // RUNNING 任务超过 1 小时标记为失败
            if ("RUNNING".equals(vo.getStatus())) {
                long age = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(
                        vo.getElapsedSeconds() != null ? vo.getElapsedSeconds() : 0);
                if (age > TimeUnit.HOURS.toMillis(1)) {
                    vo.setStatus("FAILED");
                    vo.setErrorMessage("任务超时");
                    return false; // 保留记录供查询
                }
            }
            return false;
        });
    }
}
