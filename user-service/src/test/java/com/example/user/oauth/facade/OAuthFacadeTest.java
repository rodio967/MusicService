package com.example.user.oauth.facade;

import com.example.user.enums.ConnectionStatus;
import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.client.OAuthProviderClient;
import com.example.user.oauth.connection.OAuthConnectionEntity;
import com.example.user.oauth.connection.OAuthConnectionService;
import com.example.user.oauth.dto.OAuthTokenResponse;
import com.example.user.oauth.exception.ReauthorizationRequiredException;
import com.example.user.oauth.registry.OAuthClientRegistry;
import com.example.user.oauth.state.OAuthStateStore;
import com.example.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class OAuthFacadeTest {

    private static final Long USER_ID = 42L;
    private static final Long CONNECTION_ID = 7L;

    private OAuthConnectionService connectionService;
    private OAuthProviderClient client;
    private OAuthFacade facade;

    @BeforeEach
    void setUp() {
        OAuthClientRegistry clients = mock(OAuthClientRegistry.class);
        connectionService = mock(OAuthConnectionService.class);
        client = mock(OAuthProviderClient.class);
        when(clients.get(MusicPlatform.SPOTIFY)).thenReturn(client);

        facade = new OAuthFacade(
                clients,
                connectionService,
                mock(OAuthStateStore.class),
                mock(UserService.class)
        );
    }

    @Test
    void marksConnectionWhenProviderRejectsRefreshToken() {
        givenConnection(expiredConnection("refresh-token"));
        when(client.refreshAccessToken("refresh-token"))
                .thenThrow(new ReauthorizationRequiredException(MusicPlatform.SPOTIFY));

        assertThatThrownBy(() -> facade.getValidAccessToken(USER_ID, "spotify"))
                .isInstanceOf(ReauthorizationRequiredException.class);

        verify(connectionService).markReauthorizationRequired(CONNECTION_ID);
        verify(connectionService, never()).updateTokens(anyLong(), any());
    }

    @Test
    void doesNotCallProviderWhenReauthorizationAlreadyRequired() {
        OAuthConnectionEntity connection = expiredConnection("refresh-token");
        connection.setStatus(ConnectionStatus.REAUTHORIZATION_REQUIRED);
        givenConnection(connection);

        assertThatThrownBy(() -> facade.getValidAccessToken(USER_ID, "spotify"))
                .isInstanceOf(ReauthorizationRequiredException.class);

        verifyNoInteractions(client);
    }

    @Test
    void marksConnectionWhenRefreshTokenIsMissing() {
        givenConnection(expiredConnection(null));

        assertThatThrownBy(() -> facade.getValidAccessToken(USER_ID, "spotify"))
                .isInstanceOf(ReauthorizationRequiredException.class);

        verify(connectionService).markReauthorizationRequired(CONNECTION_ID);
        verifyNoInteractions(client);
    }

    @Test
    void refreshesExpiredTokenOfActiveConnection() {
        givenConnection(expiredConnection("refresh-token"));
        OAuthTokenResponse tokenResponse =
                new OAuthTokenResponse("new-access-token", "Bearer", 3600L, null, null);
        when(client.refreshAccessToken("refresh-token")).thenReturn(tokenResponse);

        String accessToken = facade.getValidAccessToken(USER_ID, "spotify");

        assertThat(accessToken).isEqualTo("new-access-token");
        verify(connectionService).updateTokens(CONNECTION_ID, tokenResponse);
        verify(connectionService, never()).markReauthorizationRequired(anyLong());
    }

    @Test
    void concurrentRequestsRefreshTokenOnlyOnce() throws Exception {
        AtomicReference<OAuthConnectionEntity> storedConnection =
                new AtomicReference<>(expiredConnection("refresh-token"));
        when(connectionService.getConnectionByUserIdAndPlatform(USER_ID, MusicPlatform.SPOTIFY))
                .thenAnswer(invocation -> expiredConnection("refresh-token"));
        when(connectionService.getConnectionById(CONNECTION_ID))
                .thenAnswer(invocation -> storedConnection.get());

        OAuthTokenResponse tokenResponse =
                new OAuthTokenResponse("new-access-token", "Bearer", 3600L, null, null);
        when(client.refreshAccessToken("refresh-token")).thenAnswer(invocation -> {
            Thread.sleep(100);
            return tokenResponse;
        });
        when(connectionService.updateTokens(CONNECTION_ID, tokenResponse)).thenAnswer(invocation -> {
            OAuthConnectionEntity refreshed = expiredConnection("refresh-token");
            refreshed.setAccessToken("new-access-token");
            refreshed.setExpiresAt(Instant.now().plusSeconds(3600));
            storedConnection.set(refreshed);
            return refreshed;
        });

        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<String>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                results.add(executor.submit(() -> {
                    start.await();
                    return facade.getValidAccessToken(USER_ID, "spotify");
                }));
            }
            start.countDown();

            for (Future<String> result : results) {
                assertThat(result.get(5, TimeUnit.SECONDS)).isEqualTo("new-access-token");
            }
        } finally {
            executor.shutdownNow();
        }

        verify(client, times(1)).refreshAccessToken("refresh-token");
    }

    private void givenConnection(OAuthConnectionEntity connection) {
        when(connectionService.getConnectionByUserIdAndPlatform(USER_ID, MusicPlatform.SPOTIFY)).thenReturn(connection);
        when(connectionService.getConnectionById(CONNECTION_ID)).thenReturn(connection);
    }

    private OAuthConnectionEntity expiredConnection(String refreshToken) {
        OAuthConnectionEntity connection = new OAuthConnectionEntity();
        connection.setId(CONNECTION_ID);
        connection.setPlatform(MusicPlatform.SPOTIFY);
        connection.setAccessToken("old-access-token");
        connection.setRefreshToken(refreshToken);
        connection.setExpiresAt(Instant.now().minusSeconds(10));
        return connection;
    }
}
