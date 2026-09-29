package com.example.user.oauth.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "oauth.http")
public record OAuthHttpProperties(
        @NotNull Duration connectTimeout,
        @NotNull Duration readTimeout
) {}
