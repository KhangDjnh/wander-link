package com.khangdev.identity_service.controller;

import com.khangdev.identity_service.dto.request.ChangePasswordRequest;
import com.khangdev.identity_service.dto.request.UserRequestDTO;
import com.khangdev.identity_service.dto.response.ApiResponse;
import com.khangdev.identity_service.dto.response.UserResponse;
import com.khangdev.identity_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class UserController {
    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserRequestDTO request) {
        return ApiResponse.<UserResponse>builder()
                .message("Success")
                .code(1000)
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getAllUsers () {
        return ApiResponse.<List<UserResponse>>builder()
                .code(1000)
                .message("Success")
                .result(userService.getAllUsers())
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUserById (@PathVariable Long userId) {
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Success")
                .result(userService.getUserById(userId))
                .build();
    }
    @GetMapping("/getUserInfo")
    ApiResponse<UserResponse> getUserInfo () {
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Success")
                .result(userService.getMyInfo())
                .build();
    }

    @PutMapping("/{userId}/change-password")
    @PreAuthorize("hasRole('ADMIN') or @authz.isOwner(#userId)")
    ApiResponse<String> changePassword (@PathVariable Long userId, @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Success")
                .result("Password changed successfully")
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser (@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Success")
                .result("User deleted successfully")
                .build();
    }

}