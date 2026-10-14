package com.example.user.oauth.exception;

public class UnsupportedProviderException extends RuntimeException {

    public UnsupportedProviderException(String provider) {
        super("Музыкальный провайдер не поддерживается: " + provider);
    }
}
