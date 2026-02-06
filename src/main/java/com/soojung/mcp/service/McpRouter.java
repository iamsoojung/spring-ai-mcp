package com.soojung.mcp.service;

import com.soojung.mcp.dto.McpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class McpRouter {

    private final ChatService chatService;

    public Object route(McpRequest request) {

        return switch (request.method()) {

            case "chat" -> new ChatResult(
                chatService.chat(request.params().message())
            );

            default -> throw new RuntimeException("Unknown method: " + request.method());
        };
    }

    private record ChatResult(String reply) {}
}
