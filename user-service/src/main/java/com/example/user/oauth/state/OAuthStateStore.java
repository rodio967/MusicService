package com.example.user.oauth.state;

import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.dto.StateEntry;

public interface OAuthStateStore {

    void save(String state, Long userId, MusicPlatform platform);

    StateEntry getAndRemove(String state);
}
