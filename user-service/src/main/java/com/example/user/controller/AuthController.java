package com.example.user.controller;

import com.example.user.dto.request.LoginRequest;
import com.example.user.dto.request.RegisterRequest;
import com.example.user.dto.response.LoginResponse;
import com.example.user.dto.response.RegisterResponse;
import com.example.user.dto.response.UserInfoResponse;
import com.example.user.entity.User;
import com.example.user.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.user.service.UserService;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {

        User user = userService.registerNewUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(
                        "Регистрация успешна. Теперь вы можете войти.",
                        user.getId(),
                        user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getUserInfo(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(userService.getUserInfo(principal));
    }

}
