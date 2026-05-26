package cn.edu.seig.portalship.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShipAddReqDTO {

    @NotBlank(message = "船舶名称不能为空")
    private String shipName;

    private String cargoType;

    private Integer cargoNum;

    private String destination;
}
