package com.khangdev.identity_service.entity;

import com.khangdev.identity_service.enums.AuthProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull(message = "Username is missing")
    String username;

    @NotNull(message = "Email is missing")
    @Email(message = "Invalid email")
    @Column(unique = true)
    String email;

    @NotNull(message = "Password is missing")
    String password;

    @Column(name = "user_keycloak_id")
    @NotNull(message = "Keycloak user id is missing")
    String userKeycloakId;

    @NotNull(message = "Provider is missing")
    @Enumerated(EnumType.STRING)
    AuthProvider provider;

    @Column(name = "last_login")
    Instant lastLogin;

    @Column(name = "first_name")
    @NotNull(message = "First name is missing")
    String firstName;

    @Column(name = "last_name")
    @NotNull(message = "Last name is missing")
    String lastName;

    @Column(name = "is_active")
    Boolean isActive;

    @Column(name = "created_at")
    @CreationTimestamp
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    @CreationTimestamp
    LocalDateTime updatedAt;

    @Column(name = "failed_login_attempts")
    int failedLoginAttempts;
}
