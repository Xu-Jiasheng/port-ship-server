package cn.edu.seig.portalship.model.dto;

import lombok.Data;

@Data
public class UserPasswordDTO {

    private String oldPassword;
    private String newPassword;
    private String repeatPassword;
}
