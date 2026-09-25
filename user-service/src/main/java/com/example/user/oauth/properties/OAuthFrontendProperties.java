package com.example.user.oauth.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Validated
@ConfigurationProperties(prefix = "oauth.frontend")
public record OAuthFrontendProperties(
        @NotNull URI redirectUri
) {}
