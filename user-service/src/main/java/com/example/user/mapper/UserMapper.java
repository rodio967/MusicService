package com.example.user.mapper;

import com.example.user.dto.request.RegisterRequest;
import com.example.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());

        return user;
    }
}
