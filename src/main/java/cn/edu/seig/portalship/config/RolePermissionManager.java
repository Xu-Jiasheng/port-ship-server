package cn.edu.seig.portalship.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RolePermissionManager {

    private final RolePathPermissionsConfig rolePathPermissionsConfig;

    @Autowired
    public RolePermissionManager(RolePathPermissionsConfig rolePathPermissionsConfig) {
        this.rolePathPermissionsConfig = rolePathPermissionsConfig;
    }

    public boolean hasPermission(String role, String requestURI) {
        Map<String, List<String>> permissions = rolePathPermissionsConfig.getPermissions();
        List<String> allowedPaths = permissions.get(role);
        if (allowedPaths != null) {
            for (String path : allowedPaths) {
                if (requestURI.startsWith(path)) {
                    return true;
                }
            }
        }
        return false;
    }
}
