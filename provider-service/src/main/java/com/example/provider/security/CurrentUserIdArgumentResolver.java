package com.example.provider.security;

import com.example.provider.exception.InvalidUserIdHeaderException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Long resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        String header = webRequest.getHeader(USER_ID_HEADER);
        if (header == null || header.isBlank()) {
            throw new InvalidUserIdHeaderException();
        }

        try {
            long userId = Long.parseLong(header.trim());
            if (userId <= 0) {
                throw new InvalidUserIdHeaderException();
            }

            return userId;
        } catch (NumberFormatException e) {
            throw new InvalidUserIdHeaderException();
        }
    }
}
