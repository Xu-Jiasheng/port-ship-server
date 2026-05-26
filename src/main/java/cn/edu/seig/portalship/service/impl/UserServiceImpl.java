package cn.edu.seig.portalship.service.impl;

import cn.edu.seig.portalship.constant.JwtClaimsConstant;
import cn.edu.seig.portalship.constant.MessageConstant;
import cn.edu.seig.portalship.enumeration.RoleEnum;
import cn.edu.seig.portalship.enumeration.UserStatusEnum;
import cn.edu.seig.portalship.mapper.UserMapper;
import cn.edu.seig.portalship.model.dto.*;
import cn.edu.seig.portalship.model.entity.User;
import cn.edu.seig.portalship.model.vo.UserVO;
import cn.edu.seig.portalship.result.PageResult;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.EmailService;
import cn.edu.seig.portalship.service.ISystemLogService;
import cn.edu.seig.portalship.service.IUserService;
import cn.edu.seig.portalship.util.JwtUtil;
import cn.edu.seig.portalship.util.ThreadLocalUtil;
import cn.edu.seig.portalship.util.TypeConversionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ISystemLogService systemLogService;

    @Override
    public Result sendVerificationCode(String email) {
        // 60秒内不允许重复发送，防止邮件轰炸
        String rateLimitKey = "rateLimit:code:" + email;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(rateLimitKey))) {
            return Result.error("验证码已发送，请60秒后再试");
        }

        String verificationCode = emailService.sendVerificationCodeEmail(email);
        if (verificationCode == null) {
            return Result.error(MessageConstant.EMAIL_SEND_FAILED);
        }
        stringRedisTemplate.opsForValue().set("verificationCode:" + email, verificationCode, 5, TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set(rateLimitKey, "1", 60, TimeUnit.SECONDS);
        return Result.success(MessageConstant.EMAIL_SEND_SUCCESS);
    }

    @Override
    public boolean verifyVerificationCode(String email, String verificationCode) {
        String storedCode = stringRedisTemplate.opsForValue().get("verificationCode:" + email);
        return storedCode != null && storedCode.equals(verificationCode);
    }

    @Override
    public Result register(UserRegisterDTO dto) {
        stringRedisTemplate.delete("verificationCode:" + dto.getEmail());

        User userByUsername = userMapper.selectOne(new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (userByUsername != null) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }

        User userByEmail = userMapper.selectOne(new QueryWrapper<User>().eq("email", dto.getEmail()));
        if (userByEmail != null) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(dto.getUsername())
                .setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()))
                .setEmail(dto.getEmail())
                .setRole("user")
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now())
                .setUserStatus(UserStatusEnum.ENABLE);

        if (userMapper.insert(user) == 0) {
            return Result.error(MessageConstant.REGISTER + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.REGISTER + MessageConstant.SUCCESS);
    }

    @Override
    public Result login(UserLoginDTO dto) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", dto.getEmail()));
        if (user == null) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ERROR);
        }
        if (user.getUserStatus() == UserStatusEnum.DISABLE) {
            return Result.error(MessageConstant.ACCOUNT_LOCKED);
        }

        if (DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()).equals(user.getPassword())) {
            Map<String, Object> claims = new HashMap<>();
            // 使用归一化方法避免双重 ROLE_ 前缀
            claims.put(JwtClaimsConstant.ROLE, normalizeRole(user.getRole()));
            claims.put(JwtClaimsConstant.USER_ID, user.getUserId());
            claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
            claims.put(JwtClaimsConstant.EMAIL, user.getEmail());
            String token = JwtUtil.generateToken(claims);

            stringRedisTemplate.opsForValue().set(token, token, 6, TimeUnit.HOURS);

            // 异步记录登录日志
            systemLogService.log(user.getUserId(), user.getUsername(),
                    "用户登录", "POST /auth/login", "email=" + dto.getEmail(), "127.0.0.1");

            return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, token);
        }

        return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
    }

    @Override
    public Result<UserVO> userInfo() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(map.get(JwtClaimsConstant.USER_ID));
        User user = userMapper.selectById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setRole(normalizeRole(user.getRole()));
        return Result.success(userVO);
    }

    @Override
    public Result updateUserInfo(UserDTO dto) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(map.get(JwtClaimsConstant.USER_ID));

        User userByUsername = userMapper.selectOne(new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (userByUsername != null && !userByUsername.getUserId().equals(userId)) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }

        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setUserId(userId);
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public Result updateUserAvatar(String avatarUrl) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(map.get(JwtClaimsConstant.USER_ID));

        userMapper.update(new User().setAvatar(avatarUrl).setUpdateTime(LocalDateTime.now()),
                new QueryWrapper<User>().eq("id", userId));
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public Result updateUserPassword(UserPasswordDTO dto, String token) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(map.get(JwtClaimsConstant.USER_ID));
        User user = userMapper.selectById(userId);

        if (!user.getPassword().equals(DigestUtils.md5DigestAsHex(dto.getOldPassword().getBytes()))) {
            return Result.error(MessageConstant.OLD_PASSWORD_ERROR);
        }
        if (user.getPassword().equals(DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes()))) {
            return Result.error(MessageConstant.NEW_PASSWORD_ERROR);
        }
        if (!dto.getRepeatPassword().equals(dto.getNewPassword())) {
            return Result.error(MessageConstant.PASSWORD_NOT_MATCH);
        }

        userMapper.update(new User().setPassword(DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes()))
                .setUpdateTime(LocalDateTime.now()), new QueryWrapper<User>().eq("id", userId));

        stringRedisTemplate.delete(token);
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public Result resetUserPassword(UserResetPasswordDTO dto) {
        stringRedisTemplate.delete("verificationCode:" + dto.getEmail());

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", dto.getEmail()));
        if (user == null) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.NOT_EXIST);
        }
        if (!dto.getRepeatPassword().equals(dto.getNewPassword())) {
            return Result.error(MessageConstant.PASSWORD_NOT_MATCH);
        }

        userMapper.update(new User().setPassword(DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes()))
                .setUpdateTime(LocalDateTime.now()), new QueryWrapper<User>().eq("id", user.getUserId()));

        return Result.success(MessageConstant.PASSWORD + MessageConstant.RESET + MessageConstant.SUCCESS);
    }

    @Override
    public Result logout(String token) {
        Boolean result = stringRedisTemplate.delete(token);
        if (result != null && result) {
            return Result.success(MessageConstant.LOGOUT + MessageConstant.SUCCESS);
        }
        return Result.error(MessageConstant.LOGOUT + MessageConstant.FAILED);
    }

    @Override
    public Result<PageResult<UserVO>> getAllUsers(Integer pageNum, Integer pageSize, String username) {
        Page<User> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        queryWrapper.orderByDesc("create_time");

        IPage<User> userPage = userMapper.selectPage(page, queryWrapper);
        List<UserVO> voList = userPage.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setRole(normalizeRole(user.getRole()));
            return vo;
        }).toList();

        return Result.success(new PageResult<>(userPage.getTotal(), voList));
    }

    @Override
    public Result updateUser(Long userId, UserDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setUserId(userId);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public Result updateUserStatus(Long userId, Integer userStatus) {
        UserStatusEnum statusEnum = userStatus == 0 ? UserStatusEnum.ENABLE : UserStatusEnum.DISABLE;
        userMapper.update(new User().setUserStatus(statusEnum).setUpdateTime(LocalDateTime.now()),
                new QueryWrapper<User>().eq("id", userId));
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public Result updateUserRole(Long userId, String role) {
        // 存入数据库时去除 ROLE_ 前缀，保持与原始数据格式一致
        String dbRole = role;
        if (dbRole != null && dbRole.toUpperCase().startsWith("ROLE_")) {
            dbRole = dbRole.substring(5).toLowerCase();
        }
        userMapper.update(new User().setRole(dbRole).setUpdateTime(LocalDateTime.now()),
                new QueryWrapper<User>().eq("id", userId));
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public Result deleteUser(Long userId) {
        userMapper.deleteById(userId);
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    @Override
    public Result deleteUsers(List<Long> userIds) {
        userMapper.deleteByIds(userIds);
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 将数据库中存储的角色值统一转换为带 ROLE_ 前缀的格式，与前端约定保持一致
     */
    private String normalizeRole(String role) {
        if (role == null) return "ROLE_USER";
        String upper = role.toUpperCase();
        if (upper.startsWith("ROLE_")) return upper;
        return "ROLE_" + upper;
    }
}
