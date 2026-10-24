-- Izimboa Delivery : présence des livreurs, positions GPS, propositions de courses (attribution automatique).
-- Positions : PostgreSQL simple pour démarrer ; PostGIS + TimescaleDB au lot 5 (décision n° 42), sans changer l'API.

-- Un livreur en ligne et sa dernière position connue (une ligne par livreur).
CREATE TABLE delivery.courier_presence (
    courier_id      UUID        PRIMARY KEY,
    online          BOOLEAN     NOT NULL,
    lat             DOUBLE PRECISION,
    lng             DOUBLE PRECISION,
    accuracy_m      REAL,
    position_at     TIMESTAMPTZ,
    online_since    TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ NOT NULL
);
CREATE INDEX courier_presence_online_idx ON delivery.courier_presence (online) WHERE online;

-- Tracé GPS : un point toutes les ~5 s pendant que le livreur est en ligne (jamais hors ligne).
CREATE TABLE delivery.courier_position (
    id            BIGSERIAL   PRIMARY KEY,
    courier_id    UUID        NOT NULL,
    delivery_id   UUID,
    lat           DOUBLE PRECISION NOT NULL,
    lng           DOUBLE PRECISION NOT NULL,
    accuracy_m    REAL,
    speed_mps     REAL,
    heading_deg   REAL,
    recorded_at   TIMESTAMPTZ NOT NULL,   -- heure du téléphone
    received_at   TIMESTAMPTZ NOT NULL    -- heure de réception (envois groupés après une coupure réseau)
);
CREATE INDEX courier_position_courier_idx  ON delivery.courier_position (courier_id, recorded_at);
CREATE INDEX courier_position_delivery_idx ON delivery.courier_position (delivery_id, recorded_at) WHERE delivery_id IS NOT NULL;

-- Proposition d'une course à un livreur : il a quelques secondes pour accepter, sinon elle passe au suivant.
CREATE TABLE delivery.delivery_offer (
    id            UUID        PRIMARY KEY,
    delivery_id   UUID        NOT NULL REFERENCES delivery.delivery (id),
    courier_id    UUID        NOT NULL,
    status        VARCHAR(12) NOT NULL,   -- OFFERED | ACCEPTED | DECLINED | EXPIRED | CANCELLED
    distance_m    INTEGER     NOT NULL,   -- du livreur au point de ramassage, à vol d'oiseau
    offered_at    TIMESTAMPTZ NOT NULL,
    expires_at    TIMESTAMPTZ NOT NULL,
    answered_at   TIMESTAMPTZ
);
-- Au plus une proposition en attente par livraison et par livreur.
CREATE UNIQUE INDEX delivery_offer_one_per_delivery ON delivery.delivery_offer (delivery_id) WHERE status = 'OFFERED';
CREATE UNIQUE INDEX delivery_offer_one_per_courier  ON delivery.delivery_offer (courier_id)  WHERE status = 'OFFERED';
CREATE INDEX delivery_offer_delivery_idx ON delivery.delivery_offer (delivery_id);
