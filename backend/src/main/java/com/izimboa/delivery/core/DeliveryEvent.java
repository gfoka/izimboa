package com.izimboa.delivery.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Une ligne de l'historique : quelle étape, qui, où, quand. Jamais modifiée. */
@Entity
@Table(name = "delivery_event", schema = "delivery")
class DeliveryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(name = "actor_id")
    private UUID actorId;

    private Double lat;
    private Double lng;

    @Column(length = 300)
    private String note;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected DeliveryEvent() {}

    DeliveryEvent(Delivery delivery, DeliveryStatus status, UUID actorId, Double lat, Double lng, String note, Instant at) {
        this.delivery = delivery;
        this.status = status;
        this.actorId = actorId;
        this.lat = lat;
        this.lng = lng;
        this.note = note;
        this.occurredAt = at;
    }

    DeliveryStatus status() { return status; }
    UUID actorId() { return actorId; }
    Double lat() { return lat; }
    Double lng() { return lng; }
    String note() { return note; }
    Instant occurredAt() { return occurredAt; }
}
