package cn.edu.seig.portalship.service;

import cn.edu.seig.portalship.model.dto.StatisticsQueryDTO;
import cn.edu.seig.portalship.model.vo.DashboardVO;
import cn.edu.seig.portalship.model.vo.OperationRankVO;
import cn.edu.seig.portalship.result.Result;

import java.util.List;

public interface IStatisticsService {

    Result<DashboardVO> getDashboard();

    Result<List<OperationRankVO>> getEfficiencyRank(Integer limit);

    Result<List<?>> exportShipData(StatisticsQueryDTO queryDTO);
}
