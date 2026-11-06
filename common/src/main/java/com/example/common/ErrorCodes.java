package com.example.common;


public final class ErrorCodes {

    // Общие
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String MALFORMED_JSON = "MALFORMED_JSON";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    // user-service, internal API токенов
    public static final String OAUTH_CONNECTION_NOT_FOUND = "OAUTH_CONNECTION_NOT_FOUND";
    public static final String REAUTHORIZATION_REQUIRED = "REAUTHORIZATION_REQUIRED";
    public static final String TOKEN_EXCHANGE_FAILED = "TOKEN_EXCHANGE_FAILED";

    // provider-service
    public static final String UNSUPPORTED_PLATFORM = "UNSUPPORTED_PLATFORM";
    public static final String INVALID_CURSOR = "INVALID_CURSOR";
    public static final String PROVIDER_NOT_CONNECTED = "PROVIDER_NOT_CONNECTED";
    public static final String PROVIDER_ACCESS_DENIED = "PROVIDER_ACCESS_DENIED";
    public static final String RATE_LIMITED = "RATE_LIMITED";
    public static final String PROVIDER_UNAVAILABLE = "PROVIDER_UNAVAILABLE";
    public static final String USER_SERVICE_UNAVAILABLE = "USER_SERVICE_UNAVAILABLE";

    private ErrorCodes() {
    }
}
