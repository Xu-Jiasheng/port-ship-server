package cn.edu.seig.portalship.model.vo;

import lombok.Data;

/**
 * 导入/导出任务进度 VO
 */
@Data
public class ImportProgressVO {

    /** 任务唯一标识 */
    private String taskId;

    /** 任务类型：import / export */
    private String type;

    /** 状态：RUNNING / SUCCESS / FAILED */
    private String status;

    /** 总行数 */
    private Integer totalRows;

    /** 已处理行数 */
    private Integer processedRows;

    /** 成功行数 */
    private Integer successRows;

    /** 失败行数 */
    private Integer failedRows;

    /** 耗时（秒） */
    private Long elapsedSeconds;

    /** 下载链接（导出完成时） */
    private String downloadUrl;

    /** 错误信息 */
    private String errorMessage;

    public static ImportProgressVO running(String taskId, String type) {
        ImportProgressVO vo = new ImportProgressVO();
        vo.setTaskId(taskId);
        vo.setType(type);
        vo.setStatus("RUNNING");
        return vo;
    }

    public static ImportProgressVO success(String taskId, String type) {
        ImportProgressVO vo = new ImportProgressVO();
        vo.setTaskId(taskId);
        vo.setType(type);
        vo.setStatus("SUCCESS");
        return vo;
    }

    public static ImportProgressVO failed(String taskId, String type, String errorMessage) {
        ImportProgressVO vo = new ImportProgressVO();
        vo.setTaskId(taskId);
        vo.setType(type);
        vo.setStatus("FAILED");
        vo.setErrorMessage(errorMessage);
        return vo;
    }
}
