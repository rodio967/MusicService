package com.example.user.internal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;


@Slf4j
@Component
public class InternalApiAuthenticationFilter extends OncePerRequestFilter {

    public static final String SECRET_HEADER = "X-Internal-Secret";
    public static final String INTERNAL_SERVICE_ROLE = "INTERNAL_SERVICE";

    private static final String INTERNAL_PATH_PREFIX = "/api/internal/";

    private final byte[] expectedSecret;

    public InternalApiAuthenticationFilter(InternalApiProperties properties) {
        this.expectedSecret = properties.secret().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String providedSecret = request.getHeader(SECRET_HEADER);

        if (providedSecret != null && isValidSecret(providedSecret)) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "internal-service",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + INTERNAL_SERVICE_ROLE))
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("Rejected internal API request to {}: missing or invalid {}", request.getRequestURI(), SECRET_HEADER);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidSecret(String providedSecret) {
        return MessageDigest.isEqual(expectedSecret, providedSecret.getBytes(StandardCharsets.UTF_8));
    }
}
