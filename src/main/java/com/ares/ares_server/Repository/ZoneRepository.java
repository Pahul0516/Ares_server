package com.ares.ares_server.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ares.ares_server.Domain.Zone;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ZoneRepository extends JpaRepository<Zone, Long>{

    List<Zone> findByOwnerId(UUID owner_id);

    Optional<Zone> findById(Long id);
}