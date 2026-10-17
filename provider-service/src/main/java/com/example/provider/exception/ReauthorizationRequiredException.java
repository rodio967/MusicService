package com.example.provider.exception;

import com.example.common.ErrorCodes;
import com.example.common.MusicPlatform;
import org.springframework.http.HttpStatus;

public class ReauthorizationRequiredException extends ApiException {

    public ReauthorizationRequiredException(MusicPlatform platform) {
        super(
                HttpStatus.CONFLICT,
                ErrorCodes.REAUTHORIZATION_REQUIRED,
                "Нужно заново подключить аккаунт " + platform.displayName()
        );
    }
}
