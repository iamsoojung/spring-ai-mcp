package com.soojung.mcp.stdio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soojung.mcp.dto.McpRequest;
import com.soojung.mcp.dto.McpResponse;
import com.soojung.mcp.service.McpRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpStdioRunner implements CommandLineRunner {

    private final McpRouter router;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {

        // --stdio 옵션 있을 때만 실행
        if (!System.getProperty("mcp.stdio", "false").equals("true")) {
            return;
        }

        log.info("MCP stdio mode started");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String line;
        while ((line = reader.readLine()) != null) {

            McpRequest request = objectMapper.readValue(line, McpRequest.class);

            Object result = router.route(request);

            McpResponse response = new McpResponse("2.0", request.id(), result);

            System.out.println(objectMapper.writeValueAsString(response));
            System.out.flush();
        }
    }
}
