package cn.edu.seig.portalship.model.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {

    private Long totalShips;
    private Long inPortShips;
    private Long workingShips;
    private Long departedShips;
    private Long todayOperations;
    private Double todayEfficiency;
    private List<Map<String, Object>> monthlyBoxTrend;
    private List<Map<String, Object>> shipTypeDistribution;
}
