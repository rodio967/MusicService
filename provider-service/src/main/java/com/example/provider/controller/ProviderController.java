package com.example.provider.controller;

import com.example.provider.model.CursorPage;
import com.example.provider.model.PlaylistSummary;
import com.example.provider.security.CurrentUserId;
import com.example.provider.service.ProviderFacade;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/providers")
public class ProviderController {

    private final ProviderFacade facade;

    @GetMapping("/{platform}/playlists")
    public ResponseEntity<CursorPage<PlaylistSummary>> getPlaylists(
            @PathVariable String platform,
            @CurrentUserId Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit
    ) {
        return ResponseEntity.ok(facade.fetchPlaylists(userId, platform, cursor, limit));
    }


}
