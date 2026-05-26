package cn.edu.seig.portalship.service.impl;

import cn.edu.seig.portalship.constant.JwtClaimsConstant;
import cn.edu.seig.portalship.constant.MessageConstant;
import cn.edu.seig.portalship.mapper.ShipInfoMapper;
import cn.edu.seig.portalship.mapper.ShipMapper;
import cn.edu.seig.portalship.model.dto.ShipInfoAddDTO;
import cn.edu.seig.portalship.model.dto.ShipInfoDTO;
import cn.edu.seig.portalship.model.dto.ShipSearchDTO;
import cn.edu.seig.portalship.model.entity.Ship;
import cn.edu.seig.portalship.model.entity.ShipInfo;
import cn.edu.seig.portalship.model.vo.ShipInfoVO;
import cn.edu.seig.portalship.result.PageResult;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.IShipInfoService;
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

@Service
public class ShipInfoServiceImpl extends ServiceImpl<ShipInfoMapper, ShipInfo> implements IShipInfoService {

    @Autowired
    private ShipInfoMapper shipInfoMapper;
    @Autowired
    private ShipMapper shipMapper;

    @Override
    public Result addShip(ShipInfoAddDTO dto) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(claims.get(JwtClaimsConstant.USER_ID));

        ShipInfo ship = new ShipInfo();
        BeanUtils.copyProperties(dto, ship);
        ship.setCreateBy(userId);
        ship.setCreateTime(LocalDateTime.now());
        ship.setUpdateTime(LocalDateTime.now());
        if (ship.getStatus() == null || ship.getStatus().isEmpty()) {
            ship.setStatus("待靠泊");
        }

        shipInfoMapper.insert(ship);
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    @Override
    public Result updateShip(ShipInfoDTO dto) {
        ShipInfo ship = new ShipInfo();
        BeanUtils.copyProperties(dto, ship);
        ship.setUpdateTime(LocalDateTime.now());
        int rows = shipInfoMapper.updateById(ship);
        if (rows == 0 && dto.getId() != null) {
            Ship fallback = shipMapper.selectById(dto.getId());
            if (fallback != null) {
                if (dto.getShipName() != null) fallback.setShipName(dto.getShipName());
                if (dto.getCargoType() != null) fallback.setCargoType(dto.getCargoType());
                if (dto.getStatus() != null) fallback.setStatus(dto.getStatus());
                shipMapper.updateById(fallback);
                return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
            }
            return Result.error(MessageConstant.SHIP + MessageConstant.NOT_EXIST);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public Result deleteShip(Long id) {
        int rows = shipInfoMapper.deleteById(id);
        if (rows == 0) {
            rows = shipMapper.deleteById(id);
        }
        if (rows == 0) {
            return Result.error(MessageConstant.SHIP + MessageConstant.NOT_EXIST);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    @Override
    public Result<ShipInfoVO> getShipDetail(Long id) {
        ShipInfo ship = shipInfoMapper.selectById(id);
        if (ship != null) {
            ShipInfoVO vo = new ShipInfoVO();
            BeanUtils.copyProperties(ship, vo);
            return Result.success(vo);
        }
        Ship fallback = shipMapper.selectById(id);
        if (fallback != null) {
            ShipInfoVO vo = new ShipInfoVO();
            BeanUtils.copyProperties(fallback, vo);
            return Result.success(vo);
        }
        return Result.error(MessageConstant.SHIP + MessageConstant.NOT_EXIST);
    }

    @Override
    public Result<PageResult<ShipInfoVO>> getShipList(ShipSearchDTO searchDTO) {
        // First try tb_ship_info
        try {
            Page<ShipInfo> page = new Page<>(
                    searchDTO.getPageNum() != null ? searchDTO.getPageNum() : 1,
                    searchDTO.getPageSize() != null ? searchDTO.getPageSize() : 10);

            QueryWrapper<ShipInfo> queryWrapper = new QueryWrapper<>();
            if (searchDTO.getShipName() != null && !searchDTO.getShipName().isEmpty()) {
                queryWrapper.like("ship_name", searchDTO.getShipName());
            }
            if (searchDTO.getVoyageNo() != null && !searchDTO.getVoyageNo().isEmpty()) {
                queryWrapper.like("voyage_no", searchDTO.getVoyageNo());
            }
            if (searchDTO.getStatus() != null && !searchDTO.getStatus().isEmpty()) {
                queryWrapper.eq("status", searchDTO.getStatus());
            }
            if (searchDTO.getShipType() != null && !searchDTO.getShipType().isEmpty()) {
                queryWrapper.eq("ship_type", searchDTO.getShipType());
            }
            queryWrapper.orderByDesc("create_time");

            IPage<ShipInfo> shipPage = shipInfoMapper.selectPage(page, queryWrapper);

            if (shipPage.getTotal() > 0) {
                List<ShipInfoVO> voList = shipPage.getRecords().stream().map(ship -> {
                    ShipInfoVO vo = new ShipInfoVO();
                    BeanUtils.copyProperties(ship, vo);
                    return vo;
                }).toList();
                return Result.success(new PageResult<>(shipPage.getTotal(), voList));
            }
        } catch (Exception ignored) {
            // tb_ship_info table may not exist, fall through to tb_ships
        }

        // Fallback: query tb_ships instead
        Page<Ship> fallbackPage = new Page<>(
                searchDTO.getPageNum() != null ? searchDTO.getPageNum() : 1,
                searchDTO.getPageSize() != null ? searchDTO.getPageSize() : 10);

        QueryWrapper<Ship> fallbackWrapper = new QueryWrapper<>();
        if (searchDTO.getShipName() != null && !searchDTO.getShipName().isEmpty()) {
            fallbackWrapper.like("ship_name", searchDTO.getShipName());
        }
        if (searchDTO.getStatus() != null && !searchDTO.getStatus().isEmpty()) {
            fallbackWrapper.eq("status", searchDTO.getStatus());
        }
        fallbackWrapper.orderByDesc("create_time");

        IPage<Ship> fallbackShipPage = shipMapper.selectPage(fallbackPage, fallbackWrapper);
        List<ShipInfoVO> fallbackVoList = fallbackShipPage.getRecords().stream().map(ship -> {
            ShipInfoVO vo = new ShipInfoVO();
            BeanUtils.copyProperties(ship, vo);
            return vo;
        }).toList();

        return Result.success(new PageResult<>(fallbackShipPage.getTotal(), fallbackVoList));
    }

    @Override
    public Result updateShipStatus(Long id, String status) {
        int rows = shipInfoMapper.update(new ShipInfo().setStatus(status).setUpdateTime(LocalDateTime.now()),
                new QueryWrapper<ShipInfo>().eq("id", id));
        if (rows == 0) {
            rows = shipMapper.update(new Ship().setStatus(status),
                    new QueryWrapper<Ship>().eq("id", id));
        }
        if (rows == 0) {
            return Result.error(MessageConstant.SHIP + MessageConstant.NOT_EXIST);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }
}
