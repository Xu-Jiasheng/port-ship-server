package cn.edu.seig.portalship.model.vo;

import lombok.Data;

@Data
public class ShipStatisticsVO {

    private String period;
    private Long arriveCount;
    private Long departCount;
    private Long totalContainers;
    private Double avgEfficiency;
}
