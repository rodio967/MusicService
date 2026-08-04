package com.example.user.security;

import com.example.user.entity.User;
import com.example.user.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)
    );

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, 60_000);
    }

    @Test
    void generatesTokenWithStableUserIdAsSubject() {
        CustomUserDetails principal = userDetails(42L, true);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        String token = tokenProvider.generateToken(authentication);

        assertThat(tokenProvider.getUserIdIfTokenValid(token)).contains(42L);
    }

    @Test
    void rejectsTokenWithModifiedSignature() {
        CustomUserDetails principal = userDetails(42L, true);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
        String token = tokenProvider.generateToken(authentication);
        char replacement = token.endsWith("a") ? 'b' : 'a';
        String modifiedToken = token.substring(0, token.length() - 1) + replacement;

        assertThat(tokenProvider.getUserIdIfTokenValid(modifiedToken)).isEmpty();
    }

    @Test
    void rejectsMalformedToken() {
        assertThat(tokenProvider.getUserIdIfTokenValid("not-a-jwt")).isEmpty();
    }

    @Test
    void rejectsExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject("42")
                .expiration(Date.from(Instant.now().minusSeconds(1)))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET)))
                .compact();

        assertThat(tokenProvider.getUserIdIfTokenValid(expiredToken)).isEmpty();
    }

    @Test
    void rejectsKeyShorterThanRequiredForHs256() {
        String shortSecret = Base64.getEncoder().encodeToString(
                "too-short".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> new JwtTokenProvider(shortSecret, 60_000))
                .isInstanceOf(WeakKeyException.class);
    }

    private CustomUserDetails userDetails(Long id, boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setUsername("username");
        user.setEmail("user@example.com");
        user.setPassword("encoded-password");
        user.setEnabled(enabled);
        user.setRoles(Set.of(Role.USER));
        return CustomUserDetails.build(user);
    }
}
