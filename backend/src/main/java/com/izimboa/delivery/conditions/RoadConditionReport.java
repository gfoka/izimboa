package com.izimboa.delivery.conditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Relevé terrain d'un livreur (« il pleut ici », « gros bouchon »).
 * Données d'entraînement du futur modèle qui affinera délais et primes (décision n° 32).
 */
@Entity
@Table(name = "road_condition_report", schema = "delivery")
class RoadConditionReport {

    enum Kind { RAIN, HEAVY_TRAFFIC, CLEAR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "courier_id", nullable = false)
    private UUID courierId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Kind kind;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    @Column(name = "reported_at", nullable = false)
    private Instant reportedAt;

    protected RoadConditionReport() {}

    RoadConditionReport(UUID courierId, Kind kind, double lat, double lng, Instant reportedAt) {
        this.courierId = courierId;
        this.kind = kind;
        this.lat = lat;
        this.lng = lng;
        this.reportedAt = reportedAt;
    }

    Long id() { return id; }
    Instant reportedAt() { return reportedAt; }
}
