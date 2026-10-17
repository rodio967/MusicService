package com.example.provider.exception;

import com.example.common.ErrorCodes;
import com.example.provider.security.CurrentUserIdArgumentResolver;
import org.springframework.http.HttpStatus;


public class InvalidUserIdHeaderException extends ApiException {

    public InvalidUserIdHeaderException() {
        super(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.INVALID_REQUEST,
                "Не передан или некорректен заголовок " + CurrentUserIdArgumentResolver.USER_ID_HEADER
        );
    }
}
