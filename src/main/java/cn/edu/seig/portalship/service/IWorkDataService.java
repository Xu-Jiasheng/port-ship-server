package cn.edu.seig.portalship.service;

import cn.edu.seig.portalship.model.entity.WorkData;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IWorkDataService extends IService<WorkData> {

    WorkData getToday();
}
