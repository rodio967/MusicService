package com.example.provider.provider.spotify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyPlaylistPage(List<SpotifyPlaylist> items, String next) {}
