package com.khangdev.identity_service.dto.response;

import com.khangdev.identity_service.enums.AuthProvider;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String username;
    String email;
    String firstName;
    String lastName;
    String userKeycloakId;
    AuthProvider provider;
}
