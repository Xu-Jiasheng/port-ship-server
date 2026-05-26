package cn.edu.seig.portalship.model.vo;

import lombok.Data;

@Data
public class UserLoginVO {

    private Long userId;
    private String username;
    private String role;
    private String email;
    private String token;
}
