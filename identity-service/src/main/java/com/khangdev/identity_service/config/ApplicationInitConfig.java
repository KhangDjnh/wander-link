package com.khangdev.identity_service.config;

import com.khangdev.identity_service.entity.User;
import com.khangdev.identity_service.enums.AuthProvider;
import com.khangdev.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            if(!userRepository.existsByUsername("admin")) {
                User user = User.builder()
                        .username("admin")
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("admin"))
                        .firstName("Dinh")
                        .lastName("Khang")
                        .userKeycloakId("aec921fa-6dfa-43a1-8473-28b4628f475b")
                        .provider(AuthProvider.LOCAL)
                        .build();
                userRepository.save(user);
                log.warn("Info: Admin user created with default password");
            }

        };
    }
}