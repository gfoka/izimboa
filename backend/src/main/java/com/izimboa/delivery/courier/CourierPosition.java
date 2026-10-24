package com.izimboa.delivery.courier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Un point du tracé GPS d'un livreur (jamais modifié). Matière première de la future IA (décision n° 45). */
@Entity
@Table(name = "courier_position", schema = "delivery")
class CourierPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "courier_id", nullable = false) private UUID courierId;
    @Column(name = "delivery_id") private UUID deliveryId;
    @Column(nullable = false) private double lat;
    @Column(nullable = false) private double lng;
    @Column(name = "accuracy_m") private Float accuracyMeters;
    @Column(name = "speed_mps") private Float speedMps;
    @Column(name = "heading_deg") private Float headingDeg;
    @Column(name = "recorded_at", nullable = false) private Instant recordedAt;
    @Column(name = "received_at", nullable = false) private Instant receivedAt;

    protected CourierPosition() {}

    CourierPosition(UUID courierId, UUID deliveryId, double lat, double lng, Float accuracyMeters, Float speedMps,
                    Float headingDeg, Instant recordedAt, Instant receivedAt) {
        this.courierId = courierId;
        this.deliveryId = deliveryId;
        this.lat = lat;
        this.lng = lng;
        this.accuracyMeters = accuracyMeters;
        this.speedMps = speedMps;
        this.headingDeg = headingDeg;
        this.recordedAt = recordedAt;
        this.receivedAt = receivedAt;
    }
}
