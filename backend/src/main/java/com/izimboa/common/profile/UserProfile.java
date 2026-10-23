package com.izimboa.common.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Trace minimale d'un utilisateur côté API : son identifiant Keycloak et ses dates.
 * Aucune donnée de connexion ici (elles vivent uniquement dans Keycloak).
 */
@Entity
@Table(name = "user_profile", schema = "common")
public class UserProfile {

    @Id
    private UUID id;

    @Column(name = "first_seen_at", nullable = false, updatable = false)
    private Instant firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    protected UserProfile() {}

    public UserProfile(UUID id, Instant now) {
        this.id = id;
        this.firstSeenAt = now;
        this.lastSeenAt = now;
    }

    public void seen(Instant now) {
        this.lastSeenAt = now;
    }

    public UUID getId() { return id; }
    public Instant getFirstSeenAt() { return firstSeenAt; }
    public Instant getLastSeenAt() { return lastSeenAt; }
}
