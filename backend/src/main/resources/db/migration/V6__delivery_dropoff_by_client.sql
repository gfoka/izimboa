-- Lieu de livraison précisé par le destinataire depuis son lien de suivi (décision 72).
-- Le vendeur place l'épingle sur le quartier ; le client indique le lieu exact, jusqu'au ramassage,
-- à 3 km au plus du point du vendeur (le prix, calculé à la création, ne change pas).
ALTER TABLE delivery.delivery
    ADD COLUMN dropoff_by_client      BOOLEAN          NOT NULL DEFAULT FALSE,
    ADD COLUMN dropoff_initial_lat    DOUBLE PRECISION,
    ADD COLUMN dropoff_initial_lng    DOUBLE PRECISION,
    ADD COLUMN dropoff_confirmed_at   TIMESTAMPTZ,
    ADD COLUMN dropoff_client_updates SMALLINT         NOT NULL DEFAULT 0;
