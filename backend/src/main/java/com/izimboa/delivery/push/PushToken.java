package com.izimboa.delivery.push;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Jeton Firebase d'un téléphone : un livreur peut en avoir plusieurs (changement de téléphone). */
@Entity
@Table(name = "push_token", schema = "delivery")
class PushToken {

    @Id @Column(length = 512) private String token;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "app_version", length = 40) private String appVersion;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected PushToken() {}

    PushToken(String token, UUID userId, String appVersion, Instant now) {
        this.token = token;
        this.userId = userId;
        this.appVersion = appVersion;
        this.createdAt = now;
        this.updatedAt = now;
    }

    void refresh(UUID userId, String appVersion, Instant now) {
        this.userId = userId;
        this.appVersion = appVersion;
        this.updatedAt = now;
    }

    String token() { return token; }
    UUID userId() { return userId; }
}
