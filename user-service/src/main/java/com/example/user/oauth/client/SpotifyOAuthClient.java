package com.example.user.oauth.client;

import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.properties.SpotifyOAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;


@Component
@RequiredArgsConstructor
public class SpotifyOAuthClient extends AbstractOAuthClient {

    private final SpotifyOAuthProperties properties;

    private static final URI AUTHORIZATION_URI = URI.create("https://accounts.spotify.com/authorize");


    @Override
    public MusicPlatform getPlatform() {
        return MusicPlatform.SPOTIFY;
    }

    @Override
    public URI buildAuthorizationUri(String state) {
        return UriComponentsBuilder.fromUri(AUTHORIZATION_URI)
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.clientId())
                .queryParam("redirect_uri", properties.clientSecret())
                .queryParam("scope", String.join(" ", properties.scopes()))
                .queryParam("state", state)
                .build()
                .encode()
                .toUri();
    }


}
