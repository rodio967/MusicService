package com.example.user.internal;

import com.example.user.internal.dto.AccessTokenResponse;
import com.example.user.oauth.facade.OAuthFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalTokenController {

    private final OAuthFacade facade;

    @GetMapping("/users/{userId}/connections/{platform}/access-token")
    public ResponseEntity<AccessTokenResponse> getAccessToken(
            @PathVariable Long userId,
            @PathVariable String platform
    ) {
        String accessToken = facade.getValidAccessToken(userId, platform);

        return ResponseEntity.ok(new AccessTokenResponse(accessToken));
    }
}
