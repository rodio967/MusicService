package com.example.user.oauth.client;

import com.example.user.oauth.dto.OAuthTokenResponse;
import com.example.user.oauth.dto.ProviderAccountInfo;
import com.example.user.oauth.properties.OAuthProviderProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public abstract class AbstractOAuthClient implements OAuthProviderClient {

    protected final OAuthProviderProperties properties;
    protected final RestClient restClient;

    public AbstractOAuthClient(OAuthProviderProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public OAuthTokenResponse exchangeCode(String code) {
        log.info("[{}] Exchanging code for token", getPlatform());

        OAuthTokenResponse tokenResponse = fetchToken(code);

        if (tokenResponse == null || tokenResponse.accessToken() == null) {
            log.error("[{}] Failed to get token — empty response", getPlatform());
            throw new RuntimeException("Failed to get token from " + getPlatform()); // TODO заменить на собственное исключение
        }

        log.info("[{}] Token received successfully", getPlatform());

        return tokenResponse;
    }

    @Override
    public ProviderAccountInfo fetchProviderAccount(String accessToken) {
        log.info("[{}] Fetching account info", getPlatform());

        ProviderAccountInfo providerAccount = fetchAccountInfo(accessToken);

        if (providerAccount == null || providerAccount.id() == null) {
            log.error("[{}] Failed to get account info — empty response", getPlatform());
            throw new RuntimeException("Failed to get account info from " + getPlatform());
        }

        log.info("[{}] Account info received successfully", getPlatform());

        return providerAccount;
    }

    protected abstract OAuthTokenResponse fetchToken(String code);

    protected abstract ProviderAccountInfo fetchAccountInfo(String accessToken);

    protected String encodeCredentials() {
        String auth = properties.clientId() + ":" + properties.clientSecret();
        return Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }


}
