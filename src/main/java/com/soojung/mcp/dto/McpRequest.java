package com.soojung.mcp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record McpRequest(
    String jsonrpc,
    String id,
    String method,
    ChatParams params
) {
    public record ChatParams(String message) {}
}
