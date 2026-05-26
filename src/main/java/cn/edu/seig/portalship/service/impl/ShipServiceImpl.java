package cn.edu.seig.portalship.service.impl;

import cn.edu.seig.portalship.mapper.ShipMapper;
import cn.edu.seig.portalship.model.dto.ShipAddReqDTO;
import cn.edu.seig.portalship.model.entity.Ship;
import cn.edu.seig.portalship.service.IShipService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShipServiceImpl extends ServiceImpl<ShipMapper, Ship> implements IShipService {

    @Autowired
    private ShipMapper shipMapper;

    @Override
    public void addShip(ShipAddReqDTO dto, Long userId) {
        Ship ship = new Ship();
        ship.setShipName(dto.getShipName());
        ship.setCargoType(dto.getCargoType());
        ship.setCargoNum(dto.getCargoNum());
        ship.setDestination(dto.getDestination());
        ship.setStatus("待靠泊");
        ship.setCreateBy(userId);
        ship.setCreateTime(LocalDateTime.now());
        shipMapper.insert(ship);
    }

    @Override
    public List<Ship> getMyShips(Long userId) {
        QueryWrapper<Ship> wrapper = new QueryWrapper<>();
        wrapper.eq("create_by", userId).orderByDesc("create_time");
        return shipMapper.selectList(wrapper);
    }

    @Override
    public List<Ship> getAllShips() {
        QueryWrapper<Ship> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return shipMapper.selectList(wrapper);
    }
}
