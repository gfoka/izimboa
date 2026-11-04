package com.izimboa.delivery.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Note du destinataire (1 à 5 étoiles, remarque facultative), donnée depuis son lien de suivi (décision 75). */
@Entity
@Table(name = "delivery_rating", schema = "delivery")
class DeliveryRating {

    @Id @Column(name = "delivery_id") private UUID deliveryId;
    @Column(nullable = false) private short stars;
    @Column(length = 500) private String comment;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected DeliveryRating() {}

    DeliveryRating(UUID deliveryId, Instant now) {
        this.deliveryId = deliveryId;
        this.createdAt = now;
    }

    void set(int stars, String comment, Instant now) {
        this.stars = (short) stars;
        this.comment = comment;
        this.updatedAt = now;
    }

    UUID getDeliveryId() { return deliveryId; }
    int getStars() { return stars; }
    String getComment() { return comment; }
    Instant getUpdatedAt() { return updatedAt; }
}
