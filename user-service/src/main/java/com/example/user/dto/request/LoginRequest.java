package com.example.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "username не может быть пустым")
        String username,

        @NotBlank(message = "password не может быть пустым")
        String password
) {}
