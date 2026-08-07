package com.example.user.oauth.registry;

import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.client.OAuthProviderClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuthClientRegistry {

    private final Map<MusicPlatform, OAuthProviderClient> clients;

    public OAuthClientRegistry(List<OAuthProviderClient> clientList) {
        this.clients = clientList.stream()
                .collect(Collectors.toMap(
                        OAuthProviderClient::getPlatform,
                        Function.identity()
                ));

    }

    public OAuthProviderClient get(MusicPlatform platform) {
        OAuthProviderClient client = clients.get(platform);

        if (client == null) {
            throw new RuntimeException();
        }

        return client;
    }
}
