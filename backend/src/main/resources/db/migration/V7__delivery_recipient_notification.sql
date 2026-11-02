-- Messages WhatsApp envoyés automatiquement au destinataire (décision 73) : un seul par livraison et par étape.
-- status : SENDING (en cours), SENT (accepté par Meta), FAILED (refusé), DISABLED (envoi désactivé sur ce serveur).
CREATE TABLE delivery.recipient_notification (
    id           BIGSERIAL    PRIMARY KEY,
    delivery_id  UUID         NOT NULL REFERENCES delivery.delivery (id),
    kind         VARCHAR(20)  NOT NULL,
    template     VARCHAR(60)  NOT NULL,
    status       VARCHAR(12)  NOT NULL,
    message_id   VARCHAR(120),
    error        VARCHAR(300),
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    UNIQUE (delivery_id, kind)
);
