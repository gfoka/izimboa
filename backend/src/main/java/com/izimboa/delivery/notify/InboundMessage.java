package com.izimboa.delivery.notify;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Message reçu sur le numéro Izimboa (webhook Meta), transmis au support par e-mail (décision 75). */
@Entity
@Table(name = "inbound_message", schema = "delivery")
class InboundMessage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "wa_message_id", nullable = false, length = 120) private String waMessageId;
    @Column(name = "from_phone", nullable = false, length = 20) private String fromPhone;
    @Column(name = "profile_name", length = 120) private String profileName;
    @Column(nullable = false, length = 20) private String type;
    @Column(length = 4000) private String body;
    @Column(name = "context_message_id", length = 120) private String contextMessageId;
    @Column(name = "delivery_id") private UUID deliveryId;
    @Column(name = "received_at", nullable = false) private Instant receivedAt;
    @Column(name = "forwarded_at") private Instant forwardedAt;
    @Column(name = "auto_replied", nullable = false) private boolean autoReplied;

    protected InboundMessage() {}

    InboundMessage(String waMessageId, String fromPhone, String profileName, String type, String body,
                   String contextMessageId, Instant receivedAt) {
        this.waMessageId = waMessageId;
        this.fromPhone = fromPhone;
        this.profileName = profileName;
        this.type = type;
        this.body = body;
        this.contextMessageId = contextMessageId;
        this.receivedAt = receivedAt;
    }

    void linkTo(UUID deliveryId) { this.deliveryId = deliveryId; }
    void forwarded(Instant at) { this.forwardedAt = at; }
    void autoReplied() { this.autoReplied = true; }

    String getWaMessageId() { return waMessageId; }
    String getFromPhone() { return fromPhone; }
    String getProfileName() { return profileName; }
    String getType() { return type; }
    String getBody() { return body; }
    String getContextMessageId() { return contextMessageId; }
    UUID getDeliveryId() { return deliveryId; }
    Instant getReceivedAt() { return receivedAt; }
}
