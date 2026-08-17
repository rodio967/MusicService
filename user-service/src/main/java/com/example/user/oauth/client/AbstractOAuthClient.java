package com.example.user.oauth.client;

import com.example.user.oauth.dto.OAuthTokenResponse;
import com.example.user.oauth.dto.ProviderAccountInfo;
import com.example.user.oauth.exception.ProviderAccountException;
import com.example.user.oauth.exception.TokenExchangeException;
import com.example.user.oauth.properties.OAuthProviderProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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

        OAuthTokenResponse tokenResponse;
        try {
            tokenResponse = fetchToken(code);
        } catch (RestClientException ex) {
            throw new TokenExchangeException("Не удалось обменять authorization code на токен", ex);
        }

        if (tokenResponse == null
                || tokenResponse.accessToken() == null
                || tokenResponse.accessToken().isBlank()
                || tokenResponse.expiresIn() == null) {
            log.error("[{}] Ошибка при получении токена: Провайдер вернул некорректный ответ", getPlatform());
            throw new TokenExchangeException("Провайдер вернул некорректный ответ с токеном");
        }

        log.info("[{}] Token received successfully", getPlatform());

        return tokenResponse;
    }

    @Override
    public ProviderAccountInfo fetchProviderAccount(String accessToken) {
        log.info("[{}] Fetching account info", getPlatform());

        ProviderAccountInfo providerAccount;
        try {
            providerAccount = fetchAccountInfo(accessToken);
        } catch (RestClientException ex) {
            throw new ProviderAccountException("Не удалось получить профиль пользователя у провайдера", ex);
        }

        if (providerAccount == null || providerAccount.id() == null) {
            log.error("[{}] Ошибка при получении account info: Пустой ответ", getPlatform());
            throw new ProviderAccountException("Провайдер вернул пустой профиль пользователя");
        }

        log.info("[{}] Account info received successfully", getPlatform());

        return providerAccount;
    }

    @Override
    public OAuthTokenResponse refreshAccessToken(String refreshToken) {
        log.info("[{}] Refreshing access token", getPlatform());

        OAuthTokenResponse tokenResponse;
        try {
            tokenResponse = refreshToken(refreshToken);
        } catch (RestClientException ex) {
            throw new TokenExchangeException("Ошибка при операции refresh", ex);
        }

        if (tokenResponse == null
                || tokenResponse.accessToken() == null
                || tokenResponse.accessToken().isBlank()
                || tokenResponse.expiresIn() == null) {
            log.error("[{}] Ошибка при refresh: Провайдер вернул некорректный ответ", getPlatform());
            throw new TokenExchangeException("Провайдер вернул некорректный ответ при refresh");
        }

        log.info("[{}] Refresh completed successfully", getPlatform());

        return tokenResponse;
    }

    protected abstract OAuthTokenResponse fetchToken(String code);

    protected abstract ProviderAccountInfo fetchAccountInfo(String accessToken);

    protected abstract OAuthTokenResponse refreshToken(String refreshToken);

    protected String encodeCredentials() {
        String auth = properties.clientId() + ":" + properties.clientSecret();
        return Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }


}
