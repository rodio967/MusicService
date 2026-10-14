package com.example.user.oauth.state;

import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.dto.StateEntry;
import com.example.user.oauth.exception.InvalidOAuthStateException;
import com.example.user.oauth.properties.OAuthStateProperties;
import com.example.user.oauth.properties.OAuthStateProperties.StoreType;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryOAuthStateStoreTest {

    @Test
    void returnsSavedEntryOnlyOnce() {
        InMemoryOAuthStateStore store = storeWithTtl(Duration.ofMinutes(10));
        store.save("state", 42L, MusicPlatform.SPOTIFY);

        StateEntry entry = store.getAndRemove("state");

        assertThat(entry.userId()).isEqualTo(42L);
        assertThat(entry.platform()).isEqualTo(MusicPlatform.SPOTIFY);
        assertThatThrownBy(() -> store.getAndRemove("state"))
                .isInstanceOf(InvalidOAuthStateException.class);
    }

    @Test
    void rejectsUnknownState() {
        assertThatThrownBy(() -> storeWithTtl(Duration.ofMinutes(10)).getAndRemove("unknown"))
                .isInstanceOf(InvalidOAuthStateException.class);
    }

    @Test
    void rejectsExpiredState() {
        InMemoryOAuthStateStore store = storeWithTtl(Duration.ofMillis(-1));
        store.save("state", 42L, MusicPlatform.SPOTIFY);

        assertThatThrownBy(() -> store.getAndRemove("state"))
                .isInstanceOf(InvalidOAuthStateException.class);
    }

    private static InMemoryOAuthStateStore storeWithTtl(Duration ttl) {
        return new InMemoryOAuthStateStore(new OAuthStateProperties(StoreType.MEMORY, ttl));
    }
}
