package com.izimboa.delivery.dispatch;

import java.util.UUID;

/** Une course vient d'être proposée à un livreur (publié dans la transaction de l'attribution). */
public record OfferMade(UUID offerId, UUID courierId, int secondsLeft) {}
