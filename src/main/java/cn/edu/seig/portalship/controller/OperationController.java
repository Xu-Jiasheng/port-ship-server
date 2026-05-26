package cn.edu.seig.portalship.controller;

import cn.edu.seig.portalship.model.dto.OperationSearchDTO;
import cn.edu.seig.portalship.model.dto.ShipOperationAddDTO;
import cn.edu.seig.portalship.model.dto.ShipOperationDTO;
import cn.edu.seig.portalship.model.vo.ShipOperationVO;
import cn.edu.seig.portalship.result.PageResult;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.IShipOperationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/operation")
public class OperationController {

    @Autowired
    private IShipOperationService operationService;

    @PostMapping("/addOperation")
    public Result addOperation(@RequestBody @Valid ShipOperationAddDTO dto) {
        return operationService.addOperation(dto);
    }

    @PutMapping("/updateOperation")
    public Result updateOperation(@RequestBody ShipOperationDTO dto) {
        return operationService.updateOperation(dto);
    }

    @DeleteMapping("/deleteOperation/{id}")
    public Result deleteOperation(@PathVariable("id") Long id) {
        return operationService.deleteOperation(id);
    }

    @PostMapping("/getAllOperations")
    public Result<PageResult<ShipOperationVO>> getAllOperations(@RequestBody OperationSearchDTO searchDTO) {
        return operationService.getOperationList(searchDTO);
    }
}
