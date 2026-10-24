package com.izimboa.delivery.dispatch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Une course proposée à un livreur, qui a quelques secondes pour répondre. */
@Entity
@Table(name = "delivery_offer", schema = "delivery")
class DeliveryOffer {

    enum Status { OFFERED, ACCEPTED, DECLINED, EXPIRED, CANCELLED }

    @Id
    private UUID id;

    @Column(name = "delivery_id", nullable = false) private UUID deliveryId;
    @Column(name = "courier_id", nullable = false) private UUID courierId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Status status;

    @Column(name = "distance_m", nullable = false) private int distanceMeters;
    @Column(name = "offered_at", nullable = false) private Instant offeredAt;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "answered_at") private Instant answeredAt;

    protected DeliveryOffer() {}

    DeliveryOffer(UUID deliveryId, UUID courierId, int distanceMeters, Instant now, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.deliveryId = deliveryId;
        this.courierId = courierId;
        this.distanceMeters = distanceMeters;
        this.status = Status.OFFERED;
        this.offeredAt = now;
        this.expiresAt = expiresAt;
    }

    boolean pending(Instant now) {
        return status == Status.OFFERED && now.isBefore(expiresAt);
    }

    void close(Status result, Instant now) {
        this.status = result;
        this.answeredAt = now;
    }

    UUID id() { return id; }
    UUID deliveryId() { return deliveryId; }
    UUID courierId() { return courierId; }
    Status status() { return status; }
    int distanceMeters() { return distanceMeters; }
    Instant expiresAt() { return expiresAt; }
}
