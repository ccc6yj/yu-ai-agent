package com.yujian.yuaiagent.demo.advanced;

/**
 * Function Calling 示例中使用的请求和响应对象
 */
public final class WeatherFunctionModels {

    private WeatherFunctionModels() {
    }

    public record WeatherRequest(String city) {
    }

    public record WeatherResponse(String city, String weather, Integer temperature) {
    }
}
