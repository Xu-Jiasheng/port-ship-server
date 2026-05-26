package cn.edu.seig.portalship.service;

import cn.edu.seig.portalship.model.dto.OperationSearchDTO;
import cn.edu.seig.portalship.model.dto.ShipOperationAddDTO;
import cn.edu.seig.portalship.model.dto.ShipOperationDTO;
import cn.edu.seig.portalship.model.entity.ShipOperation;
import cn.edu.seig.portalship.model.vo.ShipOperationVO;
import cn.edu.seig.portalship.result.PageResult;
import cn.edu.seig.portalship.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IShipOperationService extends IService<ShipOperation> {

    Result addOperation(ShipOperationAddDTO dto);

    Result updateOperation(ShipOperationDTO dto);

    Result deleteOperation(Long id);

    Result<PageResult<ShipOperationVO>> getOperationList(OperationSearchDTO searchDTO);
}
