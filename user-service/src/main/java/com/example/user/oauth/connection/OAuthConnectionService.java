package com.example.user.oauth.connection;


import com.example.user.entity.User;
import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.dto.OAuthTokenResponse;
import com.example.user.oauth.dto.ProviderAccountInfo;
import com.example.user.oauth.exception.ConnectionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OAuthConnectionService {

    private final OAuthConnectionRepository connectionRepository;


    @Transactional
    public OAuthConnectionEntity saveOrUpdateConnection(
            User user,
            MusicPlatform platform,
            OAuthTokenResponse tokenResponse,
            ProviderAccountInfo accountInfo) {

        OAuthConnectionEntity entity = connectionRepository.findByUserIdAndPlatform(user.getId(), platform)
                .orElseGet(() -> {
                    OAuthConnectionEntity newEntity = new OAuthConnectionEntity();
                    newEntity.setUser(user);
                    newEntity.setPlatform(platform);

                    return newEntity;
                });


        entity.setProviderAccountId(accountInfo.id());
        entity.setAccessToken(tokenResponse.accessToken());
        entity.setExpiresAt(Instant.now().plusSeconds(tokenResponse.expiresIn()));
        if (tokenResponse.scopes() != null && !tokenResponse.scopes().isBlank()) {
            entity.setScopes(tokenResponse.scopes());
        }

        if (tokenResponse.refreshToken() != null && !tokenResponse.refreshToken().isBlank()) {
            entity.setRefreshToken(tokenResponse.refreshToken());
        }


        return connectionRepository.save(entity);
    }

    @Transactional
    public OAuthConnectionEntity updateTokens(Long connectionId, OAuthTokenResponse tokenResponse) {
        OAuthConnectionEntity entity = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ConnectionNotFoundException(
                        "OAuth-подключение " + connectionId + " не найдено"
                ));

        entity.setAccessToken(tokenResponse.accessToken());
        entity.setExpiresAt(Instant.now().plusSeconds(tokenResponse.expiresIn()));
        if (tokenResponse.scopes() != null && !tokenResponse.scopes().isBlank()) {
            entity.setScopes(tokenResponse.scopes());
        }

        if (tokenResponse.refreshToken() != null && !tokenResponse.refreshToken().isBlank()) {
            entity.setRefreshToken(tokenResponse.refreshToken());
        }

        return connectionRepository.save(entity);
    }


    public OAuthConnectionEntity getConnection(Long userId, MusicPlatform platform) {
        return connectionRepository.findByUserIdAndPlatform(userId, platform)
                .orElseThrow(() -> new ConnectionNotFoundException(
                        "OAuth-подключение пользователя " + userId + " к " + platform + " не найдено"
                ));
    }

}
