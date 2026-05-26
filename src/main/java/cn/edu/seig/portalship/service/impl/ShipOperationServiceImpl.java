package cn.edu.seig.portalship.service.impl;

import cn.edu.seig.portalship.constant.JwtClaimsConstant;
import cn.edu.seig.portalship.constant.MessageConstant;
import cn.edu.seig.portalship.mapper.ShipInfoMapper;
import cn.edu.seig.portalship.mapper.ShipOperationMapper;
import cn.edu.seig.portalship.model.dto.OperationSearchDTO;
import cn.edu.seig.portalship.model.dto.ShipOperationAddDTO;
import cn.edu.seig.portalship.model.dto.ShipOperationDTO;
import cn.edu.seig.portalship.model.entity.ShipInfo;
import cn.edu.seig.portalship.model.entity.ShipOperation;
import cn.edu.seig.portalship.model.vo.ShipOperationVO;
import cn.edu.seig.portalship.result.PageResult;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.IShipOperationService;
import cn.edu.seig.portalship.util.ThreadLocalUtil;
import cn.edu.seig.portalship.util.TypeConversionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ShipOperationServiceImpl extends ServiceImpl<ShipOperationMapper, ShipOperation> implements IShipOperationService {

    @Autowired
    private ShipOperationMapper operationMapper;
    @Autowired
    private ShipInfoMapper shipInfoMapper;

    @Override
    public Result addOperation(ShipOperationAddDTO dto) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(claims.get(JwtClaimsConstant.USER_ID));

        ShipOperation operation = new ShipOperation();
        BeanUtils.copyProperties(dto, operation);
        operation.setCreateBy(userId);
        operation.setCreateTime(LocalDateTime.now());

        operationMapper.insert(operation);
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    @Override
    public Result updateOperation(ShipOperationDTO dto) {
        ShipOperation operation = new ShipOperation();
        BeanUtils.copyProperties(dto, operation);
        operationMapper.updateById(operation);
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public Result deleteOperation(Long id) {
        operationMapper.deleteById(id);
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    @Override
    public Result<PageResult<ShipOperationVO>> getOperationList(OperationSearchDTO searchDTO) {
        Page<ShipOperation> page = new Page<>(
                searchDTO.getPageNum() != null ? searchDTO.getPageNum() : 1,
                searchDTO.getPageSize() != null ? searchDTO.getPageSize() : 10);

        QueryWrapper<ShipOperation> queryWrapper = new QueryWrapper<>();
        if (searchDTO.getShipId() != null) {
            queryWrapper.eq("ship_id", searchDTO.getShipId());
        }
        if (searchDTO.getWorkType() != null && !searchDTO.getWorkType().isEmpty()) {
            queryWrapper.eq("work_type", searchDTO.getWorkType());
        }
        queryWrapper.orderByDesc("create_time");

        IPage<ShipOperation> opPage = operationMapper.selectPage(page, queryWrapper);

        // 批量查询船名，避免 N+1 问题
        List<Long> shipIds = opPage.getRecords().stream()
                .map(ShipOperation::getShipId).distinct().toList();
        Map<Long, String> shipNameMap = new HashMap<>();
        if (!shipIds.isEmpty()) {
            shipInfoMapper.selectBatchIds(shipIds).forEach(s -> shipNameMap.put(s.getId(), s.getShipName()));
        }

        List<ShipOperationVO> voList = opPage.getRecords().stream().map(op -> {
            ShipOperationVO vo = new ShipOperationVO();
            BeanUtils.copyProperties(op, vo);
            vo.setShipName(shipNameMap.getOrDefault(op.getShipId(), "未知"));
            return vo;
        }).toList();

        return Result.success(new PageResult<>(opPage.getTotal(), voList));
    }
}
