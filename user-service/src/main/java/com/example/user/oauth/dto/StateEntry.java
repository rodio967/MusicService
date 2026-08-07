package com.example.user.oauth.dto;

import java.time.Instant;

public record StateEntry(
        Long userId,
        Instant expiresAt
) {}
