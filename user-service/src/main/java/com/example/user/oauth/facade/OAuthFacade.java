package com.example.user.oauth.facade;

import com.example.user.entity.User;
import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.client.OAuthProviderClient;
import com.example.user.oauth.connection.OAuthConnectionService;
import com.example.user.oauth.dto.OAuthTokenResponse;
import com.example.user.oauth.dto.ProviderAccountInfo;
import com.example.user.oauth.exception.InvalidOAuthStateException;
import com.example.user.oauth.exception.OAuthAuthorizationException;
import com.example.user.oauth.registry.OAuthClientRegistry;
import com.example.user.oauth.state.OAuthStateStore;
import com.example.user.service.UserService;
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
    private final UserService userService;


    public URI beginAuthorization(String platform, Long userId) {
        OAuthProviderClient client = clients.get(MusicPlatform.toPlatform(platform));
        String state = UUID.randomUUID().toString();
        store.save(state, userId);

        return client.buildAuthorizationUri(state);
    }


    public void completeAuthorization(String platform, String state, String code, String error) {
        if (state == null || state.isBlank()) throw new InvalidOAuthStateException();

        Long userId = store.getAndRemove(state);

        if (error != null && !error.isBlank()) {
            throw new OAuthAuthorizationException("Провайдер отклонил авторизацию");
        }
        if (code == null || code.isBlank()) {
            throw new OAuthAuthorizationException("Провайдер не вернул authorization code");
        }

        MusicPlatform musicPlatform = MusicPlatform.toPlatform(platform);
        User user = userService.findById(userId);

        OAuthProviderClient client = clients.get(musicPlatform);

        OAuthTokenResponse tokenResponse = client.exchangeCode(code);
        ProviderAccountInfo accountInfo = client.fetchProviderAccount(tokenResponse.accessToken());

        connectionService.saveOrUpdateConnection(user, musicPlatform, tokenResponse, accountInfo);
    }


}
