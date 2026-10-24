package com.izimboa.delivery.courier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Un livreur : en ligne ou non, et sa dernière position connue. */
@Entity
@Table(name = "courier_presence", schema = "delivery")
class CourierPresence {

    @Id
    @Column(name = "courier_id")
    private UUID courierId;

    @Column(nullable = false)
    private boolean online;

    private Double lat;
    private Double lng;

    @Column(name = "accuracy_m")
    private Float accuracyMeters;

    @Column(name = "position_at")
    private Instant positionAt;

    @Column(name = "online_since")
    private Instant onlineSince;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CourierPresence() {}

    CourierPresence(UUID courierId, Instant now) {
        this.courierId = courierId;
        this.updatedAt = now;
    }

    void goOnline(Instant now) {
        if (!online) onlineSince = now;
        online = true;
        updatedAt = now;
    }

    /** Hors ligne : on garde la dernière position mais elle ne sert plus à rien (ni attribution, ni suivi). */
    void goOffline(Instant now) {
        online = false;
        onlineSince = null;
        updatedAt = now;
    }

    /** Ne garde que la position la plus récente (les points peuvent arriver dans le désordre après une coupure). */
    void moveTo(double lat, double lng, Float accuracy, Instant recordedAt, Instant now) {
        if (positionAt == null || recordedAt.isAfter(positionAt)) {
            this.lat = lat;
            this.lng = lng;
            this.accuracyMeters = accuracy;
            this.positionAt = recordedAt;
        }
        updatedAt = now;
    }

    UUID courierId() { return courierId; }
    boolean online() { return online; }
    Double lat() { return lat; }
    Double lng() { return lng; }
    Float accuracyMeters() { return accuracyMeters; }
    Instant positionAt() { return positionAt; }
    Instant onlineSince() { return onlineSince; }
}
