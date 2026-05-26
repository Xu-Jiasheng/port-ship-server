package cn.edu.seig.portalship.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShipOperationAddDTO {

    @NotNull
    private Long shipId;

    private String quayCraneNo;
    private String workType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalContainers;
    private Integer normalBoxes;
    private Integer reeferBoxes;
    private Integer dangerBoxes;
    private BigDecimal workEfficiency;
    private Integer truckCount;
}
