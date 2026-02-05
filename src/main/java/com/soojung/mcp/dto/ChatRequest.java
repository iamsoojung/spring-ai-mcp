package com.soojung.mcp.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
    @NotBlank(message = "메시지는 비어 있을 수 없습니다.") String message
) {}
