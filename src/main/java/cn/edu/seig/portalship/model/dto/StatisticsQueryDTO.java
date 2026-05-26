package cn.edu.seig.portalship.model.dto;

import lombok.Data;

@Data
public class StatisticsQueryDTO {

    private String startDate;
    private String endDate;
    private String type;
}
