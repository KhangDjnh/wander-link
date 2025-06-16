package com.khangdev.identity_service.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String oldPassword;
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String newPassword;
}
