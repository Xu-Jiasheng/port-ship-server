package cn.edu.seig.portalship.model.dto;

import lombok.Data;

@Data
public class UserResetPasswordDTO {

    private String email;
    private String newPassword;
    private String repeatPassword;
    private String verificationCode;
}
