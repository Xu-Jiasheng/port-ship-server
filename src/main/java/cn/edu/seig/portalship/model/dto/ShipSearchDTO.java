package cn.edu.seig.portalship.model.dto;

import lombok.Data;

@Data
public class ShipSearchDTO {

    private Integer pageNum;
    private Integer pageSize;
    private String shipName;
    private String voyageNo;
    private String status;
    private String shipType;
}
