package cn.edu.seig.portalship.controller;

import cn.edu.seig.portalship.constant.JwtClaimsConstant;
import cn.edu.seig.portalship.constant.MessageConstant;
import cn.edu.seig.portalship.enumeration.UserStatusEnum;
import cn.edu.seig.portalship.mapper.UserMapper;
import cn.edu.seig.portalship.model.dto.UserLoginReqDTO;
import cn.edu.seig.portalship.model.dto.UserUpdatePwdDTO;
import cn.edu.seig.portalship.model.entity.User;
import cn.edu.seig.portalship.model.vo.UserLoginVO;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody @Valid UserLoginReqDTO dto) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (user == null) {
            return Result.error("用户名不存在");
        }
        if (user.getUserStatus() == UserStatusEnum.DISABLE) {
            return Result.error(MessageConstant.ACCOUNT_LOCKED);
        }
        if (!DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()).equals(user.getPassword())) {
            return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
        }

        Map<String, Object> claims = new HashMap<>();
        // 归一化角色格式：admin → ROLE_ADMIN，ROLE_ADMIN → ROLE_ADMIN
        String role = user.getRole();
        if (role != null && !role.toUpperCase().startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }
        claims.put(JwtClaimsConstant.ROLE, role != null ? role.toUpperCase() : "ROLE_USER");
        claims.put(JwtClaimsConstant.USER_ID, user.getUserId());
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        claims.put(JwtClaimsConstant.EMAIL, user.getEmail());
        String token = JwtUtil.generateToken(claims);
        stringRedisTemplate.opsForValue().set(token, token, 6, TimeUnit.HOURS);

        UserLoginVO vo = new UserLoginVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setEmail(user.getEmail());
        vo.setToken(token);

        return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, vo);
    }

    @PostMapping("/update-pwd")
    public Result updatePwd(@RequestBody @Valid UserUpdatePwdDTO dto) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (user == null) {
            return Result.error("用户名不存在");
        }
        if (!DigestUtils.md5DigestAsHex(dto.getOldPwd().getBytes()).equals(user.getPassword())) {
            return Result.error(MessageConstant.OLD_PASSWORD_ERROR);
        }
        if (dto.getOldPwd().equals(dto.getNewPwd())) {
            return Result.error(MessageConstant.NEW_PASSWORD_ERROR);
        }

        user.setPassword(DigestUtils.md5DigestAsHex(dto.getNewPwd().getBytes()));
        userMapper.updateById(user);
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }
}
