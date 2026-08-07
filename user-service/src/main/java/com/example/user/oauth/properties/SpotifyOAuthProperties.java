package com.example.user.oauth.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.Set;

@Validated
@ConfigurationProperties(prefix = "oauth.providers.spotify")
public record SpotifyOAuthProperties(
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        @NotNull URI redirectUri,
        @NotEmpty Set<String> scopes
) {}
