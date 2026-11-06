package com.example.provider.service;

import com.example.common.MusicPlatform;
import com.example.provider.exception.UnsupportedPlatformException;
import com.example.provider.model.CursorPage;
import com.example.provider.model.PlatformAccess;
import com.example.provider.model.PlaylistSummary;
import com.example.provider.provider.MusicProvider;
import com.example.provider.provider.MusicProviderRegistry;
import com.example.provider.usertoken.UserServiceTokenClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderFacade {

    private final MusicProviderRegistry registry;
    private final UserServiceTokenClient tokenClient;

    public CursorPage<PlaylistSummary> fetchPlaylists(Long userId, String platform, String cursor, int limit) {
        MusicPlatform musicPlatform = MusicPlatform.toPlatform(platform)
                .orElseThrow(() -> new UnsupportedPlatformException(platform));

        MusicProvider provider = registry.get(musicPlatform);
        PlatformAccess access = tokenClient.getPlatformAccess(userId, musicPlatform);

        CursorPage<PlaylistSummary> page = provider.getPlaylists(access, cursor, limit);
        return page;
    }
}
