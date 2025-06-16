package com.khangdev.identity_service.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    EXISTED_USER(1001, "User already existed", HttpStatus.CONFLICT),
    USER_NOT_FOUND(1002, "User not found", HttpStatus.NOT_FOUND),
    INVALID_USERNAME_OR_PASSWORD(1003, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    INVALID_USERNAME(1004, "Invalid username", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1005, "Invalid password", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1006, "Invalid token", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED(1007, "User is not authenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1008, "User is not authorized", HttpStatus.FORBIDDEN),
    INVALID_KEY(1009, "Invalid key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1010, "User already existed", HttpStatus.CONFLICT),
    EMAIL_EXISTED(1011, "Email already existed", HttpStatus.CONFLICT),
    USERNAME_IS_MISSING(1012, "Username is missing", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SAME_AS_OLD(1013, "New password is same as old password", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR(1014, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR)
    ;

    int code;
    String message;
    HttpStatusCode httpStatusCode;
    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}