package com.example.user.oauth.exception;

public class ProviderAccountException extends RuntimeException {

    public ProviderAccountException(String message) {
        super(message);
    }

    public ProviderAccountException(String message, Throwable cause) {
        super(message, cause);
    }
}
