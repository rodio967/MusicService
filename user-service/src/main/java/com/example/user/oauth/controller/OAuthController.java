package com.example.user.oauth.controller;


import com.example.user.oauth.facade.OAuthFacade;
import com.example.user.security.CustomUserDetails;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
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
public class OAuthController {

    private final OAuthFacade facade;

    @GetMapping("/{platform}/authorize")
    public ResponseEntity<Void> authorize(
            @PathVariable String platform,
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        /*
        1) Нужна платформа, к которой подключаемся
        2) По платформе находим HTTP Client для сервиса (Spotify, Youtube)
        3) build URL для запроса на сервер
        4) сохраняем состояние/state для последующей аутентификации
        5) делаем push
        * */
        URI authorizationUri = facade.beginAuthorization(platform, user.getUserId());

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(authorizationUri)
                .build();
    }

    @GetMapping("/{platform}/callback")
    public ResponseEntity<Void> callback(
            @PathVariable String platform,
            @RequestParam @NotBlank String state,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error
    ) {

        facade.completeAuthorization(platform, state, code, error);

        URI redirect = URI.create("/dashboard?success=" + platform + "_connected");

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(redirect)
                .build();
    }

}
