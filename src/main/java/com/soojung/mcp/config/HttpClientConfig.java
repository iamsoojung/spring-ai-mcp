package com.soojung.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * 외부 API 호출용 RestClient Bean
 */
@Configuration
public class HttpClientConfig {

    // 날씨 API
    @Bean
    public RestClient weatherRestClient() {
        return RestClient.builder().baseUrl("https://api.open-meteo.com").build();
    }

    // 지오 코딩 API
    @Bean
    public RestClient geoRestClient() {
        return RestClient.builder().baseUrl("https://geocoding-api.open-meteo.com").build();
    }
}
