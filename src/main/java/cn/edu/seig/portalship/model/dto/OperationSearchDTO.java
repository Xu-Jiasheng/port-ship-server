package cn.edu.seig.portalship.model.dto;

import lombok.Data;

@Data
public class OperationSearchDTO {

    private Integer pageNum;
    private Integer pageSize;
    private Long shipId;
    private String workType;
}
