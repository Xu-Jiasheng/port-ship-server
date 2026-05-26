package cn.edu.seig.portalship.model.vo;

import lombok.Data;

@Data
public class UserVO {

    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private Integer status;
    private String createTime;
}
