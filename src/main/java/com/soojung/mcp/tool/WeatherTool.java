package com.soojung.mcp.tool;

import com.soojung.mcp.service.GeoService;
import com.soojung.mcp.service.WeatherClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherTool {

    private final GeoService geoService;
    private final WeatherClient weatherClient;

    @Tool(description = "도시 이름으로 현재 날씨를 조회한다")
    public String getWeather(String city) {

        long start = System.currentTimeMillis();
        log.info(">>> TOOL START : getWeather(city={})", city);

//        String result = switch (city.toLowerCase()) {
//            case "seoul" -> "서울은 현재 5도이며 맑습니다.";
//            case "busan" -> "부산은 현재 8도이며 바람이 붑니다.";
//            default -> "해당 도시의 날씨 정보를 찾을 수 없습니다.";
//        };

        double[] coord = geoService.getCoordinates(city);

        if (coord == null) {
            return city + "의 위치를 찾을 수 없습니다.";
        }

        String weather = weatherClient.getWeather(coord[0], coord[1]);

        long end = System.currentTimeMillis();
        log.info("<<< TOOL END : {} ({}ms)", weather, (end - start));

        return city + "의 날씨: " + weather;
    }
}
