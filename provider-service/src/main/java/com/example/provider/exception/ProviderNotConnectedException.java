package com.example.provider.exception;

import com.example.common.ErrorCodes;
import com.example.common.MusicPlatform;
import org.springframework.http.HttpStatus;

public class ProviderNotConnectedException extends ApiException {

    public ProviderNotConnectedException(MusicPlatform platform) {
        super(
                HttpStatus.NOT_FOUND,
                ErrorCodes.PROVIDER_NOT_CONNECTED,
                "Аккаунт " + platform.displayName() + " не подключён"
        );
    }
}
