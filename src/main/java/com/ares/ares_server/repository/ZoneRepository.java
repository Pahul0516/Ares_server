package com.ares.ares_server.repository;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ares.ares_server.domain.Zone;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ZoneRepository extends JpaRepository<Zone, Long>{

    List<Zone> findByOwnerId(UUID ownerId);

    Optional<Zone> findById(Long id);

    @Query("""
        SELECT z
        FROM Zone z
        WHERE intersects(z.polygon, :geom) = true
    """)
    List<Zone> findZonesIntersecting(@Param("geom") Geometry geom);
}