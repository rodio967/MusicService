package com.example.provider.usertoken;

import com.example.common.MusicPlatform;
import com.example.provider.exception.ProviderNotConnectedException;
import com.example.provider.exception.ProviderUnavailableException;
import com.example.provider.exception.ReauthorizationRequiredException;
import com.example.provider.exception.UserServiceUnavailableException;
import com.example.provider.model.PlatformAccess;
import com.example.provider.security.InternalApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserServiceTokenClientTest {

    private static final String TOKEN_URL =
            "http://user-service/api/internal/users/42/connections/spotify/access-token";

    private MockRestServiceServer server;
    private UserServiceTokenClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://user-service");
        server = MockRestServiceServer.bindTo(builder).build();

        client = new UserServiceTokenClient(
                builder.build(),
                new InternalApiProperties("internal-secret")
        );
    }

    @Test
    void returnsAccessTokenAndSendsInternalSecret() {
        server.expect(requestTo(TOKEN_URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Internal-Secret", "internal-secret"))
                .andRespond(withSuccess(
                        """
                        {"accessToken":"spotify-token","expiresAt":"2026-10-17T12:00:00Z","providerAccountId":"spotify-user"}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertThat(client.getPlatformAccess(42L, MusicPlatform.SPOTIFY))
                .isEqualTo(new PlatformAccess("spotify-token", "spotify-user"));
        server.verify();
    }

    @Test
    void connectionNotFoundMeansProviderNotConnected() {
        respondWithError(HttpStatus.NOT_FOUND, "OAUTH_CONNECTION_NOT_FOUND");

        assertThatThrownBy(() -> client.getPlatformAccess(42L, MusicPlatform.SPOTIFY))
                .isInstanceOf(ProviderNotConnectedException.class)
                .hasMessage("Аккаунт Spotify не подключён");
    }

    @Test
    void reauthorizationRequiredIsPassedThrough() {
        respondWithError(HttpStatus.CONFLICT, "REAUTHORIZATION_REQUIRED");

        assertThatThrownBy(() -> client.getPlatformAccess(42L, MusicPlatform.SPOTIFY))
                .isInstanceOf(ReauthorizationRequiredException.class);
    }

    @Test
    void failedTokenRefreshMeansProviderUnavailable() {
        respondWithError(HttpStatus.BAD_GATEWAY, "TOKEN_EXCHANGE_FAILED");

        assertThatThrownBy(() -> client.getPlatformAccess(42L, MusicPlatform.SPOTIFY))
                .isInstanceOf(ProviderUnavailableException.class);
    }

    @Test
    void notFoundWithoutKnownCodeMeansUserServiceUnavailable() {
        respondWithError(HttpStatus.NOT_FOUND, "NOT_FOUND");

        assertThatThrownBy(() -> client.getPlatformAccess(42L, MusicPlatform.SPOTIFY))
                .isInstanceOf(UserServiceUnavailableException.class);
    }

    @Test
    void wrongSecretMeansUserServiceUnavailable() {
        server.expect(requestTo(TOKEN_URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.getPlatformAccess(42L, MusicPlatform.SPOTIFY))
                .isInstanceOf(UserServiceUnavailableException.class);
    }

    @Test
    void serverErrorMeansUserServiceUnavailable() {
        server.expect(requestTo(TOKEN_URL)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.getPlatformAccess(42L, MusicPlatform.SPOTIFY))
                .isInstanceOf(UserServiceUnavailableException.class);
    }

    @Test
    void htmlErrorPageMeansUserServiceUnavailable() {
        server.expect(requestTo(TOKEN_URL)).andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                .contentType(MediaType.TEXT_HTML)
                .body("<html><body>502 Bad Gateway</body></html>"));

        assertThatThrownBy(() -> client.getPlatformAccess(42L, MusicPlatform.SPOTIFY))
                .isInstanceOf(UserServiceUnavailableException.class);
    }

    @Test
    void malformedJsonErrorMeansUserServiceUnavailable() {
        server.expect(requestTo(TOKEN_URL)).andRespond(withStatus(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{not json"));

        assertThatThrownBy(() -> client.getPlatformAccess(42L, MusicPlatform.SPOTIFY))
                .isInstanceOf(UserServiceUnavailableException.class);
    }

    @Test
    void connectionFailureMeansUserServiceUnavailable() {
        server.expect(requestTo(TOKEN_URL)).andRespond(withException(new IOException("Connection refused")));

        assertThatThrownBy(() -> client.getPlatformAccess(42L, MusicPlatform.SPOTIFY))
                .isInstanceOf(UserServiceUnavailableException.class);
    }

    @Test
    void emptyTokenMeansUserServiceUnavailable() {
        server.expect(requestTo(TOKEN_URL))
                .andRespond(withSuccess("{\"accessToken\":\"\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.getPlatformAccess(42L, MusicPlatform.SPOTIFY))
                .isInstanceOf(UserServiceUnavailableException.class);
    }

    private void respondWithError(HttpStatus status, String code) {
        String body = """
                {"timestamp":"2026-10-17T12:00:00Z","status":%d,"code":"%s","message":"...","path":"/api/internal"}
                """.formatted(status.value(), code);

        server.expect(requestTo(TOKEN_URL))
                .andRespond(withStatus(status).contentType(MediaType.APPLICATION_JSON).body(body));
    }
}
