package cn.edu.seig.portalship.controller;

import cn.edu.seig.portalship.mapper.ShipMapper;
import cn.edu.seig.portalship.model.dto.UserDTO;
import cn.edu.seig.portalship.model.entity.Ship;
import cn.edu.seig.portalship.model.entity.User;
import cn.edu.seig.portalship.model.vo.ShipInfoVO;
import cn.edu.seig.portalship.model.vo.UserVO;
import cn.edu.seig.portalship.result.PageResult;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.IShipService;
import cn.edu.seig.portalship.service.IUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IShipService shipService;
    @Autowired
    private ShipMapper shipMapper;

    @GetMapping("/getAllUsers")
    public Result<PageResult<UserVO>> getAllUsers(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username) {
        return userService.getAllUsers(pageNum, pageSize, username);
    }

    @PutMapping("/updateUser/{id}")
    public Result updateUser(@PathVariable("id") Long userId, @RequestBody UserDTO dto) {
        return userService.updateUser(userId, dto);
    }

    @PatchMapping("/updateUserStatus/{id}/{status}")
    public Result updateUserStatus(@PathVariable("id") Long userId, @PathVariable("status") Integer userStatus) {
        return userService.updateUserStatus(userId, userStatus);
    }

    @PatchMapping("/updateUserRole/{id}/{role}")
    public Result updateUserRole(@PathVariable("id") Long userId, @PathVariable("role") String role) {
        return userService.updateUserRole(userId, role);
    }

    @DeleteMapping("/deleteUser/{id}")
    public Result deleteUser(@PathVariable("id") Long userId) {
        return userService.deleteUser(userId);
    }

    @DeleteMapping("/deleteUsers")
    public Result deleteUsers(@RequestBody List<Long> userIds) {
        return userService.deleteUsers(userIds);
    }

    @GetMapping("/ship-list")
    public Result<PageResult<ShipInfoVO>> shipList(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        Page<Ship> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Ship> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        IPage<Ship> shipPage = shipMapper.selectPage(page, wrapper);
        List<ShipInfoVO> voList = shipPage.getRecords().stream().map(ship -> {
            ShipInfoVO vo = new ShipInfoVO();
            BeanUtils.copyProperties(ship, vo);
            return vo;
        }).toList();
        return Result.success(new PageResult<>(shipPage.getTotal(), voList));
    }

    @GetMapping("/user-list")
    public Result<List<UserVO>> userList() {
        List<User> users = userService.list(new QueryWrapper<User>().orderByDesc("create_time"));
        List<UserVO> voList = users.stream().map(user -> {
            UserVO vo = new UserVO();
            org.springframework.beans.BeanUtils.copyProperties(user, vo);
            // 角色格式统一：admin → ROLE_ADMIN
            String role = user.getRole();
            if (role != null && !role.startsWith("ROLE_")) {
                vo.setRole("ROLE_" + role.toUpperCase());
            }
            return vo;
        }).toList();
        return Result.success(voList);
    }
}
