package cn.edu.seig.portalship.service.impl;

import cn.edu.seig.portalship.mapper.WorkDataMapper;
import cn.edu.seig.portalship.model.entity.WorkData;
import cn.edu.seig.portalship.service.IWorkDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class WorkDataServiceImpl extends ServiceImpl<WorkDataMapper, WorkData> implements IWorkDataService {

    @Autowired
    private WorkDataMapper workDataMapper;

    @Override
    public WorkData getToday() {
        QueryWrapper<WorkData> wrapper = new QueryWrapper<>();
        wrapper.eq("work_date", LocalDate.now()).last("LIMIT 1");
        return workDataMapper.selectOne(wrapper);
    }
}
