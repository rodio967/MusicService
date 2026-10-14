package com.example.user.oauth.client;

import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.dto.OAuthTokenResponse;
import com.example.user.oauth.dto.ProviderAccountInfo;

import java.net.URI;

public interface OAuthProviderClient {

    MusicPlatform getPlatform();

    URI buildAuthorizationUri(String state);

    OAuthTokenResponse exchangeCode(String code);

    OAuthTokenResponse refreshAccessToken(String refreshToken);

    ProviderAccountInfo fetchProviderAccount(String accessToken);





}
