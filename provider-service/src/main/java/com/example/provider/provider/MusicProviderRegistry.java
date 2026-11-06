package com.example.provider.provider;

import com.example.common.MusicPlatform;
import com.example.provider.exception.UnsupportedPlatformException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class MusicProviderRegistry {

    private final Map<MusicPlatform, MusicProvider> providers;

    public MusicProviderRegistry(List<MusicProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(MusicProvider::getPlatform, Function.identity()));
    }

    public MusicProvider get(MusicPlatform platform) {
        MusicProvider provider = providers.get(platform);

        if (provider == null) {
            throw new UnsupportedPlatformException(platform.displayName());
        }

        return provider;
    }
}
