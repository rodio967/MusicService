package com.example.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Имя пользователя обязательно")
        @Size(min = 1, max = 20, message = "Имя пользователя должно быть от 1 до 20 символов")
        String username,

        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный формат email")
        @Size(max = 50, message = "Слишком большой email")
        String email,

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 6, max = 20, message = "Пароль должен быть от 6 до 20")
        String password,

        @NotBlank(message = "Подтверждение пароля обязательно")
        @Size(min = 6, max = 20, message = "Подтверждение должно быть от 6 до 20")
        String confirmPassword
) {}
