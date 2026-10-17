package com.example.provider.exception;

import com.example.common.ErrorCodes;
import org.springframework.http.HttpStatus;


public class UserServiceUnavailableException extends ApiException {

    public UserServiceUnavailableException(Throwable cause) {
        super(
                HttpStatus.BAD_GATEWAY,
                ErrorCodes.USER_SERVICE_UNAVAILABLE,
                "Сервис временно недоступен, попробуйте позже",
                cause
        );
    }
}
