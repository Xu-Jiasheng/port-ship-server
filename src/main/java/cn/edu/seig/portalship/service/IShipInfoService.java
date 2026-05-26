package cn.edu.seig.portalship.service;

import cn.edu.seig.portalship.model.dto.ShipInfoAddDTO;
import cn.edu.seig.portalship.model.dto.ShipInfoDTO;
import cn.edu.seig.portalship.model.dto.ShipSearchDTO;
import cn.edu.seig.portalship.model.entity.ShipInfo;
import cn.edu.seig.portalship.model.vo.ShipInfoVO;
import cn.edu.seig.portalship.result.PageResult;
import cn.edu.seig.portalship.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IShipInfoService extends IService<ShipInfo> {

    Result addShip(ShipInfoAddDTO dto);

    Result updateShip(ShipInfoDTO dto);

    Result deleteShip(Long id);

    Result<ShipInfoVO> getShipDetail(Long id);

    Result<PageResult<ShipInfoVO>> getShipList(ShipSearchDTO searchDTO);

    Result updateShipStatus(Long id, String status);
}
