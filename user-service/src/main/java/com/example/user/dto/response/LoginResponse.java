package com.example.user.dto.response;

import com.example.user.enums.Role;

import java.util.Set;

public record LoginResponse(
        String token,
        String tokenType,
        Long userId,
        String username,
        String email,
        Set<Role> roles
) {}
