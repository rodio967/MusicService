package com.example.provider.service;

import com.example.common.MusicPlatform;
import com.example.provider.exception.UnsupportedPlatformException;
import com.example.provider.model.CursorPage;
import com.example.provider.model.PlatformAccess;
import com.example.provider.model.PlaylistSummary;
import com.example.provider.provider.MusicProvider;
import com.example.provider.provider.MusicProviderRegistry;
import com.example.provider.usertoken.UserServiceTokenClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProviderFacadeTest {

    private final MusicProvider spotify = mock(MusicProvider.class);
    private final UserServiceTokenClient tokenClient = mock(UserServiceTokenClient.class);

    private ProviderFacade facade;

    @BeforeEach
    void setUp() {
        when(spotify.getPlatform()).thenReturn(MusicPlatform.SPOTIFY);
        facade = new ProviderFacade(new MusicProviderRegistry(List.of(spotify)), tokenClient);
    }

    @Test
    void passesUserTokenAndCursorToProvider() {
        PlatformAccess access = new PlatformAccess("token", "me");
        CursorPage<PlaylistSummary> page = new CursorPage<>(List.of(), "40");
        when(tokenClient.getPlatformAccess(42L, MusicPlatform.SPOTIFY)).thenReturn(access);
        when(spotify.getPlaylists(access, "20", 20)).thenReturn(page);

        assertThat(facade.fetchPlaylists(42L, "spotify", "20", 20)).isSameAs(page);
    }

    @Test
    void unknownPlatformFailsWithoutCallingUserService() {
        assertThatThrownBy(() -> facade.fetchPlaylists(42L, "deezer", null, 20))
                .isInstanceOf(UnsupportedPlatformException.class);
        verifyNoInteractions(tokenClient);
    }

    @Test
    void platformWithoutProviderFailsWithoutCallingUserService() {
        assertThatThrownBy(() -> facade.fetchPlaylists(42L, "yandex", null, 20))
                .isInstanceOf(UnsupportedPlatformException.class);
        verifyNoInteractions(tokenClient);
    }
}
