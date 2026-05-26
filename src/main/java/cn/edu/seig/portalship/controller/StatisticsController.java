package cn.edu.seig.portalship.controller;

import cn.edu.seig.portalship.model.dto.StatisticsQueryDTO;
import cn.edu.seig.portalship.model.vo.DashboardVO;
import cn.edu.seig.portalship.model.vo.OperationRankVO;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private IStatisticsService statisticsService;

    @GetMapping("/getPublicStatistics")
    public Result<DashboardVO> getPublicStatistics() {
        return statisticsService.getDashboard();
    }

    @GetMapping("/getDashboard")
    public Result<DashboardVO> getDashboard() {
        return statisticsService.getDashboard();
    }

    @GetMapping("/getEfficiencyRank")
    public Result<List<OperationRankVO>> getEfficiencyRank(@RequestParam(required = false, defaultValue = "10") Integer limit) {
        return statisticsService.getEfficiencyRank(limit);
    }

    @PostMapping("/exportShipData")
    public Result<List<?>> exportShipData(@RequestBody StatisticsQueryDTO queryDTO) {
        return statisticsService.exportShipData(queryDTO);
    }
}
