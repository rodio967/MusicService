package com.example.user.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Пользователь с " + id + " не найден");
    }

    public UserNotFoundException(String username) {
        super("Пользователь с " + username + " не найден");
    }
}
