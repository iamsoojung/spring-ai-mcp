package com.soojung.mcp.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Open-Meteo Geocoding API 호출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeocodingClient {

    private final RestClient geoRestClient;

    /**
     * 도시 이름 → 위도/경도 변환
     */
    public double[] getCoordinates(String city) {

        log.info("지오 코딩 API 호출 city={}", city);

        Map response = geoRestClient.get()
            .uri("/v1/search?name={city}&count=1&language=ko&format=json", city)
            .retrieve().body(Map.class);

        List results = (List) response.get("results");

        if (results == null || results.isEmpty()) {
            return null;
        }

        Map first = (Map) results.get(0);

        double lat = (double) first.get("latitude");
        double lon = (double) first.get("longitude");

        log.info("좌표 변환 성공 lat={}, lon={}", lat, lon);

        return new double[]{lat, lon};
    }
}
