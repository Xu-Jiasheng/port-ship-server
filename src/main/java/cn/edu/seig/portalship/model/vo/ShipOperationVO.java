package cn.edu.seig.portalship.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShipOperationVO {

    private Long id;
    private Long shipId;
    private String shipName;
    private String quayCraneNo;
    private String workType;
    private String startTime;
    private String endTime;
    private Integer totalContainers;
    private Integer normalBoxes;
    private Integer reeferBoxes;
    private Integer dangerBoxes;
    private BigDecimal workEfficiency;
    private Integer truckCount;
    private String createTime;
}
