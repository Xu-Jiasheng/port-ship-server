package cn.edu.seig.portalship.service;

import cn.edu.seig.portalship.model.dto.ShipAddReqDTO;
import cn.edu.seig.portalship.model.entity.Ship;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IShipService extends IService<Ship> {

    void addShip(ShipAddReqDTO dto, Long userId);

    List<Ship> getMyShips(Long userId);

    List<Ship> getAllShips();
}
