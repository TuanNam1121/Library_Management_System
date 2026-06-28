package com.library.management.repositories;

import com.library.management.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    User findUserByUsernameAndPassword(String Email, String Password);
}
