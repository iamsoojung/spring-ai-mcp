package com.soojung.mcp.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WeatherTool {

    @Tool(description = "도시 이름으로 현재 날씨를 조회한다")
    public String getWeather(String city) {
        long start = System.currentTimeMillis();
        log.info(">>> TOOL START : getWeather(city={})", city);

        // TODO: 실제 날씨 API 호출
        String result = switch (city.toLowerCase()) {
            case "seoul" -> "서울은 현재 5도이며 맑습니다.";
            case "busan" -> "부산은 현재 8도이며 바람이 붑니다.";
            default -> "해당 도시의 날씨 정보를 찾을 수 없습니다.";
        };

        long end = System.currentTimeMillis();
        log.info("<<< TOOL END : result={}, latency={}ms", result, (end - start));

        return result;
    }
}
