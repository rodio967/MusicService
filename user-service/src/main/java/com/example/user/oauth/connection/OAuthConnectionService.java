package com.example.user.oauth.connection;


import com.example.user.entity.User;
import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.dto.OAuthTokenResponse;
import com.example.user.oauth.dto.ProviderAccountInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OAuthConnectionService {

    private final OAuthConnectionRepository connectionRepository;


    public OAuthConnectionEntity saveToken(User user, MusicPlatform platform,
                                           OAuthTokenResponse tokenResponse,
                                           ProviderAccountInfo accountInfo) {

        OAuthConnectionEntity entity = new OAuthConnectionEntity();
        entity.setUser(user);
        entity.setPlatform(platform);
        entity.setProviderAccountId(accountInfo.id());
        entity.setAccessToken(tokenResponse.accessToken());
        entity.setRefreshToken(tokenResponse.refreshToken());
        entity.setExpiresAt(Instant.now().plusSeconds(tokenResponse.expiresIn()));
        entity.setScopes(tokenResponse.scopes());


        return connectionRepository.save(entity);
    }

}
