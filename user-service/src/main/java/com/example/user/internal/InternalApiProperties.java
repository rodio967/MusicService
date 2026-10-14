package com.example.user.internal;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "internal.api")
public record InternalApiProperties(
        @NotBlank String secret
) {}
