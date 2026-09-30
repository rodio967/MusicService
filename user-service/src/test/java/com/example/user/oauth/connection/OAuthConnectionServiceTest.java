package com.example.user.oauth.connection;

import com.example.user.entity.User;
import com.example.user.enums.ConnectionStatus;
import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.dto.OAuthTokenResponse;
import com.example.user.oauth.dto.ProviderAccountInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OAuthConnectionServiceTest {

    private OAuthConnectionRepository repository;
    private OAuthConnectionService service;

    @BeforeEach
    void setUp() {
        repository = mock(OAuthConnectionRepository.class);
        service = new OAuthConnectionService(repository);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void reconnectReactivatesConnectionThatRequiredReauthorization() {
        User user = new User();
        user.setId(42L);

        OAuthConnectionEntity existing = new OAuthConnectionEntity();
        existing.setId(7L);
        existing.setStatus(ConnectionStatus.REAUTHORIZATION_REQUIRED);
        when(repository.findByUserIdAndPlatform(42L, MusicPlatform.SPOTIFY)).thenReturn(Optional.of(existing));

        OAuthConnectionEntity saved = service.saveOrUpdateConnection(
                user,
                MusicPlatform.SPOTIFY,
                new OAuthTokenResponse("access", "Bearer", 3600L, "refresh", null),
                new ProviderAccountInfo("spotify-user", "Name")
        );

        assertThat(saved.getStatus()).isEqualTo(ConnectionStatus.ACTIVE);
        assertThat(saved.getRefreshToken()).isEqualTo("refresh");
    }

    @Test
    void markReauthorizationRequiredChangesStatus() {
        OAuthConnectionEntity existing = new OAuthConnectionEntity();
        existing.setId(7L);
        when(repository.findById(7L)).thenReturn(Optional.of(existing));

        service.markReauthorizationRequired(7L);

        assertThat(existing.getStatus()).isEqualTo(ConnectionStatus.REAUTHORIZATION_REQUIRED);
    }
}
