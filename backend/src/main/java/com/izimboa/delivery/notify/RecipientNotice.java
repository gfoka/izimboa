package com.izimboa.delivery.notify;

import java.util.UUID;

/**
 * Une étape qui mérite un message WhatsApp au destinataire. Publiée par la livraison pendant sa transaction,
 * envoyée seulement après l'enregistrement (jamais pour une étape annulée).
 *
 * @param area      quartier écrit par le vendeur (adresse de livraison)
 * @param askDropoff le destinataire doit indiquer le lieu exact (décision 72)
 */
public record RecipientNotice(UUID deliveryId, Kind kind, String recipientPhone, String recipientName,
                              String packageDescription, String reference, long amountToPay, String area,
                              boolean askDropoff, String trackingToken) {

    public enum Kind { CREATED, IN_TRANSIT, DELIVERED }
}
