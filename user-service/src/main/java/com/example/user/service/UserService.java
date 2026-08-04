package com.example.user.service;

import com.example.user.dto.request.LoginRequest;
import com.example.user.dto.request.RegisterRequest;
import com.example.user.dto.response.LoginResponse;
import com.example.user.entity.User;
import com.example.user.enums.Role;
import com.example.user.exception.EmailAlreadyExistsException;
import com.example.user.exception.PasswordMismatchException;
import com.example.user.exception.UserNotFoundException;
import com.example.user.exception.UsernameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import com.example.user.mapper.UserMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.user.repository.UserRepository;
import com.example.user.security.JwtTokenProvider;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public User registerNewUser(RegisterRequest request) {
        if (!Objects.equals(request.password(), request.confirmPassword())) throw new PasswordMismatchException("Пароли не совпадают");

        if (userRepository.existsByUsername(request.username())) throw new UsernameAlreadyExistsException("Имя пользователя уже занято");

        if (userRepository.existsByEmail(request.email())) throw new EmailAlreadyExistsException("Email уже занят");

        User user = mapper.toEntity(request);
        String encodedPassword = passwordEncoder.encode(request.password());
        user.setPassword(encodedPassword);
        user.setRoles(Set.of(Role.USER));

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByUsernameWithRoles(String username) {
        return userRepository.findByUsernameWithRoles(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);

        User user = findByUsernameWithRoles(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(authentication.getName()));

        return new LoginResponse(
                jwt,
                "bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles()
        );
    }
}
