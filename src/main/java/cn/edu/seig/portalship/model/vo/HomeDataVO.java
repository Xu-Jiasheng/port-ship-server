package cn.edu.seig.portalship.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HomeDataVO {

    private PortDataVO portData;
    private WeatherVO weather;
    private WorkDataVO workData;

    @Data
    public static class PortDataVO {
        private Long id;
        private String portName;
        private Integer totalBerths;
        private Integer availableBerths;
        private BigDecimal todayThroughput;
        private BigDecimal monthlyThroughput;
        private String updateTime;
    }

    @Data
    public static class WeatherVO {
        private Long id;
        private BigDecimal temperature;
        private String windDirection;
        private BigDecimal windSpeed;
        private BigDecimal waveHeight;
        private BigDecimal visibility;
        private String weatherDesc;
        private String updateTime;
    }

    @Data
    public static class WorkDataVO {
        private Long id;
        private String workDate;
        private Integer totalShips;
        private Integer completedShips;
        private Integer totalContainers;
        private BigDecimal efficiencyRate;
        private String updateTime;
    }
}
