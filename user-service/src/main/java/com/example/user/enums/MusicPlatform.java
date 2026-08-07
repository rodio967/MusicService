package com.example.user.enums;

import com.example.user.oauth.exception.UnknowPlatformException;

public enum MusicPlatform {
    SPOTIFY,
    YOUTUBE,
    YANDEX;

    public static MusicPlatform toPlatform(String value) {
        try {
            return MusicPlatform.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnknowPlatformException("Unknow Platform " + value);
        }
    }
}
