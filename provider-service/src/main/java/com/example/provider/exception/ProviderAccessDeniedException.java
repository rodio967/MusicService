package com.example.provider.exception;

import com.example.common.ErrorCodes;
import com.example.common.MusicPlatform;
import org.springframework.http.HttpStatus;

public class ProviderAccessDeniedException extends ApiException {

    public ProviderAccessDeniedException(MusicPlatform platform) {
        super(
                HttpStatus.FORBIDDEN,
                ErrorCodes.PROVIDER_ACCESS_DENIED,
                platform.displayName() + " отклонил доступ к аккаунту"
        );
    }
}
