package com.example.provider.security;

import com.example.common.ApiErrorResponse;
import com.example.common.ErrorCodes;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
public class InternalSecretFilter extends OncePerRequestFilter {

    public static final String SECRET_HEADER = "X-Internal-Secret";

    private final byte[] expectedSecret;
    private final ObjectMapper objectMapper;

    public InternalSecretFilter(InternalApiProperties properties, ObjectMapper objectMapper) {
        this.expectedSecret = properties.secret().getBytes(StandardCharsets.UTF_8);
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String providedSecret = request.getHeader(SECRET_HEADER);
        if (isValidSecret(providedSecret)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Rejected request to {}: missing or invalid {}", request.getRequestURI(), SECRET_HEADER);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), new ApiErrorResponse(
                Instant.now(),
                HttpServletResponse.SC_UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED,
                "Требуется аутентификация",
                request.getRequestURI(),
                Map.of()
        ));

    }

    private boolean isValidSecret(String providedSecret) {
        return providedSecret != null
                && MessageDigest.isEqual(expectedSecret, providedSecret.getBytes(StandardCharsets.UTF_8));
    }
}
