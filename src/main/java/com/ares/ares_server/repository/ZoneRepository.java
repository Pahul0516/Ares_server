package com.ares.ares_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ares.ares_server.domain.Zone;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ZoneRepository extends JpaRepository<Zone, Long>{

    List<Zone> findByOwnerId(UUID ownerId);

    Optional<Zone> findById(Long id);
}