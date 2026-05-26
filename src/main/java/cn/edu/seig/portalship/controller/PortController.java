package cn.edu.seig.portalship.controller;

import cn.edu.seig.portalship.model.entity.PortData;
import cn.edu.seig.portalship.model.entity.Weather;
import cn.edu.seig.portalship.model.entity.WorkData;
import cn.edu.seig.portalship.model.vo.HomeDataVO;
import cn.edu.seig.portalship.result.Result;
import cn.edu.seig.portalship.service.IPortDataService;
import cn.edu.seig.portalship.service.IWeatherService;
import cn.edu.seig.portalship.service.IWorkDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/port")
public class PortController {

    @Autowired
    private IPortDataService portDataService;
    @Autowired
    private IWeatherService weatherService;
    @Autowired
    private IWorkDataService workDataService;

    @GetMapping("/home-data")
    public Result<HomeDataVO> homeData() {
        HomeDataVO vo = new HomeDataVO();

        PortData portData = portDataService.getLatest();
        if (portData != null) {
            HomeDataVO.PortDataVO pd = new HomeDataVO.PortDataVO();
            pd.setId(portData.getId());
            pd.setPortName(portData.getPortName());
            pd.setTotalBerths(portData.getTotalBerths());
            pd.setAvailableBerths(portData.getAvailableBerths());
            pd.setTodayThroughput(portData.getTodayThroughput());
            pd.setMonthlyThroughput(portData.getMonthlyThroughput());
            if (portData.getUpdateTime() != null) {
                pd.setUpdateTime(portData.getUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            vo.setPortData(pd);
        }

        Weather weather = weatherService.getLatest();
        if (weather != null) {
            HomeDataVO.WeatherVO w = new HomeDataVO.WeatherVO();
            w.setId(weather.getId());
            w.setTemperature(weather.getTemperature());
            w.setWindDirection(weather.getWindDirection());
            w.setWindSpeed(weather.getWindSpeed());
            w.setWaveHeight(weather.getWaveHeight());
            w.setVisibility(weather.getVisibility());
            w.setWeatherDesc(weather.getWeatherDesc());
            if (weather.getUpdateTime() != null) {
                w.setUpdateTime(weather.getUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            vo.setWeather(w);
        }

        WorkData workData = workDataService.getToday();
        if (workData != null) {
            HomeDataVO.WorkDataVO wd = new HomeDataVO.WorkDataVO();
            wd.setId(workData.getId());
            if (workData.getWorkDate() != null) {
                wd.setWorkDate(workData.getWorkDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            wd.setTotalShips(workData.getTotalShips());
            wd.setCompletedShips(workData.getCompletedShips());
            wd.setTotalContainers(workData.getTotalContainers());
            wd.setEfficiencyRate(workData.getEfficiencyRate());
            if (workData.getUpdateTime() != null) {
                wd.setUpdateTime(workData.getUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            vo.setWorkData(wd);
        }

        return Result.success(vo);
    }
}
