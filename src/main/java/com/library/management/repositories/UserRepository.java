package com.library.management.repositories;

import com.library.management.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    User findUserByUsernameAndPassword(String Email, String Password);

    @Query("FROM User u")
    List<User> findAllReader();

    @Query("""
            SELECT u FROM User u
                WHERE (
                    u.email LIKE CONCAT('%', :keyword, '%')
                    OR u.username LIKE CONCAT('%', :keyword, '%')
                    OR u.phone LIKE CONCAT('%', :keyword, '%')
                )
            """)
    List<User> searchReader(@Param("keyword") String keyword);

    @Query("FROM User u WHERE u.id = :id")
    User findById(@Param("id") long id);
}
