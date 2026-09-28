package com.example.user.oauth.facade;

import com.example.user.entity.User;
import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.client.OAuthProviderClient;
import com.example.user.oauth.connection.OAuthConnectionEntity;
import com.example.user.oauth.connection.OAuthConnectionService;
import com.example.user.oauth.dto.OAuthTokenResponse;
import com.example.user.oauth.dto.ProviderAccountInfo;
import com.example.user.oauth.dto.StateEntry;
import com.example.user.oauth.exception.InvalidOAuthStateException;
import com.example.user.oauth.exception.OAuthAuthorizationException;
import com.example.user.oauth.exception.ReauthorizationRequiredException;
import com.example.user.oauth.registry.OAuthClientRegistry;
import com.example.user.oauth.state.OAuthStateStore;
import com.example.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
public class OAuthFacade {

    private final OAuthClientRegistry clients;
    private final OAuthConnectionService connectionService;
    private final OAuthStateStore store;
    private final UserService userService;

    private final ConcurrentHashMap<Long, ReentrantLock> refreshLocks = new ConcurrentHashMap<>();


    public URI beginAuthorization(String platform, Long userId) {
        MusicPlatform musicPlatform = MusicPlatform.toPlatform(platform);
        OAuthProviderClient client = clients.get(musicPlatform);
        String state = UUID.randomUUID().toString();
        store.save(state, userId, musicPlatform);

        return client.buildAuthorizationUri(state);
    }


    public void completeAuthorization(String platform, String state, String code, String error) {
        if (state == null || state.isBlank()) throw new InvalidOAuthStateException();

        MusicPlatform musicPlatform = MusicPlatform.toPlatform(platform);
        StateEntry stateEntry = store.getAndRemove(state);

        if (stateEntry.platform() != musicPlatform) throw new InvalidOAuthStateException();

        if (error != null && !error.isBlank()) {
            throw new OAuthAuthorizationException("Провайдер отклонил авторизацию");
        }
        if (code == null || code.isBlank()) {
            throw new OAuthAuthorizationException("Провайдер не вернул authorization code");
        }

        User user = userService.findById(stateEntry.userId());

        OAuthProviderClient client = clients.get(musicPlatform);

        OAuthTokenResponse tokenResponse = client.exchangeCode(code);
        ProviderAccountInfo accountInfo = client.fetchProviderAccount(tokenResponse.accessToken());

        connectionService.saveOrUpdateConnection(user, musicPlatform, tokenResponse, accountInfo);
    }


    public String getValidAccessToken(Long userId, String platform) {
        MusicPlatform musicPlatform = MusicPlatform.toPlatform(platform);
        OAuthConnectionEntity entity = connectionService.getConnectionByUserIdAndPlatform(userId, musicPlatform);

        if (entity.isReauthorizationRequired()) throw new ReauthorizationRequiredException(musicPlatform);

        if (entity.isTokenValid()) {
            return entity.getAccessToken();
        }

        ReentrantLock lock = refreshLocks.computeIfAbsent(entity.getId(), id -> new ReentrantLock());
        lock.lock();
        try {
            return refreshAccessToken(entity.getId(), musicPlatform);
        } finally {
            lock.unlock();
        }
    }


    private String refreshAccessToken(Long connectionId, MusicPlatform musicPlatform) {
        OAuthConnectionEntity entity = connectionService.getConnectionById(connectionId);

        if (entity.isReauthorizationRequired()) throw new ReauthorizationRequiredException(musicPlatform);

        if (entity.isTokenValid()) {
            return entity.getAccessToken();
        }

        OAuthProviderClient client = clients.get(musicPlatform);

        if (entity.getRefreshToken() == null || entity.getRefreshToken().isBlank()) {
            connectionService.markReauthorizationRequired(entity.getId());
            throw new ReauthorizationRequiredException(musicPlatform);
        }

        OAuthTokenResponse tokenResponse;
        try {
            tokenResponse = client.refreshAccessToken(entity.getRefreshToken());
        } catch (ReauthorizationRequiredException ex) {
            connectionService.markReauthorizationRequired(entity.getId());
            throw ex;
        }

        connectionService.updateTokens(entity.getId(), tokenResponse);

        return tokenResponse.accessToken();
    }


}
