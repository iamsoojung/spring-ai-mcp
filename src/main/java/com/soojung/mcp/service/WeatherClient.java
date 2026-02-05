package com.soojung.mcp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Open-Meteo Weather API 호출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherClient {

    private final RestClient weatherRestClient;

    /**
     * 위/경도로 현재 날씨 조회
     */
    public String getWeather(double lat, double lon) {

        log.info("날씨 API 호출 lat={}, lon={}", lat, lon);

        Map response = weatherRestClient.get()
            .uri("/v1/forecast?latitude={lat}&longitude={lon}&current_weather=true", lat, lon)
            .retrieve().body(Map.class);

        Map current = (Map) response.get("current_weather");

        double temp = (double) current.get("temperature");
        double wind = (double) current.get("windspeed");

        return "현재 기온 %.1f°C, 풍속 %.1f km/h".formatted(temp, wind);
    }
}
