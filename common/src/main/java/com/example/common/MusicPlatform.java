package com.example.common;

import java.util.Locale;
import java.util.Optional;

public enum MusicPlatform {
    SPOTIFY("Spotify"),
    YOUTUBE("YouTube"),
    YANDEX("Яндекс Музыка");

    private final String displayName;

    MusicPlatform(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public String pathValue() {
        return name().toLowerCase(Locale.ROOT);
    }


    public static Optional<MusicPlatform> toPlatform(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(MusicPlatform.valueOf(value.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
