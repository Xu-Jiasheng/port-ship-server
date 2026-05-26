package cn.edu.seig.portalship.enumeration;

import lombok.Getter;

@Getter
public enum RoleEnum {

    ADMIN("ROLE_ADMIN"),
    OPERATOR("ROLE_OPERATOR"),
    VIEWER("ROLE_VIEWER");

    private final String role;

    RoleEnum(String role) {
        this.role = role;
    }
}
