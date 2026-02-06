package com.soojung.mcp.dto;

public record McpResponse(
    String jsonrpc,
    String id,
    Object result
) {}
