package com.khangdev.identity_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("authz")
public class AuthorizationHelper {

    public boolean isOwner(String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        // Giả sử bạn dùng Keycloak, token là KeycloakPrincipal -> lấy sub
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            String sub = jwt.getClaimAsString("sub");
            return sub.equals(userId);
        }

        return false;
    }
}