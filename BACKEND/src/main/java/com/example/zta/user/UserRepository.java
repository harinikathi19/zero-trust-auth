package com.example.zta.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by username
    Optional<User> findByUsername(String username);

    // Check if a username exists
    boolean existsByUsername(String username);
}


