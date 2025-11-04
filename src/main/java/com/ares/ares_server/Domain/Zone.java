package com.ares.ares_server.Domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.locationtech.jts.geom.Polygon;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "zones")
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zones_id_gen")
    @SequenceGenerator(name = "zones_id_gen", sequenceName = "zones_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "last_updated", nullable = false)
    private OffsetDateTime lastUpdated;

    @ColumnDefault("auth.uid()")
    @Column(name = "owner")
    private UUID owner;

    @NotNull
    @Column(name = "polygon", nullable = false)
    private Polygon polygon;

    @ColumnDefault("st_area(st_transform(polygon, 3857))")
    @Column(name = "area")
    private Double area;

}