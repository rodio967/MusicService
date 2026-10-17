package com.example.provider.usertoken.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record AccessTokenResponse(String accessToken) {}
