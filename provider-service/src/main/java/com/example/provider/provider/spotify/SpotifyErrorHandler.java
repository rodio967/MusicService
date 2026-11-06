package com.example.provider.provider.spotify;

import com.example.common.MusicPlatform;
import com.example.provider.exception.ProviderAccessDeniedException;
import com.example.provider.exception.ProviderUnavailableException;
import com.example.provider.exception.RateLimitedException;
import com.example.provider.exception.ReauthorizationRequiredException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;


public class SpotifyErrorHandler implements RestClient.ResponseSpec.ErrorHandler {

    private final MusicPlatform SPOTIFY = MusicPlatform.SPOTIFY;

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
        int status = response.getStatusCode().value();

        throw switch (status) {
            case 401 -> new ReauthorizationRequiredException(SPOTIFY);
            case 403 -> new ProviderAccessDeniedException(SPOTIFY);
            case 429 -> new RateLimitedException(SPOTIFY, retryAfter(response.getHeaders()));
            default -> new ProviderUnavailableException(
                    SPOTIFY, new IllegalStateException("Spotify responded " + status + " to " + request.getURI()));
        };
    }

    private Duration retryAfter(HttpHeaders headers) {
        try {
            String value = headers.getFirst(HttpHeaders.RETRY_AFTER);
            return value == null ? null : Duration.ofSeconds(Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
