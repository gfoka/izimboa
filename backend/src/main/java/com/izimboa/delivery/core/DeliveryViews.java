package com.izimboa.delivery.core;

import com.izimboa.delivery.notify.RecipientNotifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Formats renvoyés par l'API. Le code de remise n'est jamais montré au livreur. */
final class DeliveryViews {

    private DeliveryViews() {}

    record StopView(double lat, double lng, String address, String landmark) {}

    record PricingView(String currency, long basePrice, long expressSurcharge, long codAmount, long codCommission,
                       long totalFee, FeePayment feePayment, Long returnFee, long sellerPayout) {}

    record EventView(DeliveryStatus status, Instant at, String note) {}

    record DeliveryView(UUID id, String reference, DeliveryStatus status, String trackingUrl,
                        StopView pickup, String pickupPhone, StopView dropoff, String recipientName,
                        String recipientPhone, String packageDescription, boolean express, BigDecimal distanceKm,
                        PricingView pricing, String handoverCode, int handoverFailures, UUID courierId,
                        int deliveryAttempts, Instant createdAt, Instant updatedAt, List<EventView> timeline,
                        String dropoffState, List<RecipientNotifier.NoticeView> recipientNotices) {}

    record DeliverySummary(UUID id, String reference, DeliveryStatus status, String dropoffAddress,
                           String recipientName, boolean express, long codAmount, long totalFee,
                           UUID courierId, Instant createdAt, Instant updatedAt, String dropoffState) {}

    /**
     * Suivi public (lien WhatsApp du destinataire) : aucune donnée personnelle. Le code de remise y figure tant que
     * le colis n'est pas remis : le lien n'est envoyé qu'au destinataire, jamais au livreur (décision 73).
     */
    record TrackingView(String reference, DeliveryStatus status, boolean express, String dropoffLandmark,
                        long amountToPay, String currency, List<EventView> timeline, DropoffRequest dropoffRequest,
                        String handoverCode) {}

    /**
     * Pour le destinataire qui doit indiquer le lieu (sinon null) : le point actuel (quartier du vendeur ou lieu
     * déjà indiqué), le quartier écrit par le vendeur, et s'il peut encore le modifier.
     */
    record DropoffRequest(String state, boolean editable, double lat, double lng, String area, int maxShiftMeters) {}

    /**
     * {@code forSender} : vendeur ou opérateur. Le livreur ne voit ni le code de remise, ni le lien de suivi
     * (qui l'affiche), ni les messages envoyés au destinataire.
     */
    static DeliveryView full(Delivery d, String trackingBaseUrl, boolean forSender, List<RecipientNotifier.NoticeView> notices) {
        boolean showHandoverCode = forSender;
        return new DeliveryView(d.getId(), d.getReference(), d.getStatus(),
                forSender ? trackingBaseUrl + d.getTrackingToken() : null,
                new StopView(d.getPickupLat(), d.getPickupLng(), d.getPickupAddress(), d.getPickupLandmark()),
                d.getPickupPhone(),
                new StopView(d.getDropoffLat(), d.getDropoffLng(), d.getDropoffAddress(), d.getDropoffLandmark()),
                d.getRecipientName(), d.getRecipientPhone(), d.getPackageDescription(), d.isExpress(),
                km(d.getDistanceMeters()),
                new PricingView("XAF", d.getBasePrice(), d.getExpressSurcharge(), d.getCodAmount(), d.getCodCommission(),
                        d.getTotalFee(), d.getFeePayment(), d.getReturnFee(), d.sellerPayout()),
                showHandoverCode ? d.getHandoverCode() : null, d.getHandoverFailures(), d.getCourierId(),
                d.getDeliveryAttempts(), d.getCreatedAt(), d.getUpdatedAt(), timeline(d), d.dropoffState(),
                forSender ? notices : List.of());
    }

    static DeliverySummary summary(Delivery d) {
        return new DeliverySummary(d.getId(), d.getReference(), d.getStatus(), d.getDropoffAddress(),
                d.getRecipientName(), d.isExpress(), d.getCodAmount(), d.getTotalFee(), d.getCourierId(),
                d.getCreatedAt(), d.getUpdatedAt(), d.dropoffState());
    }

    static TrackingView tracking(Delivery d) {
        DropoffRequest request = d.isDropoffByClient()
                ? new DropoffRequest(d.dropoffState(), d.isDropoffEditableByClient(), d.getDropoffLat(), d.getDropoffLng(),
                        d.getDropoffAddress(), (int) Delivery.MAX_CLIENT_DROPOFF_SHIFT_M)
                : null;
        return new TrackingView(d.getReference(), d.getStatus(), d.isExpress(), d.getDropoffLandmark(),
                d.getCodAmount(), "XAF", timeline(d), request, d.getStatus().isFinal() ? null : d.getHandoverCode());
    }

    private static List<EventView> timeline(Delivery d) {
        return d.getEvents().stream().map(e -> new EventView(e.status(), e.occurredAt(), publicNote(e))).toList();
    }

    /** Les notes internes d'attribution (identifiant du livreur) ne sortent pas. */
    private static String publicNote(DeliveryEvent e) {
        return e.status() == DeliveryStatus.ASSIGNED ? null : e.note();
    }

    private static BigDecimal km(int meters) {
        return BigDecimal.valueOf(meters).movePointLeft(3).setScale(1, RoundingMode.HALF_UP);
    }
}
