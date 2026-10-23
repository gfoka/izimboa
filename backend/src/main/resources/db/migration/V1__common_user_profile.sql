-- Module commun : trace minimale des utilisateurs (l'identifiant vient de Keycloak).
CREATE TABLE common.user_profile (
    id             UUID        PRIMARY KEY,
    first_seen_at  TIMESTAMPTZ NOT NULL,
    last_seen_at   TIMESTAMPTZ NOT NULL
);
