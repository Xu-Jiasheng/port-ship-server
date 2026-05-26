package cn.edu.seig.portalship.controller;

import cn.edu.seig.portalship.model.vo.ImportProgressVO;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.IExcelService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Excel 导入/导出控制器
 * <p>
 * 导入流程：POST /excel/import → 返回 taskId → 轮询 GET /excel/progress/{taskId}
 * <p>
 * 导出流程：POST /excel/export → 返回 taskId → 轮询 GET /excel/progress/{taskId}
 *          → status=SUCCESS 后调用 GET /excel/download/{taskId} 下载文件
 */
@RestController
@RequestMapping("/excel")
public class ExcelController {

    @Autowired
    private IExcelService excelService;

    /**
     * 上传 Excel 文件，异步导入船舶数据
     *
     * @param file Excel 文件（.xlsx / .xls）
     * @return { "taskId": "a1b2c3d4" }
     */
    @PostMapping("/import")
    public Result<Map<String, String>> importExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return Result.error("仅支持 .xlsx 或 .xls 格式的 Excel 文件");
        }

        String taskId = excelService.importShipData(file);
        return Result.success("导入任务已提交", Map.of("taskId", taskId));
    }

    /**
     * 提交导出任务（异步）
     * <p>
     * 返回 taskId 后，前端轮询 GET /excel/progress/{taskId}，
     * 当 status=SUCCESS 时调用 GET /excel/download/{taskId} 下载文件。
     *
     * @return { "taskId": "e5f6g7h8" }
     */
    @PostMapping("/export")
    public Result<Map<String, String>> exportExcel() {
        String taskId = excelService.exportShipData();
        return Result.success("导出任务已提交", Map.of("taskId", taskId));
    }

    /**
     * 下载已完成的导出文件
     *
     * @param taskId   任务 ID
     * @param response HttpServletResponse
     */
    @GetMapping("/download/{taskId}")
    public void downloadFile(@PathVariable String taskId, HttpServletResponse response) {
        try {
            excelService.downloadFile(taskId, response);
        } catch (RuntimeException e) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(400);
            try {
                response.getWriter().write("{\"code\":1,\"message\":\"" + e.getMessage() + "\"}");
            } catch (Exception ignored) {}
        }
    }

    /**
     * 查询导入/导出任务进度
     *
     * @param taskId 任务 ID
     * @return 进度 VO：
     *         - status=RUNNING：处理中
     *         - status=SUCCESS：完成，导出任务会附带 downloadUrl
     *         - status=FAILED：失败，查看 errorMessage
     */
    @GetMapping("/progress/{taskId}")
    public Result<ImportProgressVO> getProgress(@PathVariable String taskId) {
        ImportProgressVO progress = excelService.getProgress(taskId);
        if (progress == null) {
            return Result.error("任务不存在或已过期");
        }
        return Result.success(progress);
    }
}
