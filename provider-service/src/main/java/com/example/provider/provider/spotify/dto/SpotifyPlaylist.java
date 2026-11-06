package com.example.provider.provider.spotify.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyPlaylist(
        String id,
        String name,
        boolean collaborative,
        Owner owner,
        List<Image> images,
        @JsonAlias("tracks") ItemsInfo items
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Owner(String id) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(String url) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemsInfo(int total) {}
}
