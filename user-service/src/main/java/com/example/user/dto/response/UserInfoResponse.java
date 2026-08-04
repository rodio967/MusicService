package com.example.user.dto.response;


import java.util.List;


public record UserInfoResponse(
        Long userId,
        String username,
        String email,
        List<String> roles
) {}
