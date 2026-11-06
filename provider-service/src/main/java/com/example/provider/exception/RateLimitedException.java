package com.example.provider.exception;

import com.example.common.ErrorCodes;
import com.example.common.MusicPlatform;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Duration;

@Getter
public class RateLimitedException extends ApiException {

    private final Duration retryAfter;

    public RateLimitedException(MusicPlatform platform, Duration retryAfter) {
        super(
                HttpStatus.TOO_MANY_REQUESTS,
                ErrorCodes.RATE_LIMITED,
                "Слишком много запросов к " + platform.displayName() + ", попробуйте позже"
        );
        this.retryAfter = retryAfter;
    }
}
