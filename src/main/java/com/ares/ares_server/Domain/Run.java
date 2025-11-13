package com.ares.ares_server.Domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.locationtech.jts.geom.Polygon;

import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "runs")
public class Run {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "runs_id_gen")
    @SequenceGenerator(name = "runs_id_gen", sequenceName = "runs_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "run_owner_fkey"))
    private User owner;

    @Column(name = "distance")
    private Float distance;

    @Column(name = "area_gained")
    private Float areaGained;

    @Column(name = "polygon")
    private Polygon polygon;

    @Column(name = "duration")
    private Instant duration;
}