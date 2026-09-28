package com.example.user.oauth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProviderAccountInfo(
        @JsonProperty("id")
        String id,

        @JsonProperty("display_name")
        String displayName
) {}
