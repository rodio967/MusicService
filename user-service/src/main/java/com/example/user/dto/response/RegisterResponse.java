package com.example.user.dto.response;

public record RegisterResponse(
        String message,
        Long userId,
        String username
) {}


