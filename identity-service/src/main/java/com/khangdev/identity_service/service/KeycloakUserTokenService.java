package com.khangdev.identity_service.service;

import com.khangdev.identity_service.dto.CachedUserToken;
import com.khangdev.identity_service.dto.request.LoginRequest;
import com.khangdev.identity_service.exception.AppException;
import com.khangdev.identity_service.exception.ErrorCode;
import com.khangdev.identity_service.identity.UserAccessTokenExchangeParam;
import com.khangdev.identity_service.identity.UserRefreshTokenExchangeParam;
import com.khangdev.identity_service.identity.UserTokenExchangeResponse;
import com.khangdev.identity_service.repository.IdentityClient;
import com.khangdev.identity_service.repository.UserRepository;
import com.khangdev.identity_service.repository.UserTokenCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
@Service
public class KeycloakUserTokenService {

    private final IdentityClient identityClient;
    private final UserRepository userRepository;
    private final UserTokenCacheRepository tokenCacheRepository;

    @Value("${idp.client-id}")
    private String clientId;

    @Value("${idp.client-secret}")
    private String clientSecret;

    public synchronized UserTokenExchangeResponse getTokenInfo(LoginRequest request) {
        String email = request.getEmail();
        CachedUserToken cached = tokenCacheRepository.getToken(email);

        if (cached != null && Instant.now().isBefore(cached.getAccessTokenExpiry().minusSeconds(60))) {
            return buildResponse(cached);
        }

        return refreshToken(request, cached);
    }

    private UserTokenExchangeResponse refreshToken(LoginRequest request, CachedUserToken cachedToken) {
        UserTokenExchangeResponse response;

        if (cachedToken != null && cachedToken.getRefreshToken() != null
                && Instant.now().isBefore(cachedToken.getRefreshTokenExpiry())) {

            UserRefreshTokenExchangeParam param = UserRefreshTokenExchangeParam.builder()
                    .grant_type("refresh_token")
                    .client_id(clientId)
                    .client_secret(clientSecret)
                    .refresh_token(cachedToken.getRefreshToken())
                    .build();

            response = identityClient.exchangeUserRefreshToken(param);

        } else {
            String username = getKeycloakUsername(request.getEmail());

            UserAccessTokenExchangeParam param = UserAccessTokenExchangeParam.builder()
                    .grant_type("password")
                    .client_id(clientId)
                    .client_secret(clientSecret)
                    .username(username)
                    .password(request.getPassword())
                    .scope("openid")
                    .build();

            response = identityClient.exchangeUserAccessToken(param);
        }

        Instant now = Instant.now();
        CachedUserToken newToken = CachedUserToken.builder()
                .accessToken(response.getAccessToken())
                .refreshToken(response.getRefreshToken())
                .accessTokenExpiry(now.plusSeconds(Long.parseLong(response.getExpiresIn())))
                .refreshTokenExpiry(now.plusSeconds(Long.parseLong(response.getRefreshExpiresIn())))
                .build();

        tokenCacheRepository.saveToken(request.getEmail(), newToken,
                Duration.ofSeconds(Long.parseLong(response.getRefreshExpiresIn())));

        return buildResponse(newToken);
    }

    private UserTokenExchangeResponse buildResponse(CachedUserToken token) {
        return UserTokenExchangeResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .expiresIn(String.valueOf(Duration.between(Instant.now(), token.getAccessTokenExpiry()).getSeconds()))
                .refreshExpiresIn(String.valueOf(Duration.between(Instant.now(), token.getRefreshTokenExpiry()).getSeconds()))
                .tokenType("Bearer")
                .idToken("N/A")
                .scope("openid profile email")
                .build();
    }

    private String getKeycloakUsername(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
                .getUsername();
    }
}

