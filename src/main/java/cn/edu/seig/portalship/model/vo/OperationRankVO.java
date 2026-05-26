package cn.edu.seig.portalship.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OperationRankVO {

    private String shipName;
    private String quayCraneNo;
    private BigDecimal workEfficiency;
    private Integer totalContainers;
}
