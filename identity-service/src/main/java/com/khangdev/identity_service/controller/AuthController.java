package com.khangdev.identity_service.controller;

import com.khangdev.identity_service.dto.request.LoginRequest;
import com.khangdev.identity_service.dto.request.UserRequestDTO;
import com.khangdev.identity_service.dto.response.ApiResponse;
import com.khangdev.identity_service.dto.response.LoginResponse;
import com.khangdev.identity_service.dto.response.UserResponse;
import com.khangdev.identity_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthController {
    UserService userService;

    @PostMapping("/login")
    ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.<LoginResponse>builder()
                .code(1000)
                .message("Success")
                .result(userService.login(request))
                .build();
    }
    @PostMapping("/register")
    ApiResponse<UserResponse> register(@RequestBody @Valid UserRequestDTO request) {
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Success")
                .result(userService.createUser(request))
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody String refreshToken) {
        userService.logout(refreshToken);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Success")
                .build();
    }

    @PostMapping("/verify-token")
    ApiResponse<Boolean> verifyToken(@RequestBody String token) {
        return ApiResponse.<Boolean>builder()
                .code(1000)
                .message("Success")
                .result(userService.verifyToken(token))
                .build();
    }
}