package com.hms.auth.repository;

import com.hms.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    @Query("""
            SELECT u FROM User u 
            LEFT JOIN FETCH u.roles
            WHERE u.username = :username
            """
            )
    Optional<User> findByUsernameWithRoles(String username);
    //Optional<User> findByUsername(String username); // 👈 login ke liye future use
}
