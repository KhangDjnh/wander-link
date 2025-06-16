package com.khangdev.identity_service.repository;

import com.khangdev.identity_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsById(Long id);
    boolean existsByUsername(String userName);
    User findByUserKeycloakId(String userKeycloakId);
    Optional<User> findByEmail(String email);
}
