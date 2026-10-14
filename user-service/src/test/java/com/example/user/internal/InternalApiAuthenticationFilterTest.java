package com.example.user.internal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class InternalApiAuthenticationFilterTest {

    private static final String INTERNAL_URI = "/api/internal/users/1/connections/spotify/access-token";

    private final InternalApiAuthenticationFilter filter =
            new InternalApiAuthenticationFilter(new InternalApiProperties("correct-secret"));

    @BeforeEach
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesInternalServiceWithCorrectSecret() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request(INTERNAL_URI, "correct-secret"), new MockHttpServletResponse(), chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_INTERNAL_SERVICE");
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void doesNotAuthenticateWithWrongSecret() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request(INTERNAL_URI, "wrong-secret"), new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void doesNotAuthenticateWithoutHeader() throws Exception {
        filter.doFilter(request(INTERNAL_URI, null), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void ignoresSecretOutsideInternalApi() throws Exception {
        filter.doFilter(request("/api/auth/me", "correct-secret"), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private static MockHttpServletRequest request(String uri, String secret) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        if (secret != null) {
            request.addHeader(InternalApiAuthenticationFilter.SECRET_HEADER, secret);
        }
        return request;
    }
}
