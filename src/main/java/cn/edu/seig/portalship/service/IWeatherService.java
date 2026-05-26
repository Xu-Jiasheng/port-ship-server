package cn.edu.seig.portalship.service;

import cn.edu.seig.portalship.model.entity.Weather;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IWeatherService extends IService<Weather> {

    Weather getLatest();
}
