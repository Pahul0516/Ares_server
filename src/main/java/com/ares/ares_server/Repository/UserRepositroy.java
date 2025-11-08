package com.ares.ares_server.Repository;

import com.ares.ares_server.Domain.User;
import com.ares.ares_server.Domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepositroy extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

}
