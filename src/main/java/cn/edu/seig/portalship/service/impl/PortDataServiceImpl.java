package cn.edu.seig.portalship.service.impl;

import cn.edu.seig.portalship.mapper.PortDataMapper;
import cn.edu.seig.portalship.model.entity.PortData;
import cn.edu.seig.portalship.service.IPortDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PortDataServiceImpl extends ServiceImpl<PortDataMapper, PortData> implements IPortDataService {

    @Autowired
    private PortDataMapper portDataMapper;

    @Override
    public PortData getLatest() {
        QueryWrapper<PortData> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("update_time").last("LIMIT 1");
        return portDataMapper.selectOne(wrapper);
    }
}
