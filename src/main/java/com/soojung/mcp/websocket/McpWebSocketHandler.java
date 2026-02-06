package com.soojung.mcp.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soojung.mcp.dto.McpRequest;
import com.soojung.mcp.dto.McpResponse;
import com.soojung.mcp.service.McpRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final McpRouter router;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("MCP client connected: {}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

        McpRequest request = objectMapper.readValue(message.getPayload().toString(), McpRequest.class);

        Object result = router.route(request);

        McpResponse response = new McpResponse("2.0", request.id(), result);

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    public void handleTransportError(WebSocketSession s, Throwable e) {}

    @Override
    public void afterConnectionClosed(WebSocketSession s, CloseStatus c) {}

    @Override
    public boolean supportsPartialMessages() {return false;}
}
