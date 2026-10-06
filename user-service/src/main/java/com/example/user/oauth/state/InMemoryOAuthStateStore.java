package com.example.user.oauth.state;

import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.dto.StateEntry;
import com.example.user.oauth.exception.InvalidOAuthStateException;
import com.example.user.oauth.properties.OAuthStateProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(prefix = "oauth.state", name = "store", havingValue = "memory", matchIfMissing = true)
public class InMemoryOAuthStateStore implements OAuthStateStore {

    private final ConcurrentHashMap<String, StateEntry> store = new ConcurrentHashMap<>();
    private final Duration ttl;

    public InMemoryOAuthStateStore(OAuthStateProperties properties) {
        this.ttl = properties.ttl();
    }

    @Override
    public void save(String state, Long userId, MusicPlatform platform) {
        store.put(state, new StateEntry(userId, platform, Instant.now().plus(ttl)));
    }

    @Override
    public StateEntry getAndRemove(String state) {
        StateEntry entry = store.remove(state);

        if (entry == null) throw new InvalidOAuthStateException();

        if (Instant.now().isAfter(entry.expiresAt())) throw new InvalidOAuthStateException();

        return entry;
    }

    @Scheduled(fixedDelay = 300_000)
    public void cleanup() {
        Instant now = Instant.now();
        store.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt()));
    }

}
