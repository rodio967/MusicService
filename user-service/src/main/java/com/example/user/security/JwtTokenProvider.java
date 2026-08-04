package com.example.user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long jwtExpirationMs;
    private final JwtParser jwtParser;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String encodedSecret,
            @Value("${jwt.expiration-ms}") long jwtExpirationMs
    ) {
        if (!StringUtils.hasText(encodedSecret)) {
            throw new IllegalArgumentException("JWT secret должен быть не пустым");
        }
        if (jwtExpirationMs <= 0) {
            throw new IllegalArgumentException("JWT expiration должен быть положительным");
        }

        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret));
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .build();
    }

    public String generateToken(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new IllegalArgumentException("Unsupported authentication principal");
        }

        return generateTokenFromUserId(userDetails.getUserId());
    }

    private String generateTokenFromUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id must not be null");
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public Optional<Long> getUserIdIfTokenValid(String token) {
        try {
            Claims claims = jwtParser
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.ofNullable(claims.getSubject())
                    .filter(StringUtils::hasText)
                    .map(Long::valueOf);
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Invalid JWT token");
        }

        return Optional.empty();
    }
}
