package com.khangdev.identity_service.repository;

import com.khangdev.identity_service.dto.request.RoleRepresentation;
import com.khangdev.identity_service.identity.*;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "identity-client", url = "${idp.url}")
public interface IdentityClient {

    @PostMapping(
            value = "/realms/wander-link/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ClientTokenExchangeResponse exchangeClientToken(@QueryMap ClientTokenExchangeParam tokenExchangeParam);

    @PostMapping(
            value = "/realms/wander-link/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    UserTokenExchangeResponse exchangeUserAccessToken(@QueryMap UserAccessTokenExchangeParam tokenExchangeParam);

    @PostMapping(
            value = "/realms/wander-link/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    UserTokenExchangeResponse exchangeUserRefreshToken(@QueryMap UserRefreshTokenExchangeParam tokenExchangeParam);

    @PostMapping(
            value = "/admin/realms/wander-link/users",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> createUser(
            @RequestHeader("Authorization") String token,
            @RequestBody UserCreationParam userCreationParam);

    @PutMapping("/admin/realms/{realm}/users/{id}/reset-password")
    void resetUserPassword(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("realm") String realm,
            @PathVariable("id") String userId,
            @RequestBody Credential credential
    );

    // ✅ Lấy thông tin role từ tên
    @GetMapping("/admin/realms/wander-link/roles/{roleName}")
    RoleRepresentation getRoleByName(
            @RequestHeader("Authorization") String token,
            @PathVariable("roleName") String roleName
    );

    // ✅ Gán các role vào user
    @PostMapping("/admin/realms/wander-link/users/{userId}/role-mappings/realm")
    void assignRealmRolesToUser(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") String userId,
            @RequestBody List<RoleRepresentation> roles
    );
    @PostMapping(value = "/realms/wander-link/protocol/openid-connect/logout",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void logout(@RequestHeader("Authorization") String clientToken,
                @RequestParam("refresh_token") String refreshToken,
                @RequestParam("client_id") String clientId
    );

    @PostMapping(value = "/realms/wander-link/protocol/openid-connect/token/introspect",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, Object> introspectToken(
            @RequestHeader("Authorization") String clientToken,
            @RequestParam("token") String token,
            @RequestParam("token_type_hint") String tokenTypeHint
    );

}