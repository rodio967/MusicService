package com.example.user.oauth.client;

import com.example.user.enums.MusicPlatform;

import java.net.URI;

public interface OAuthProviderClient {

    MusicPlatform getPlatform();

    URI buildAuthorizationUri(String state);

}
