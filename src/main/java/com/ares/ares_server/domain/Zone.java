package com.ares.ares_server.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.locationtech.jts.geom.Polygon;

import java.time.OffsetDateTime;

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

    @NotNull
    @Column(name = "polygon", nullable = false)
    private Polygon polygon;

    @ColumnDefault("st_area(st_transform(polygon, 3857))")
    @Column(name = "area", insertable = false, updatable = false)
    private Double area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner", referencedColumnName = "id", nullable = false,foreignKey = @ForeignKey(name = "zones_owner_fkey"))
    private User owner;
}