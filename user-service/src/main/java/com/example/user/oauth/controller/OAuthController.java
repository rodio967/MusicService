package com.example.user.oauth.controller;


import com.example.user.oauth.dto.AuthorizationUrlResponse;
import com.example.user.oauth.facade.OAuthFacade;
import com.example.user.oauth.exception.InvalidOAuthStateException;
import com.example.user.oauth.exception.OAuthAuthorizationException;
import com.example.user.oauth.exception.ProviderAccountException;
import com.example.user.oauth.exception.TokenExchangeException;
import com.example.user.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class OAuthController {

    private final OAuthFacade facade;

    @GetMapping("/{platform}/authorize")
    public ResponseEntity<AuthorizationUrlResponse> authorize(
            @PathVariable String platform,
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        URI authorizationUri = facade.beginAuthorization(platform, user.getUserId());

        return ResponseEntity.ok(new AuthorizationUrlResponse(authorizationUri.toString()));
    }

    @GetMapping("/{platform}/callback")
    public ResponseEntity<Void> callback(
            @PathVariable String platform,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error
    ) {

        facade.completeAuthorization(platform, state, code, error);

        URI redirect = URI.create("/dashboard?oauth_status=success&provider=" + platform);

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(redirect)
                .build();
    }

    @ExceptionHandler(OAuthAuthorizationException.class)
    public ResponseEntity<Void> handleAuthorizationError(OAuthAuthorizationException ex) {
        log.info(ex.getMessage());
        return redirectWithError("authorization_failed");
    }

    @ExceptionHandler(InvalidOAuthStateException.class)
    public ResponseEntity<Void> handleInvalidState(InvalidOAuthStateException ex) {
        log.warn(ex.getMessage());
        return redirectWithError("invalid_state");
    }

    @ExceptionHandler(TokenExchangeException.class)
    public ResponseEntity<Void> handleTokenExchangeError(TokenExchangeException ex) {
        log.error(ex.getMessage(), ex);
        return redirectWithError("token_exchange_failed");
    }

    @ExceptionHandler(ProviderAccountException.class)
    public ResponseEntity<Void> handleProviderAccountError(ProviderAccountException ex) {
        log.error(ex.getMessage(), ex);
        return redirectWithError("provider_account_failed");
    }

    private ResponseEntity<Void> redirectWithError(String error) {
        URI redirect = URI.create("/dashboard?oauth_status=error&error=" + error);

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(redirect)
                .build();
    }

}
