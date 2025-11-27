package com.ares.ares_server.repository;

import com.ares.ares_server.domain.Run;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RunRepository extends JpaRepository<Run, Long>{
    List<Run> findByOwnerId(UUID ownerId);
    List<Run> findByOwnerUsername(String username);
}