package cn.edu.seig.portalship.service.impl;

import cn.edu.seig.portalship.mapper.WeatherMapper;
import cn.edu.seig.portalship.model.entity.Weather;
import cn.edu.seig.portalship.service.IWeatherService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeatherServiceImpl extends ServiceImpl<WeatherMapper, Weather> implements IWeatherService {

    @Autowired
    private WeatherMapper weatherMapper;

    @Override
    public Weather getLatest() {
        QueryWrapper<Weather> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("update_time").last("LIMIT 1");
        return weatherMapper.selectOne(wrapper);
    }
}
