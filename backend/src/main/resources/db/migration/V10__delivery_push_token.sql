-- Jetons Firebase Cloud Messaging des téléphones des livreurs (décision 76) : réveillent l'application
-- pour une nouvelle course même quand Android l'a mise en veille ou arrêtée.
CREATE TABLE delivery.push_token (
    token       VARCHAR(512) PRIMARY KEY,
    user_id     UUID         NOT NULL,
    app_version VARCHAR(40),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);
CREATE INDEX push_token_user_idx ON delivery.push_token (user_id);
