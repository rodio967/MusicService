package com.example.user.oauth.exception;

import com.example.user.enums.MusicPlatform;

public class ReauthorizationRequiredException extends RuntimeException {

    public ReauthorizationRequiredException(MusicPlatform platform) {
        super("Для " + platform + " требуется повторная авторизация");
    }
}
