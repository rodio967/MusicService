package com.example.provider.provider;

import com.example.common.MusicPlatform;
import com.example.provider.model.CursorPage;
import com.example.provider.model.PlatformAccess;
import com.example.provider.model.PlaylistSummary;


public interface MusicProvider {

    MusicPlatform getPlatform();

    CursorPage<PlaylistSummary> getPlaylists(PlatformAccess access, String cursor, int limit);
}
