package com.example.user.dto.response;

public record RegisterResponse(
        boolean success,
        String message,
        Long userId,
        String username
) {}


