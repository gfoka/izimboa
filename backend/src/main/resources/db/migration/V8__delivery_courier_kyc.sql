-- Vérification d'identité des livreurs (décision 74).
-- Modèle de documents paramétrable par l'administration : chaque document a un nom, un code, des pages
-- (« Recto », « Verso », « Photo de vous »), et des réglages (obligatoire, numéro, date de validité).

CREATE TABLE delivery.kyc_document_type (
    id                  SERIAL       PRIMARY KEY,
    code                VARCHAR(40)  NOT NULL UNIQUE,
    name                VARCHAR(80)  NOT NULL,
    help                VARCHAR(300),
    mandatory           BOOLEAN      NOT NULL,
    -- NONE : pas de numéro ; OPTIONAL : numéro facultatif ; REQUIRED : numéro obligatoire
    number_requirement  VARCHAR(10)  NOT NULL CHECK (number_requirement IN ('NONE', 'OPTIONAL', 'REQUIRED')),
    expiry_required     BOOLEAN      NOT NULL,
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    position            INTEGER      NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL
);

-- Pages à photographier, dans l'ordre. camera : BACK (document) ou FRONT (selfie).
CREATE TABLE delivery.kyc_document_page (
    id        SERIAL      PRIMARY KEY,
    type_id   INTEGER     NOT NULL REFERENCES delivery.kyc_document_type (id) ON DELETE CASCADE,
    position  INTEGER     NOT NULL,
    name      VARCHAR(60) NOT NULL,
    camera    VARCHAR(5)  NOT NULL CHECK (camera IN ('BACK', 'FRONT'))
);

-- Dossier d'un livreur (identifiant Keycloak). status : DRAFT (en cours de saisie), PENDING (à vérifier),
-- ACTIVE (validé), REJECTED (refusé, peut corriger et renvoyer), SUSPENDED (suspendu).
CREATE TABLE delivery.courier_profile (
    user_id       UUID          PRIMARY KEY,
    phone         VARCHAR(20),
    first_name    VARCHAR(60),
    last_name     VARCHAR(60),
    email         VARCHAR(120),
    status        VARCHAR(10)   NOT NULL CHECK (status IN ('DRAFT', 'PENDING', 'ACTIVE', 'REJECTED', 'SUSPENDED')),
    status_reason VARCHAR(300),
    submitted_at  TIMESTAMPTZ,
    reviewed_at   TIMESTAMPTZ,
    reviewed_by   UUID,
    created_at    TIMESTAMPTZ   NOT NULL,
    updated_at    TIMESTAMPTZ   NOT NULL
);
CREATE INDEX courier_profile_status ON delivery.courier_profile (status, updated_at);

-- Un document fourni par un livreur (le dernier envoi remplace le précédent).
CREATE TABLE delivery.courier_document (
    id           UUID         PRIMARY KEY,
    user_id      UUID         NOT NULL REFERENCES delivery.courier_profile (user_id),
    type_id      INTEGER      NOT NULL REFERENCES delivery.kyc_document_type (id),
    number       VARCHAR(60),
    expires_on   DATE,
    -- false : envoyé ou remplacé depuis la dernière vérification
    reviewed     BOOLEAN      NOT NULL DEFAULT FALSE,
    submitted_at TIMESTAMPTZ  NOT NULL,
    UNIQUE (user_id, type_id)
);
CREATE INDEX courier_document_number ON delivery.courier_document (type_id, number);

-- Photos des pages (JPEG réduit par l'application, quelques centaines de Ko). Sauvegardées avec la base.
CREATE TABLE delivery.courier_document_image (
    document_id   UUID         NOT NULL REFERENCES delivery.courier_document (id) ON DELETE CASCADE,
    page_id       INTEGER      NOT NULL REFERENCES delivery.kyc_document_page (id) ON DELETE CASCADE,
    content_type  VARCHAR(30)  NOT NULL,
    data          BYTEA        NOT NULL,
    uploaded_at   TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (document_id, page_id)
);

-- Modèle de départ : la CNI (recto, verso, numéro, date d'expiration) et un selfie.
INSERT INTO delivery.kyc_document_type (code, name, help, mandatory, number_requirement, expiry_required, position, created_at, updated_at)
VALUES ('CNI', 'Carte nationale d''identité', 'Photographiez votre CNI en entier, bien lisible, sans reflet.', TRUE, 'REQUIRED', TRUE, 1, now(), now()),
       ('SELFIE', 'Photo de vous', 'Visage bien éclairé, sans lunettes de soleil ni casque.', TRUE, 'NONE', FALSE, 2, now(), now());
INSERT INTO delivery.kyc_document_page (type_id, position, name, camera)
SELECT id, 1, 'Recto', 'BACK' FROM delivery.kyc_document_type WHERE code = 'CNI'
UNION ALL SELECT id, 2, 'Verso', 'BACK' FROM delivery.kyc_document_type WHERE code = 'CNI'
UNION ALL SELECT id, 1, 'Photo de vous', 'FRONT' FROM delivery.kyc_document_type WHERE code = 'SELFIE';
