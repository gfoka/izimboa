-- Décision 75 : réponses WhatsApp des clients transmises au support, accusés de réception, note des livraisons.

-- Ce que WhatsApp dit de chaque message envoyé (webhook Meta) : distribué, lu ou échec.
ALTER TABLE delivery.recipient_notification
    ADD COLUMN delivery_status    VARCHAR(12),
    ADD COLUMN delivery_error     VARCHAR(300),
    ADD COLUMN delivery_status_at TIMESTAMPTZ;
CREATE INDEX recipient_notification_message ON delivery.recipient_notification (message_id);

-- Messages reçus sur le numéro Izimboa, transmis par e-mail à support@izimboa.com.
CREATE TABLE delivery.inbound_message (
    id                  BIGSERIAL     PRIMARY KEY,
    wa_message_id       VARCHAR(120)  NOT NULL UNIQUE,
    from_phone          VARCHAR(20)   NOT NULL,
    profile_name        VARCHAR(120),
    type                VARCHAR(20)   NOT NULL,
    body                VARCHAR(4000),
    context_message_id  VARCHAR(120),
    delivery_id         UUID          REFERENCES delivery.delivery (id),
    received_at         TIMESTAMPTZ   NOT NULL,
    forwarded_at        TIMESTAMPTZ,
    auto_replied        BOOLEAN       NOT NULL DEFAULT FALSE
);
CREATE INDEX inbound_message_phone ON delivery.inbound_message (from_phone, received_at DESC);

-- Note donnée par le destinataire depuis son lien de suivi (sans connexion), une par livraison.
CREATE TABLE delivery.delivery_rating (
    delivery_id  UUID          PRIMARY KEY REFERENCES delivery.delivery (id),
    stars        SMALLINT      NOT NULL CHECK (stars BETWEEN 1 AND 5),
    comment      VARCHAR(500),
    created_at   TIMESTAMPTZ   NOT NULL,
    updated_at   TIMESTAMPTZ   NOT NULL
);
