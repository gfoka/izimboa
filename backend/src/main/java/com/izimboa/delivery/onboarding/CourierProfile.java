package com.izimboa.delivery.onboarding;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Dossier d'un livreur : identité déclarée et décision de l'équipe Izimboa. */
@Entity
@Table(name = "courier_profile", schema = "delivery")
class CourierProfile {

    /**
     * DRAFT : il remplit son dossier ; PENDING : envoyé, à vérifier ; ACTIVE : validé (rôle livreur donné) ;
     * REJECTED : refusé, il peut corriger et renvoyer ; SUSPENDED : suspendu par l'équipe.
     */
    enum Status { DRAFT, PENDING, ACTIVE, REJECTED, SUSPENDED }

    @Id @Column(name = "user_id") private UUID userId;
    @Column(length = 20) private String phone;
    @Column(name = "first_name", length = 60) private String firstName;
    @Column(name = "last_name", length = 60) private String lastName;
    @Column(length = 120) private String email;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private Status status;
    @Column(name = "status_reason", length = 300) private String statusReason;
    @Column(name = "submitted_at") private Instant submittedAt;
    @Column(name = "reviewed_at") private Instant reviewedAt;
    @Column(name = "reviewed_by") private UUID reviewedBy;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected CourierProfile() {}

    /** {@code alreadyCourier} : livreur activé avant la vérification d'identité (il complète son dossier en restant actif). */
    CourierProfile(UUID userId, String phone, boolean alreadyCourier, Instant now) {
        this.userId = userId;
        this.phone = phone;
        this.status = alreadyCourier ? Status.ACTIVE : Status.DRAFT;
        this.createdAt = now;
        this.updatedAt = now;
    }

    void rename(String firstName, String lastName, String email, String phone, Instant now) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        if (phone != null) this.phone = phone;
        this.updatedAt = now;
    }

    /** Envoi pour vérification. Un livreur déjà actif reste actif (ses nouveaux documents sont marqués à revoir). */
    void submit(Instant now) {
        if (status == Status.DRAFT || status == Status.REJECTED) {
            status = Status.PENDING;
            statusReason = null;
        }
        submittedAt = now;
        updatedAt = now;
    }

    void decide(Status next, String reason, UUID by, Instant now) {
        this.status = next;
        this.statusReason = reason;
        this.reviewedAt = now;
        this.reviewedBy = by;
        this.updatedAt = now;
    }

    void touch(Instant now) { this.updatedAt = now; }

    UUID getUserId() { return userId; }
    String getPhone() { return phone; }
    String getFirstName() { return firstName; }
    String getLastName() { return lastName; }
    String getEmail() { return email; }
    Status getStatus() { return status; }
    String getStatusReason() { return statusReason; }
    Instant getSubmittedAt() { return submittedAt; }
    Instant getReviewedAt() { return reviewedAt; }
    Instant getCreatedAt() { return createdAt; }
    Instant getUpdatedAt() { return updatedAt; }

    boolean hasName() {
        return firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank();
    }

    String displayName() {
        return hasName() ? firstName + " " + lastName : null;
    }
}
