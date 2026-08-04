package com.example.user.security;

import com.example.user.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtTokenProvider tokenProvider;
    private CustomUserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        tokenProvider = mock(JwtTokenProvider.class);
        userDetailsService = mock(CustomUserDetailsService.class);
        filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer jwt");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesActiveUserFromValidToken() throws Exception {
        UserDetails userDetails = activeUserDetails();
        when(tokenProvider.getUserIdIfTokenValid("jwt")).thenReturn(Optional.of(42L));
        when(userDetailsService.loadUserById(42L)).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isSameAs(userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doesNotAuthenticateDisabledUser() throws Exception {
        UserDetails userDetails = activeUserDetails();
        when(userDetails.isEnabled()).thenReturn(false);
        when(tokenProvider.getUserIdIfTokenValid("jwt")).thenReturn(Optional.of(42L));
        when(userDetailsService.loadUserById(42L)).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doesNotReplaceExistingAuthentication() throws Exception {
        UsernamePasswordAuthenticationToken existingAuthentication =
                new UsernamePasswordAuthenticationToken("existing-user", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isSameAs(existingAuthentication);
        verifyNoInteractions(tokenProvider, userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doesNotMaskDatabaseFailureAsMissingAuthentication() {
        when(tokenProvider.getUserIdIfTokenValid("jwt")).thenReturn(Optional.of(42L));
        when(userDetailsService.loadUserById(42L))
                .thenThrow(new DataAccessResourceFailureException("Database unavailable"));

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(DataAccessResourceFailureException.class);
        verifyNoInteractions(filterChain);
    }

    private UserDetails activeUserDetails() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(true);
        when(userDetails.isAccountNonExpired()).thenReturn(true);
        when(userDetails.isCredentialsNonExpired()).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(List.of());
        return userDetails;
    }
}
