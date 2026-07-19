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

    @Query("FROM User u WHERE u.role.id = 3")
    List<User> findAllReader();

    @Query("""
            SELECT u FROM User u
                WHERE (
                    u.email LIKE CONCAT('%', :keyword, '%')
                    OR u.username LIKE CONCAT('%', :keyword, '%')
                    OR u.phone LIKE CONCAT('%', :keyword, '%')
                )
                AND u.role.id = 3
            """)
    List<User> searchReader(@Param("keyword") String keyword);

    User findById(long id);
}
