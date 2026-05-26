package cn.edu.seig.portalship.controller;

import cn.edu.seig.portalship.model.dto.*;
import cn.edu.seig.portalship.model.vo.UserVO;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.IUserService;
import cn.edu.seig.portalship.service.MinioService;
import cn.edu.seig.portalship.util.BindingResultUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IUserService userService;
    @Autowired
    private MinioService minioService;

    @GetMapping("/sendVerificationCode")
    public Result sendVerificationCode(@RequestParam @Email String email) {
        return userService.sendVerificationCode(email);
    }

    @PostMapping("/register")
    public Result register(@RequestBody @Valid UserRegisterDTO dto, BindingResult bindingResult) {
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }
        boolean isCodeValid = userService.verifyVerificationCode(dto.getEmail(), dto.getVerificationCode());
        if (!isCodeValid) {
            return Result.error("验证码无效");
        }
        return userService.register(dto);
    }

    @PostMapping("/login")
    public Result login(@RequestBody @Valid UserLoginDTO dto, BindingResult bindingResult) {
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }
        return userService.login(dto);
    }

    @GetMapping("/getUserInfo")
    public Result<UserVO> getUserInfo() {
        return userService.userInfo();
    }

    @PutMapping("/updateUserInfo")
    public Result updateUserInfo(@RequestBody @Valid UserDTO dto, BindingResult bindingResult) {
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }
        return userService.updateUserInfo(dto);
    }

    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam("avatar") MultipartFile avatar) {
        String avatarUrl = minioService.uploadFile(avatar, "avatars");
        return userService.updateUserAvatar(avatarUrl);
    }

    @PatchMapping("/updatePassword")
    public Result updatePassword(@RequestBody @Valid UserPasswordDTO dto,
                                  @RequestHeader("Authorization") String token, BindingResult bindingResult) {
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }
        return userService.updateUserPassword(dto, token);
    }

    @PatchMapping("/resetPassword")
    public Result resetPassword(@RequestBody @Valid UserResetPasswordDTO dto, BindingResult bindingResult) {
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }
        boolean isCodeValid = userService.verifyVerificationCode(dto.getEmail(), dto.getVerificationCode());
        if (!isCodeValid) {
            return Result.error("验证码无效");
        }
        return userService.resetUserPassword(dto);
    }

    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        return userService.logout(token);
    }
}
