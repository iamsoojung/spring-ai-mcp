package com.soojung.mcp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mcp.server")
public class AiProperties {

    // MCP server name
    private String name;

    // MCP server version
    private String version;

    // MCP server description
    private String description;
}