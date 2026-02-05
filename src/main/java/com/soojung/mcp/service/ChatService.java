package com.soojung.mcp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;

    public String chat(String message) {
        long start = System.currentTimeMillis();

        // request log
        log.info("\n========== LLM REQUEST ==========");
        log.info("User Message : {}", message);
        log.info("=================================");

        // LLM call
        String response = chatClient.prompt().user(message).call().content();

        long end = System.currentTimeMillis();

        // response log
        log.info("\n========== LLM RESPONSE ==========");
        log.info("Answer : {}", response);
        log.info("Latency : {} ms", (end - start));
        log.info("==================================\n");

        return response;
    }
}
