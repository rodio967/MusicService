package com.example.provider.exception;

import com.example.common.ErrorCodes;
import com.example.common.MusicPlatform;
import org.springframework.http.HttpStatus;

public class ProviderUnavailableException extends ApiException {

    public ProviderUnavailableException(MusicPlatform platform) {
        super(
                HttpStatus.BAD_GATEWAY,
                ErrorCodes.PROVIDER_UNAVAILABLE,
                platform.displayName() + " временно недоступен, попробуйте позже"
        );
    }

    public ProviderUnavailableException(MusicPlatform platform, Throwable cause) {
        super(
                HttpStatus.BAD_GATEWAY,
                ErrorCodes.PROVIDER_UNAVAILABLE,
                platform.displayName() + " временно недоступен, попробуйте позже",
                cause
        );
    }
}
