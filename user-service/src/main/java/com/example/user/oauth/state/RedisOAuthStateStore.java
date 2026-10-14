package com.example.user.oauth.state;

import com.example.user.enums.MusicPlatform;
import com.example.user.oauth.dto.StateEntry;
import com.example.user.oauth.exception.InvalidOAuthStateException;
import com.example.user.oauth.properties.OAuthStateProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "oauth.state", name = "store", havingValue = "redis")
public class RedisOAuthStateStore implements OAuthStateStore {

    private static final String KEY_PREFIX = "oauth:state:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisOAuthStateStore(StringRedisTemplate redisTemplate,
                                ObjectMapper objectMapper,
                                OAuthStateProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = properties.ttl();

        log.info("Redis запущен успешно");
    }

    @Override
    public void save(String state, Long userId, MusicPlatform platform) {
        StateEntry entry = new StateEntry(userId, platform, Instant.now().plus(ttl));
        redisTemplate.opsForValue().set(KEY_PREFIX + state, toJson(entry), ttl);
    }

    @Override
    public StateEntry getAndRemove(String state) {
        String json = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + state);

        if (json == null) throw new InvalidOAuthStateException();
        StateEntry entry = fromJson(json);

        if (Instant.now().isAfter(entry.expiresAt())) throw new InvalidOAuthStateException();

        return entry;
    }

    private String toJson(StateEntry entry) {
        try {
            return objectMapper.writeValueAsString(entry);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Не удалось сериализовать OAuth state", ex);
        }
    }

    private StateEntry fromJson(String json) {
        try {
            return objectMapper.readValue(json, StateEntry.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Не удалось прочитать OAuth state из Redis", ex);
        }
    }
}
