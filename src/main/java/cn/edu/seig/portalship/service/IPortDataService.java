package cn.edu.seig.portalship.service;

import cn.edu.seig.portalship.model.entity.PortData;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IPortDataService extends IService<PortData> {

    PortData getLatest();
}
