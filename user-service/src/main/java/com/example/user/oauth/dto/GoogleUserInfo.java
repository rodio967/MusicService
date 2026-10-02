package com.example.user.oauth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleUserInfo(
        @JsonProperty("sub") String sub,
        @JsonProperty("name") String name
) {}
