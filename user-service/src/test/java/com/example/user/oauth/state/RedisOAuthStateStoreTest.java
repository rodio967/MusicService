package com.example.user.oauth.state;

import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.dto.StateEntry;
import com.example.user.oauth.exception.InvalidOAuthStateException;
import com.example.user.oauth.properties.OAuthStateProperties;
import com.example.user.oauth.properties.OAuthStateProperties.StoreType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RedisOAuthStateStoreTest {

    private static final Duration TTL = Duration.ofMinutes(10);

    private ValueOperations<String, String> valueOperations;
    private RedisOAuthStateStore store;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        store = new RedisOAuthStateStore(
                redisTemplate,
                objectMapper,
                new OAuthStateProperties(StoreType.REDIS, TTL)
        );
    }

    @Test
    void savesStateWithTtlAndReadsItBackAtomically() {
        store.save("abc", 42L, MusicPlatform.YOUTUBE);

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq("oauth:state:abc"), json.capture(), eq(TTL));

        when(valueOperations.getAndDelete("oauth:state:abc")).thenReturn(json.getValue());
        StateEntry entry = store.getAndRemove("abc");

        assertThat(entry.userId()).isEqualTo(42L);
        assertThat(entry.platform()).isEqualTo(MusicPlatform.YOUTUBE);
    }

    @Test
    void rejectsMissingState() {
        when(valueOperations.getAndDelete("oauth:state:unknown")).thenReturn(null);

        assertThatThrownBy(() -> store.getAndRemove("unknown"))
                .isInstanceOf(InvalidOAuthStateException.class);
    }
}
