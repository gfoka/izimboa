-- Demandes envoyées depuis les formulaires du site (entreprises qui veulent faire livrer, partenaires…).
CREATE TABLE common.contact_request (
    id           UUID         PRIMARY KEY,
    created_at   TIMESTAMPTZ  NOT NULL,
    product      VARCHAR(20)  NOT NULL,
    kind         VARCHAR(20)  NOT NULL,
    company      VARCHAR(120) NOT NULL,
    contact_name VARCHAR(120) NOT NULL,
    phone        VARCHAR(30)  NOT NULL,
    email        VARCHAR(160),
    city         VARCHAR(60),
    activity     VARCHAR(120),
    volume       VARCHAR(20),
    message      VARCHAR(2000),
    lang         VARCHAR(5)   NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'NEW'
);
CREATE INDEX contact_request_created_idx ON common.contact_request (created_at DESC);
