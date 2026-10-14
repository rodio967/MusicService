package com.example.user.oauth.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "oauth.state")
public record OAuthStateProperties(
        @NotNull StoreType store,
        @NotNull Duration ttl
) {

    public enum StoreType {
        MEMORY,
        REDIS
    }
}
