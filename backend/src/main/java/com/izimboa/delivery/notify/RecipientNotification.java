package com.izimboa.delivery.notify;

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

/** Trace d'un message WhatsApp au destinataire : un par livraison et par étape. */
@Entity
@Table(name = "recipient_notification", schema = "delivery")
class RecipientNotification {

    enum Status { SENDING, SENT, FAILED, DISABLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "delivery_id", nullable = false) private UUID deliveryId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RecipientNotice.Kind kind;
    @Column(nullable = false, length = 60) private String template;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12) private Status status;
    @Column(name = "message_id", length = 120) private String messageId;
    @Column(length = 300) private String error;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected RecipientNotification() {}

    RecipientNotification(UUID deliveryId, RecipientNotice.Kind kind, String template, Instant now) {
        this.deliveryId = deliveryId;
        this.kind = kind;
        this.template = template;
        this.status = Status.SENDING;
        this.createdAt = now;
        this.updatedAt = now;
    }

    void finish(Status status, String messageId, String error, Instant now) {
        this.status = status;
        this.messageId = messageId;
        this.error = error;
        this.updatedAt = now;
    }

    RecipientNotice.Kind getKind() { return kind; }
    Status getStatus() { return status; }
    Instant getUpdatedAt() { return updatedAt; }
}
