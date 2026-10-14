package com.example.user.oauth.dto;

import com.example.user.enums.MusicPlatform;
import java.time.Instant;

public record StateEntry(
        Long userId,
        MusicPlatform platform,
        Instant expiresAt
) {}
