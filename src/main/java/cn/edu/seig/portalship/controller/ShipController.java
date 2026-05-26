package cn.edu.seig.portalship.controller;

import cn.edu.seig.portalship.model.dto.ShipInfoAddDTO;
import cn.edu.seig.portalship.model.dto.ShipInfoDTO;
import cn.edu.seig.portalship.model.dto.ShipSearchDTO;
import cn.edu.seig.portalship.model.vo.ShipInfoVO;
import cn.edu.seig.portalship.result.PageResult;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.IShipInfoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ship")
public class ShipController {

    @Autowired
    private IShipInfoService shipInfoService;

    @PostMapping("/addShip")
    public Result addShip(@RequestBody @Valid ShipInfoAddDTO dto) {
        return shipInfoService.addShip(dto);
    }

    @PutMapping("/updateShip")
    public Result updateShip(@RequestBody ShipInfoDTO dto) {
        return shipInfoService.updateShip(dto);
    }

    @DeleteMapping("/deleteShip/{id}")
    public Result deleteShip(@PathVariable("id") Long id) {
        return shipInfoService.deleteShip(id);
    }

    @GetMapping("/getShipDetail/{id}")
    public Result<ShipInfoVO> getShipDetail(@PathVariable("id") Long id) {
        return shipInfoService.getShipDetail(id);
    }

    @PostMapping("/getAllShips")
    public Result<PageResult<ShipInfoVO>> getAllShips(@RequestBody ShipSearchDTO searchDTO) {
        return shipInfoService.getShipList(searchDTO);
    }

    @PatchMapping("/updateShipStatus/{id}/{status}")
    public Result updateShipStatus(@PathVariable("id") Long id, @PathVariable("status") String status) {
        return shipInfoService.updateShipStatus(id, status);
    }
}
