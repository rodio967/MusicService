package com.example.user.oauth.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "encryption")
public record TokenEncryptionProperties(
        @NotBlank String tokenSecret
) {}
