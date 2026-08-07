package com.example.user.service;

import com.example.user.dto.request.LoginRequest;
import com.example.user.dto.request.RegisterRequest;
import com.example.user.dto.response.LoginResponse;
import com.example.user.dto.response.UserInfoResponse;
import com.example.user.entity.User;
import com.example.user.enums.Role;
import com.example.user.exception.EmailAlreadyExistsException;
import com.example.user.exception.PasswordMismatchException;
import com.example.user.exception.UserNotFoundException;
import com.example.user.exception.UsernameAlreadyExistsException;
import com.example.user.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import com.example.user.mapper.UserMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.user.repository.UserRepository;
import com.example.user.security.JwtTokenProvider;

import java.util.List;
import java.util.Objects;

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
        user.getRoles().add(Role.USER);

        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    public User findByUsernameWithRoles(String username) {
        return userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
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

        User user = findByUsernameWithRoles(authentication.getName());

        return new LoginResponse(
                jwt,
                "bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles()
        );
    }

    public UserInfoResponse getUserInfo(CustomUserDetails principal) {
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new UserInfoResponse(
                principal.getUserId(),
                principal.getUsername(),
                principal.getEmail(),
                roles
        );
    }
}
