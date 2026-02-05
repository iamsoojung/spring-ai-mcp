package com.soojung.mcp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 도시명 좌표로 변환
 */
@Service
@RequiredArgsConstructor
public class GeoService {

    private final GeocodingClient geocodingClient;

    public double[] getCoordinates(String city) {
        return geocodingClient.getCoordinates(city);
    }
}