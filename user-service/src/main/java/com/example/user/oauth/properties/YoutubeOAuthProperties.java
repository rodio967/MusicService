package com.example.user.oauth.properties;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.Set;

@Validated
@ConfigurationProperties(prefix = "oauth.providers.youtube")
public record YoutubeOAuthProperties(
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        @NotNull URI redirectUri,
        @NotNull URI authUri,
        @NotNull URI tokenUri,
        @NotNull URI profileUri,
        @NotEmpty Set<String> scopes
) implements OAuthProviderProperties {}
