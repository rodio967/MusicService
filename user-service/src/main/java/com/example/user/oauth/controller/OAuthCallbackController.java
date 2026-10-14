package com.example.user.oauth.controller;

import com.example.user.oauth.exception.InvalidOAuthStateException;
import com.example.user.oauth.exception.OAuthAuthorizationException;
import com.example.user.oauth.exception.ProviderAccountException;
import com.example.user.oauth.exception.TokenExchangeException;
import com.example.user.oauth.exception.UnsupportedProviderException;
import com.example.user.oauth.facade.OAuthFacade;
import com.example.user.oauth.properties.OAuthFrontendProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@RequestMapping("/api/oauth/callback")
@RequiredArgsConstructor
@Slf4j
public class OAuthCallbackController {

    private final OAuthFacade facade;
    private final OAuthFrontendProperties frontendProperties;


    @GetMapping("/{platform}")
    public ResponseEntity<Void> callback(
            @PathVariable String platform,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error
    ) {

        facade.completeAuthorization(platform, state, code, error);

        return redirect(frontendRedirect()
                .queryParam("oauth_status", "success")
                .queryParam("provider", platform.toLowerCase()));
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

    @ExceptionHandler(UnsupportedProviderException.class)
    public ResponseEntity<Void> handleUnsupportedProvider(UnsupportedProviderException ex) {
        log.warn(ex.getMessage());
        return redirectWithError("unsupported_provider");
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleUnexpectedError(Exception ex) {
        log.error("Unexpected error in OAuth callback", ex);
        return redirectWithError("internal_error");
    }


    private ResponseEntity<Void> redirectWithError(String error) {
        return redirect(frontendRedirect()
                .queryParam("oauth_status", "error")
                .queryParam("error", error));
    }

    private UriComponentsBuilder frontendRedirect() {
        return UriComponentsBuilder.fromUri(frontendProperties.redirectUri());
    }

    private ResponseEntity<Void> redirect(UriComponentsBuilder builder) {
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(builder.encode().build().toUri())
                .build();
    }

}
