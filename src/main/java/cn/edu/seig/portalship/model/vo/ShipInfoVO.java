package cn.edu.seig.portalship.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShipInfoVO {

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arriveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leaveTime;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
