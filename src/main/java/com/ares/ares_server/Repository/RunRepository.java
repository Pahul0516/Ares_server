package com.ares.ares_server.Repository;

import com.ares.ares_server.Domain.Run;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RunRepository extends JpaRepository<Run, Long>{
    List<Run> findByOwnerId(UUID owner_id);
}