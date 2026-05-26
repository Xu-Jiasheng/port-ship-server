package cn.edu.seig.portalship.service;

import cn.edu.seig.portalship.model.entity.SystemLog;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ISystemLogService extends IService<SystemLog> {

    void log(Long userId, String username, String operation, String method, String params, String ip);
}
