package cn.edu.seig.portalship.service.impl;

import cn.edu.seig.portalship.mapper.ShipInfoMapper;
import cn.edu.seig.portalship.mapper.ShipOperationMapper;
import cn.edu.seig.portalship.model.dto.StatisticsQueryDTO;
import cn.edu.seig.portalship.model.entity.ShipInfo;
import cn.edu.seig.portalship.model.entity.ShipOperation;
import cn.edu.seig.portalship.model.vo.DashboardVO;
import cn.edu.seig.portalship.model.vo.OperationRankVO;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.IStatisticsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements IStatisticsService {

    @Autowired
    private ShipInfoMapper shipInfoMapper;
    @Autowired
    private ShipOperationMapper operationMapper;

    @Override
    public Result<DashboardVO> getDashboard() {
        DashboardVO vo = new DashboardVO();

        List<ShipInfo> allShips = shipInfoMapper.selectList(null);
        vo.setTotalShips((long) allShips.size());
        vo.setInPortShips(allShips.stream().filter(s -> "在港".equals(s.getStatus())).count());
        vo.setWorkingShips(allShips.stream().filter(s -> "作业中".equals(s.getStatus())).count());
        vo.setDepartedShips(allShips.stream().filter(s -> "离港".equals(s.getStatus())).count());

        List<ShipOperation> operations = operationMapper.selectList(null);
        vo.setTodayOperations((long) operations.size());

        if (!operations.isEmpty()) {
            double avgEff = operations.stream()
                    .map(ShipOperation::getWorkEfficiency)
                    .filter(Objects::nonNull)
                    .mapToDouble(BigDecimal::doubleValue)
                    .average()
                    .orElse(0);
            vo.setTodayEfficiency(BigDecimal.valueOf(avgEff).setScale(2, RoundingMode.HALF_UP).doubleValue());
        } else {
            vo.setTodayEfficiency(0.0);
        }

        // Ship type distribution
        Map<String, Long> typeDistribution = allShips.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getShipType() != null ? s.getShipType() : "未知",
                        Collectors.counting()));
        vo.setShipTypeDistribution(typeDistribution.entrySet().stream()
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", e.getKey());
                    item.put("value", e.getValue());
                    return item;
                }).collect(Collectors.toList()));

        // Monthly box trend (simple: by ship status counts)
        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        Map<String, Long> monthlyArrivals = new HashMap<>();
        for (ShipInfo s : allShips) {
            if (s.getArriveTime() != null) {
                String month = s.getArriveTime().toLocalDate().toString().substring(0, 7);
                monthlyArrivals.merge(month, 1L, Long::sum);
            }
        }
        monthlyArrivals.forEach((k, v) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("month", k);
            item.put("arrivals", v);
            monthlyTrend.add(item);
        });
        monthlyTrend.sort(Comparator.comparing(m -> (String) m.get("month")));
        vo.setMonthlyBoxTrend(monthlyTrend);

        return Result.success(vo);
    }

    @Override
    public Result<List<OperationRankVO>> getEfficiencyRank(Integer limit) {
        int pageSize = limit != null ? limit : 10;
        // 使用 MyBatis-Plus 分页替代原始 SQL LIMIT，避免 SQL 注入
        Page<ShipOperation> page = new Page<>(1, pageSize);
        com.baomidou.mybatisplus.core.metadata.IPage<ShipOperation> opPage = operationMapper.selectPage(
                page, new QueryWrapper<ShipOperation>().orderByDesc("work_efficiency"));

        // 批量查询船名，避免 N+1 查询
        List<Long> shipIds = opPage.getRecords().stream()
                .map(ShipOperation::getShipId).distinct().toList();
        Map<Long, String> shipNameMap = new HashMap<>();
        if (!shipIds.isEmpty()) {
            shipInfoMapper.selectBatchIds(shipIds)
                    .forEach(s -> shipNameMap.put(s.getId(), s.getShipName()));
        }

        List<OperationRankVO> rankList = opPage.getRecords().stream().map(op -> {
            OperationRankVO rank = new OperationRankVO();
            rank.setQuayCraneNo(op.getQuayCraneNo());
            rank.setWorkEfficiency(op.getWorkEfficiency());
            rank.setTotalContainers(op.getTotalContainers());
            rank.setShipName(shipNameMap.getOrDefault(op.getShipId(), "未知"));
            return rank;
        }).collect(Collectors.toList());

        return Result.success(rankList);
    }

    @Override
    public Result<List<?>> exportShipData(StatisticsQueryDTO queryDTO) {
        QueryWrapper<ShipInfo> wrapper = new QueryWrapper<>();
        if (queryDTO.getStartDate() != null) {
            wrapper.ge("arrive_time", queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le("arrive_time", queryDTO.getEndDate());
        }

        List<ShipInfo> ships = shipInfoMapper.selectList(wrapper);
        return Result.success(ships);
    }
}
