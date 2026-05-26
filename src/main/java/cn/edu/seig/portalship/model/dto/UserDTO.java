package cn.edu.seig.portalship.model.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String role;
}
