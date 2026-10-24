-- Izimboa Delivery : tarifs, livraisons, historique des étapes, relevés pluie / trafic.
-- Montants en francs CFA (XAF), entiers. Distances en mètres.

-- Grille de prix par palier de distance (modifiable par l'administration).
CREATE TABLE delivery.tariff_tier (
    id              SERIAL  PRIMARY KEY,
    max_distance_m  INTEGER NOT NULL UNIQUE CHECK (max_distance_m > 0),
    price_xaf       BIGINT  NOT NULL CHECK (price_xaf > 0)
);

-- Réglages de prix : une seule ligne (id = 1).
CREATE TABLE delivery.pricing_settings (
    id                     SMALLINT    PRIMARY KEY CHECK (id = 1),
    express_surcharge_xaf  BIGINT      NOT NULL CHECK (express_surcharge_xaf >= 0),
    cod_commission_bp      INTEGER     NOT NULL CHECK (cod_commission_bp BETWEEN 0 AND 10000), -- 150 = 1,5 %
    return_fee_percent     INTEGER     NOT NULL CHECK (return_fee_percent BETWEEN 0 AND 100),
    updated_at             TIMESTAMPTZ NOT NULL,
    updated_by             UUID
);

-- Base provisoire (décision n° 30), à valider avec vendeurs et livreurs.
INSERT INTO delivery.tariff_tier (max_distance_m, price_xaf) VALUES (3000, 1000), (7000, 1500), (12000, 2000);
INSERT INTO delivery.pricing_settings (id, express_surcharge_xaf, cod_commission_bp, return_fee_percent, updated_at)
VALUES (1, 500, 150, 50, now());

CREATE TABLE delivery.delivery (
    id                     UUID         PRIMARY KEY,
    reference              VARCHAR(12)  NOT NULL UNIQUE,          -- lisible au téléphone : IZ-7K3P9Q
    tracking_token         VARCHAR(32)  NOT NULL UNIQUE,          -- lien de suivi public du destinataire
    sender_id              UUID         NOT NULL,                 -- utilisateur Keycloak
    idempotency_key        VARCHAR(80),
    status                 VARCHAR(20)  NOT NULL,

    pickup_lat             DOUBLE PRECISION NOT NULL,
    pickup_lng             DOUBLE PRECISION NOT NULL,
    pickup_address         VARCHAR(300) NOT NULL,
    pickup_landmark        VARCHAR(300),
    pickup_phone           VARCHAR(20)  NOT NULL,

    dropoff_lat            DOUBLE PRECISION NOT NULL,
    dropoff_lng            DOUBLE PRECISION NOT NULL,
    dropoff_address        VARCHAR(300) NOT NULL,
    dropoff_landmark       VARCHAR(300),
    recipient_name         VARCHAR(120) NOT NULL,
    recipient_phone        VARCHAR(20)  NOT NULL,

    package_description    VARCHAR(200),
    express                BOOLEAN      NOT NULL,
    distance_m             INTEGER      NOT NULL,

    base_price_xaf         BIGINT       NOT NULL,
    express_surcharge_xaf  BIGINT       NOT NULL,
    cod_amount_xaf         BIGINT       NOT NULL CHECK (cod_amount_xaf >= 0),
    cod_commission_xaf     BIGINT       NOT NULL,
    total_fee_xaf          BIGINT       NOT NULL,
    fee_payment            VARCHAR(20)  NOT NULL,                 -- DEDUCTED_FROM_COD | PREPAID
    return_fee_xaf         BIGINT,

    handover_code          VARCHAR(4)     NOT NULL,
    handover_failures      SMALLINT     NOT NULL DEFAULT 0,
    courier_id             UUID,
    delivery_attempts      SMALLINT     NOT NULL DEFAULT 0,

    created_at             TIMESTAMPTZ  NOT NULL,
    updated_at             TIMESTAMPTZ  NOT NULL,
    version                BIGINT       NOT NULL DEFAULT 0,

    UNIQUE (sender_id, idempotency_key)
);
CREATE INDEX delivery_sender_idx  ON delivery.delivery (sender_id, created_at DESC);
CREATE INDEX delivery_courier_idx ON delivery.delivery (courier_id, created_at DESC);
CREATE INDEX delivery_status_idx  ON delivery.delivery (status, created_at DESC);

-- Historique : chaque changement d'étape, qui l'a fait, où et quand.
CREATE TABLE delivery.delivery_event (
    id           BIGSERIAL    PRIMARY KEY,
    delivery_id  UUID         NOT NULL REFERENCES delivery.delivery (id),
    status       VARCHAR(20)  NOT NULL,
    actor_id     UUID,
    lat          DOUBLE PRECISION,
    lng          DOUBLE PRECISION,
    note         VARCHAR(300),
    occurred_at  TIMESTAMPTZ  NOT NULL
);
CREATE INDEX delivery_event_delivery_idx ON delivery.delivery_event (delivery_id, occurred_at);

-- Relevés pluie / trafic envoyés par les livreurs (décision n° 32) : données d'entraînement futures.
CREATE TABLE delivery.road_condition_report (
    id           BIGSERIAL    PRIMARY KEY,
    courier_id   UUID         NOT NULL,
    kind         VARCHAR(20)  NOT NULL,                           -- RAIN | HEAVY_TRAFFIC | CLEAR
    lat          DOUBLE PRECISION NOT NULL,
    lng          DOUBLE PRECISION NOT NULL,
    reported_at  TIMESTAMPTZ  NOT NULL
);
CREATE INDEX road_condition_time_idx ON delivery.road_condition_report (reported_at);
