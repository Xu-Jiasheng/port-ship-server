package cn.edu.seig.portalship.service.impl;

import cn.edu.seig.portalship.mapper.SystemLogMapper;
import cn.edu.seig.portalship.model.entity.SystemLog;
import cn.edu.seig.portalship.service.ISystemLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLog> implements ISystemLogService {

    /**
     * 异步记录系统操作日志，不影响主业务流程
     */
    @Override
    @Async
    public void log(Long userId, String username, String operation, String method, String params, String ip) {
        SystemLog log = new SystemLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperation(operation);
        log.setMethod(method);
        log.setParams(params);
        log.setIp(ip);
        log.setCreateTime(LocalDateTime.now());
        save(log);
    }
}
