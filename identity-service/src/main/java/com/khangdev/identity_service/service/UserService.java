package com.khangdev.identity_service.service;

import com.khangdev.identity_service.dto.request.ChangePasswordRequest;
import com.khangdev.identity_service.dto.request.LoginRequest;
import com.khangdev.identity_service.dto.request.RoleRepresentation;
import com.khangdev.identity_service.dto.request.UserRequestDTO;
import com.khangdev.identity_service.dto.response.LoginResponse;
import com.khangdev.identity_service.dto.response.UserResponse;
import com.khangdev.identity_service.entity.User;
import com.khangdev.identity_service.enums.AuthProvider;
import com.khangdev.identity_service.exception.AppException;
import com.khangdev.identity_service.exception.ErrorCode;
import com.khangdev.identity_service.exception.ErrorNormalizer;
import com.khangdev.identity_service.identity.Credential;
import com.khangdev.identity_service.identity.UserCreationParam;
import com.khangdev.identity_service.identity.UserTokenExchangeResponse;
import com.khangdev.identity_service.mapper.UserMapper;
import com.khangdev.identity_service.repository.IdentityClient;
import com.khangdev.identity_service.repository.UserRepository;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    IdentityClient identityClient;
    ErrorNormalizer errorNormalizer;
    KeycloakClientTokenService keycloakClientTokenService;
    KeycloakUserTokenService keycloakUserTokenService;
    UserMapper  userMapper;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String decodedPassword = user.getPassword();
        if(!passwordEncoder.matches(request.getPassword(), decodedPassword)) {
            throw new AppException(ErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }
        UserTokenExchangeResponse tokenResponse = keycloakUserTokenService.getTokenInfo(request);
        return LoginResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .refreshExpiresIn(tokenResponse.getRefreshExpiresIn())
                .tokenType(tokenResponse.getTokenType())
                .idToken(tokenResponse.getIdToken())
                .scope(tokenResponse.getScope())
                .user(userMapper.toUserResponse(user))
                .build();
    }
    public UserResponse createUser(UserRequestDTO request) {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        try {
            var token = keycloakClientTokenService.getAccessToken();
            var creationResponse = identityClient.createUser(
                    "Bearer " + token,
                    UserCreationParam.builder()
                            .username(request.getUsername())
                            .email(request.getEmail())
                            .firstName(request.getFirstName())
                            .lastName(request.getLastName())
                            .enabled(true)
                            .emailVerified(false)
                            .credentials(List.of(Credential.builder()
                                    .type("password")
                                    .value(request.getPassword())
                                    .temporary(false)
                                    .build()))
                            .build());
            //Khi goi toi api createUser de tao User tren Keycloak thi tra ve Header.Location co chua userId, ta can lay no dua vao db
            String userKeycloakId = extractUserId(creationResponse);
            // Gán role USER + role chính từ request
            List<String> rolesToAssign = List.of("USER", request.getRole().name());

            List<RoleRepresentation> roleRepresentations = rolesToAssign.stream()
                    .map(roleName -> identityClient.getRoleByName("Bearer " + token, roleName))
                    .collect(Collectors.toList());

            identityClient.assignRealmRolesToUser("Bearer " + token, userKeycloakId, roleRepresentations);

            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(request.getPassword())
                    .userKeycloakId(userKeycloakId)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .provider(AuthProvider.LOCAL)
                    .isActive(true)
                    .build();
            user = userRepository.save(user);
            return userMapper.toUserResponse(user);
        } catch (FeignException exception) {
            throw errorNormalizer.handleKeycloakException(exception);
        }
    }

    @PreAuthorize( "hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }
    @PreAuthorize( "hasRole('ADMIN')")
    public UserResponse getUserById(Long userId) {
        return userRepository.findById(userId).map(userMapper::toUserResponse).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
    @PreAuthorize("hasRole('USER')")
    public UserResponse getMyInfo(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userKeycloakId = authentication.getName();
        var user = userRepository.findByUserKeycloakId(userKeycloakId);
        return userMapper.toUserResponse(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new AppException(ErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String userKeycloakId = user.getUserKeycloakId();
        String oldEncodedPassword = user.getPassword();
        if(!passwordEncoder.matches(request.getOldPassword(), oldEncodedPassword)) {
            throw new AppException(ErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }
        String accessToken = keycloakClientTokenService.getAccessToken();

        identityClient.resetUserPassword(
                "Bearer " + accessToken,
                "wander-link",
                userKeycloakId,
                Credential.builder()
                        .type("password")
                        .value(request.getNewPassword())
                        .temporary(false)
                        .build()
        );

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setIsActive(false);
        userRepository.save(user);
    }
    private String extractUserId(ResponseEntity<?> responseEntity) {
        String location = Objects.requireNonNull(responseEntity.getHeaders().get("Location")).getFirst();
        String[] splittedString = location.split("/");
        return splittedString[splittedString.length - 1];
    }
}
