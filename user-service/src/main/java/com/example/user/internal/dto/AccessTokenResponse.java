package com.example.user.internal.dto;

import java.time.Instant;

public record AccessTokenResponse(
        String accessToken,
        Instant expiresAt,
        String providerAccountId
) {}
