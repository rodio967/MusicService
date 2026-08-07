package com.example.user.oauth.facade;

import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.client.OAuthProviderClient;
import com.example.user.oauth.connection.OAuthConnectionService;
import com.example.user.oauth.registry.OAuthClientRegistry;
import com.example.user.oauth.state.OAuthStateStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuthFacade {

    private final OAuthClientRegistry clients;
    private final OAuthConnectionService connectionService;
    private final OAuthStateStore store;


    public URI beginAuthorization(String platform, Long userId) {
        OAuthProviderClient client = clients.get(MusicPlatform.toPlatform(platform));
        String state = UUID.randomUUID().toString();
        store.save(state, userId);

        return client.buildAuthorizationUri(state);
    }


}
