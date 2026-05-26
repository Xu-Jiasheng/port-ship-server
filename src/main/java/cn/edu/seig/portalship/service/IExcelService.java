package cn.edu.seig.portalship.service;

import cn.edu.seig.portalship.model.vo.ImportProgressVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excel 导入/导出服务
 */
public interface IExcelService {

    /**
     * 异步导入船舶 Excel 文件
     *
     * @param file 上传的 Excel 文件
     * @return 任务 ID，后续可通过 getProgress 查询进度
     */
    String importShipData(MultipartFile file);

    /**
     * 异步导出全量船舶数据为 Excel 文件
     *
     * @return 任务 ID，后续可通过 getProgress 轮询进度
     */
    String exportShipData();

    /**
     * 下载已完成的导出文件
     *
     * @param taskId   任务 ID
     * @param response HttpServletResponse
     */
    void downloadFile(String taskId, HttpServletResponse response);

    /**
     * 查询导入/导出任务进度
     *
     * @param taskId 任务 ID
     * @return 进度 VO，如果任务不存在返回 null
     */
    ImportProgressVO getProgress(String taskId);
}
