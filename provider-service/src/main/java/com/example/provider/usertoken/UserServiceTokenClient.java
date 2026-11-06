package com.example.provider.usertoken;

import com.example.common.ErrorCodes;
import com.example.common.MusicPlatform;
import com.example.provider.exception.ApiException;
import com.example.provider.exception.ProviderNotConnectedException;
import com.example.provider.exception.ProviderUnavailableException;
import com.example.provider.exception.ReauthorizationRequiredException;
import com.example.provider.exception.UserServiceUnavailableException;
import com.example.provider.model.PlatformAccess;
import com.example.provider.security.InternalApiProperties;
import com.example.provider.security.InternalSecretFilter;
import com.example.provider.usertoken.dto.AccessTokenResponse;
import com.example.provider.usertoken.dto.UserServiceErrorResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;


@Component
public class UserServiceTokenClient {

    private static final String ACCESS_TOKEN_PATH =
            "/api/internal/users/{userId}/connections/{platform}/access-token";

    private final RestClient restClient;
    private final String internalSecret;

    public UserServiceTokenClient(
            @Qualifier("userServiceRestClient") RestClient restClient,
            InternalApiProperties internalApiProperties
    ) {
        this.restClient = restClient;
        this.internalSecret = internalApiProperties.secret();
    }

    public PlatformAccess getPlatformAccess(Long userId, MusicPlatform platform) {
        AccessTokenResponse response;
        try {
            response = restClient.get()
                    .uri(ACCESS_TOKEN_PATH, userId, platform.pathValue())
                    .header(InternalSecretFilter.SECRET_HEADER, internalSecret)
                    .retrieve()
                    .body(AccessTokenResponse.class);
        } catch (RestClientResponseException ex) {
            throw translateError(ex, platform);
        } catch (RestClientException ex) {
            throw new UserServiceUnavailableException(ex);
        }

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new UserServiceUnavailableException(new IllegalStateException("Empty access token in response"));
        }

        return new PlatformAccess(response.accessToken(), response.providerAccountId());
    }


    private ApiException translateError(RestClientResponseException ex, MusicPlatform platform) {
        String code = fetchErrorCode(ex);

        return switch (code) {
            case ErrorCodes.OAUTH_CONNECTION_NOT_FOUND -> new ProviderNotConnectedException(platform);
            case ErrorCodes.REAUTHORIZATION_REQUIRED -> new ReauthorizationRequiredException(platform);
            case ErrorCodes.TOKEN_EXCHANGE_FAILED -> new ProviderUnavailableException(platform, ex);
            default -> new UserServiceUnavailableException(ex);
        };
    }

    private String fetchErrorCode(RestClientResponseException ex) {
        try {
            UserServiceErrorResponse error = ex.getResponseBodyAs(UserServiceErrorResponse.class);
            return error == null || error.code() == null ? "" : error.code();
        } catch (RestClientException e) {
            return "";
        }
    }
}
