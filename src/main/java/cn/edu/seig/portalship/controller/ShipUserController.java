package cn.edu.seig.portalship.controller;

import cn.edu.seig.portalship.constant.JwtClaimsConstant;
import cn.edu.seig.portalship.constant.MessageConstant;
import cn.edu.seig.portalship.model.dto.ShipAddReqDTO;
import cn.edu.seig.portalship.model.entity.Ship;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.IShipService;
import cn.edu.seig.portalship.util.ThreadLocalUtil;
import cn.edu.seig.portalship.util.TypeConversionUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ship-user")
public class ShipUserController {

    @Autowired
    private IShipService shipService;

    @PostMapping("/add")
    public Result add(@RequestBody @Valid ShipAddReqDTO dto) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(claims.get(JwtClaimsConstant.USER_ID));
        shipService.addShip(dto, userId);
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    @GetMapping("/my-list")
    public Result<List<Ship>> myList() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(claims.get(JwtClaimsConstant.USER_ID));
        List<Ship> list = shipService.getMyShips(userId);
        return Result.success(list);
    }
}
