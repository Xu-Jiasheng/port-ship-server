package cn.edu.seig.portalship.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShipInfoDTO {

    private Long id;
    private String shipName;
    private String nationality;
    private String imoNo;
    private String mmsiNo;
    private String shipType;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal draft;
    private BigDecimal deadweight;
    private String company;
    private String voyageNo;
    private String cargoType;
    private BigDecimal cargoAmount;
    private String berthNo;
    private LocalDateTime arriveTime;
    private LocalDateTime leaveTime;
    private String status;
}
