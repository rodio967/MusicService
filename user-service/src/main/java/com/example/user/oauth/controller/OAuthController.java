package com.example.user.oauth.controller;


import com.example.user.oauth.dto.AuthorizationUrlResponse;
import com.example.user.oauth.facade.OAuthFacade;
import com.example.user.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
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

}
