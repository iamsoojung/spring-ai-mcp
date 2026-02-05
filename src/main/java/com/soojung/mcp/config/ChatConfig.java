package com.soojung.mcp.config;

import com.soojung.mcp.tool.WeatherTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient 생성 설정
 * - System Prompt
 * - Tool 등록
 */
@Configuration
@RequiredArgsConstructor
public class ChatConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, WeatherTool weatherTool, AiProperties properties) {

        //        String systemPrompt = """
        //            You are %s (version %s).
        //            %s
        //            Use tools when needed.
        //            """.formatted(properties.getName(), properties.getVersion(), properties.getDescription());

        String systemPrompt = """
            You are %s (version %s).
            %s
            
            IMPORTANT RULES:
            - When a tool is used, the tool result is the single source of truth.
            - Never make up data if the tool returns no data.
            - If the tool says data is unavailable, tell the user that the data is unavailable.
            - Do NOT fabricate weather information.
            - Do NOT use your own knowledge for weather.
            
            Use tools whenever the question requires real-world data.
            """.formatted(properties.getName(), properties.getVersion(), properties.getDescription());

        return builder.defaultSystem(systemPrompt).defaultTools(weatherTool).build();
    }
}