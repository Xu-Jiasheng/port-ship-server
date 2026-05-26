package cn.edu.seig.portalship.model.vo;

import lombok.Data;

@Data
public class FileInfoVO {

    private Long id;
    private Long relateId;
    private String relateType;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String uploadTime;
}
