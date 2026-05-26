package cn.edu.seig.portalship.service;

import cn.edu.seig.portalship.model.dto.*;
import cn.edu.seig.portalship.model.entity.User;
import cn.edu.seig.portalship.model.vo.UserVO;
import cn.edu.seig.portalship.result.PageResult;
import cn.edu.seig.portalship.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IUserService extends IService<User> {

    Result sendVerificationCode(String email);

    boolean verifyVerificationCode(String email, String verificationCode);

    Result register(UserRegisterDTO userRegisterDTO);

    Result login(UserLoginDTO userLoginDTO);

    Result<UserVO> userInfo();

    Result updateUserInfo(UserDTO userDTO);

    Result updateUserAvatar(String avatarUrl);

    Result updateUserPassword(UserPasswordDTO userPasswordDTO, String token);

    Result resetUserPassword(UserResetPasswordDTO userResetPasswordDTO);

    Result logout(String token);

    Result<PageResult<UserVO>> getAllUsers(Integer pageNum, Integer pageSize, String username);

    Result updateUser(Long userId, UserDTO userDTO);

    Result updateUserStatus(Long userId, Integer userStatus);

    Result updateUserRole(Long userId, String role);

    Result deleteUser(Long userId);

    Result deleteUsers(List<Long> userIds);
}
