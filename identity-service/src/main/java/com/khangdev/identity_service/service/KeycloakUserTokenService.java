package com.khangdev.identity_service.service;

import com.khangdev.identity_service.dto.request.LoginRequest;
import com.khangdev.identity_service.entity.User;
import com.khangdev.identity_service.exception.AppException;
import com.khangdev.identity_service.exception.ErrorCode;
import com.khangdev.identity_service.identity.UserAccessTokenExchangeParam;
import com.khangdev.identity_service.identity.UserRefreshTokenExchangeParam;
import com.khangdev.identity_service.identity.UserTokenExchangeResponse;
import com.khangdev.identity_service.repository.IdentityClient;
import com.khangdev.identity_service.repository.UserRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class KeycloakUserTokenService {
    private final IdentityClient identityClient;
    private final UserRepository userRepository;

    @Value("${idp.client-id}")
    private String clientId;

    @Value("${idp.client-secret}")
    private String clientSecret;

    private String cachedToken;
    private Instant tokenExpiry;
    private String refreshToken;
    private Instant refreshTokenExpiry;


    public synchronized String getAccessToken(LoginRequest request) {
        if (cachedToken == null || tokenExpiry == null || Instant.now().isAfter(tokenExpiry.minusSeconds(60))) {
            refreshToken(request);
        }
        return cachedToken;
    }

    private void refreshToken(LoginRequest request) {
        try {
            if (refreshToken != null && Instant.now().isBefore(refreshTokenExpiry)) {
                // Gọi grant_type=refresh_token
                UserRefreshTokenExchangeParam param = UserRefreshTokenExchangeParam.builder()
                        .grant_type("refresh_token")
                        .client_id(clientId)
                        .client_secret(clientSecret)
                        .refresh_token(refreshToken)
                        .build();

                UserTokenExchangeResponse response = identityClient.exchangeUserRefreshToken(param);

                if (response == null || response.getAccessToken() == null)
                    throw new AppException(ErrorCode.UNAUTHORIZED); // Tùy bạn định nghĩa

                this.cachedToken = response.getAccessToken();
                this.tokenExpiry = Instant.now().plusSeconds(Long.parseLong(response.getExpiresIn()));
                this.refreshToken = response.getRefreshToken();
                this.refreshTokenExpiry = Instant.now().plusSeconds(Long.parseLong(response.getRefreshExpiresIn()));

            } else {
                UserAccessTokenExchangeParam param = UserAccessTokenExchangeParam.builder()
                        .grant_type("password")
                        .client_id(clientId)
                        .client_secret(clientSecret)
                        .username(getKeycloakUsername(request.getEmail()))
                        .password(request.getPassword())
                        .scope("openid")
                        .build();

                UserTokenExchangeResponse response = identityClient.exchangeUserAccessToken(param);

                if (response == null || response.getAccessToken() == null)
                    throw new AppException(ErrorCode.UNAUTHORIZED);

                this.cachedToken = response.getAccessToken();
                this.tokenExpiry = Instant.now().plusSeconds(Long.parseLong(response.getExpiresIn()));
                this.refreshToken = response.getRefreshToken();
                this.refreshTokenExpiry = Instant.now().plusSeconds(Long.parseLong(response.getRefreshExpiresIn()));
            }
        } catch (FeignException e) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }


    public synchronized UserTokenExchangeResponse getTokenInfo(LoginRequest request) {
        if (cachedToken == null || tokenExpiry == null || Instant.now().isAfter(tokenExpiry.minusSeconds(60))) {
            refreshToken(request);
        }

        return UserTokenExchangeResponse.builder()
                .accessToken(cachedToken)
                .refreshToken(refreshToken)
                .expiresIn(String.valueOf(Duration.between(Instant.now(), tokenExpiry).getSeconds()))
                .refreshExpiresIn(String.valueOf(Duration.between(Instant.now(), refreshTokenExpiry).getSeconds()))
                .tokenType("Bearer")
                .idToken("N/A")
                .scope("openid")
                .build();
    }

    private String getKeycloakUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return user.getUsername();
    }

}
