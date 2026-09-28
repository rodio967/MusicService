package com.example.user.oauth.client;

import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.dto.OAuthTokenResponse;
import com.example.user.oauth.dto.ProviderAccountInfo;
import com.example.user.oauth.properties.SpotifyOAuthProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;


@Component
public class SpotifyOAuthClient extends AbstractOAuthClient {

    public SpotifyOAuthClient(SpotifyOAuthProperties properties, RestClient restClient) {
        super(properties, restClient);
    }


    @Override
    public MusicPlatform getPlatform() {
        return MusicPlatform.SPOTIFY;
    }

    @Override
    public URI buildAuthorizationUri(String state) {
        return UriComponentsBuilder.fromUri(properties.authUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.clientId())
                .queryParam("redirect_uri", properties.redirectUri())
                .queryParam("scope", String.join(" ", properties.scopes()))
                .queryParam("state", state)
                .build()
                .toUri();
    }


    @Override
    protected OAuthTokenResponse fetchToken(String code) {
        return restClient.post()
                .uri(properties.tokenUri())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodeCredentials())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(buildRequestBody(code))
                .retrieve()
                .body(OAuthTokenResponse.class);
    }

    @Override
    protected ProviderAccountInfo fetchAccountInfo(String accessToken) {
        return restClient.get()
                .uri(properties.profileUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(ProviderAccountInfo.class);
    }

    @Override
    protected OAuthTokenResponse refreshToken(String refreshToken) {
        return restClient.post()
                .uri(properties.tokenUri())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodeCredentials())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(buildRequestBodyRefresh(refreshToken))
                .retrieve()
                .body(OAuthTokenResponse.class);
    }

    private MultiValueMap<String, String> buildRequestBody(String code) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("code", code);
        requestBody.add("redirect_uri", properties.redirectUri().toString());

        return requestBody;
    }

    private MultiValueMap<String, String> buildRequestBodyRefresh(String refreshToken) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "refresh_token");
        requestBody.add("refresh_token", refreshToken);

        return requestBody;
    }


}
