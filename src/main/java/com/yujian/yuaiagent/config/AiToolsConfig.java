package com.yujian.yuaiagent.config;

import com.yujian.yuaiagent.demo.advanced.WeatherFunctionModels.WeatherRequest;
import com.yujian.yuaiagent.demo.advanced.WeatherFunctionModels.WeatherResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.function.Function;

@Configuration
public class AiToolsConfig {

    @Bean
    public Function<WeatherRequest, WeatherResponse> getWeather() {
        Map<String, WeatherResponse> weatherMap = Map.of(
                "北京", new WeatherResponse("北京", "晴", 27),
                "上海", new WeatherResponse("上海", "多云", 24),
                "深圳", new WeatherResponse("深圳", "小雨", 29)
        );
        return request -> weatherMap.getOrDefault(
                request.city(),
                new WeatherResponse(request.city(), "未知", 22)
        );
    }
}
