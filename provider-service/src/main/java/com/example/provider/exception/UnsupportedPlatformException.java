package com.example.provider.exception;

import com.example.common.ErrorCodes;
import org.springframework.http.HttpStatus;

public class UnsupportedPlatformException extends ApiException {

    public UnsupportedPlatformException(String platform) {
        super(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.UNSUPPORTED_PLATFORM,
                "Платформа (" + platform + ") не поддерживается"
        );
    }
}
