package com.example.provider.exception;

import com.example.common.ErrorCodes;
import org.springframework.http.HttpStatus;

public class InvalidCursorException extends ApiException {

    public InvalidCursorException() {
        super(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_CURSOR, "Некорректный курсор страницы");
    }
}
