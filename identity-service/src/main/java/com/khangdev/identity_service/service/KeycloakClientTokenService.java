package com.khangdev.identity_service.service;

import com.khangdev.identity_service.identity.ClientTokenExchangeParam;
import com.khangdev.identity_service.identity.ClientTokenExchangeResponse;
import com.khangdev.identity_service.repository.IdentityClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class KeycloakClientTokenService {
    private final IdentityClient identityClient;

    @Value("${idp.client-id}")
    private String clientId;

    @Value("${idp.client-secret}")
    private String clientSecret;

    private String cachedToken;
    private Instant tokenExpiry;



    public synchronized String getAccessToken() {
        if(cachedToken == null || tokenExpiry == null || Instant.now().isAfter(tokenExpiry.minusSeconds(60))) {
            refreshToken();
        }
        return cachedToken;
    }
    private void refreshToken() {
        ClientTokenExchangeParam param = ClientTokenExchangeParam.builder()
                .grant_type("client_credentials")
                .client_id(clientId)
                .client_secret(clientSecret)
                .scope("openid")
                .build();

        ClientTokenExchangeResponse response = identityClient.exchangeClientToken(param);

        this.cachedToken = response.getAccessToken();
        this.tokenExpiry = Instant.now().plusSeconds(Long.parseLong(response.getExpiresIn()));
    }

}