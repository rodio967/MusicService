package com.example.user.oauth.exception;

public class InvalidOAuthStateException extends RuntimeException {

    public InvalidOAuthStateException() {
        super("OAuth state недействителен или истёк");
    }
}
