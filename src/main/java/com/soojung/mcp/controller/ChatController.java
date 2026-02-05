package com.soojung.mcp.controller;

import com.soojung.mcp.dto.ChatRequest;
import com.soojung.mcp.dto.ChatResponse;
import com.soojung.mcp.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String reply = chatService.chat(request.message());
        return new ChatResponse(reply);
    }
}
