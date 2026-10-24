package com.izimboa.delivery.core;

import com.izimboa.delivery.DeliveryProblem;
import com.izimboa.delivery.pricing.Quote;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Une livraison et ses règles : chaque changement d'étape passe par une méthode de cette classe,
 * qui vérifie que l'étape est permise et l'inscrit dans l'historique.
 */
@Entity
@Table(name = "delivery", schema = "delivery")
public class Delivery {

    /** Au-delà, le livreur ne peut plus saisir de code : un opérateur doit débloquer. */
    static final int MAX_HANDOVER_FAILURES = 5;
    /** Nombre de passages chez le destinataire avant retour obligatoire au vendeur. */
    static final int MAX_DELIVERY_ATTEMPTS = 2;

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 12)
    private String reference;

    @Column(name = "tracking_token", nullable = false, unique = true, length = 32)
    private String trackingToken;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "idempotency_key", length = 80)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(name = "pickup_lat", nullable = false) private double pickupLat;
    @Column(name = "pickup_lng", nullable = false) private double pickupLng;
    @Column(name = "pickup_address", nullable = false, length = 300) private String pickupAddress;
    @Column(name = "pickup_landmark", length = 300) private String pickupLandmark;
    @Column(name = "pickup_phone", nullable = false, length = 20) private String pickupPhone;

    @Column(name = "dropoff_lat", nullable = false) private double dropoffLat;
    @Column(name = "dropoff_lng", nullable = false) private double dropoffLng;
    @Column(name = "dropoff_address", nullable = false, length = 300) private String dropoffAddress;
    @Column(name = "dropoff_landmark", length = 300) private String dropoffLandmark;
    @Column(name = "recipient_name", nullable = false, length = 120) private String recipientName;
    @Column(name = "recipient_phone", nullable = false, length = 20) private String recipientPhone;

    @Column(name = "package_description", length = 200) private String packageDescription;
    @Column(nullable = false) private boolean express;
    @Column(name = "distance_m", nullable = false) private int distanceMeters;

    @Column(name = "base_price_xaf", nullable = false) private long basePrice;
    @Column(name = "express_surcharge_xaf", nullable = false) private long expressSurcharge;
    @Column(name = "cod_amount_xaf", nullable = false) private long codAmount;
    @Column(name = "cod_commission_xaf", nullable = false) private long codCommission;
    @Column(name = "total_fee_xaf", nullable = false) private long totalFee;
    @Enumerated(EnumType.STRING)
    @Column(name = "fee_payment", nullable = false, length = 20) private FeePayment feePayment;
    @Column(name = "return_fee_xaf") private Long returnFee;

    @Column(name = "handover_code", nullable = false, length = 4) private String handoverCode;
    @Column(name = "handover_failures", nullable = false) private short handoverFailures;
    @Column(name = "courier_id") private UUID courierId;
    @Column(name = "delivery_attempts", nullable = false) private short deliveryAttempts;

    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version private long version;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL)
    @OrderBy("occurredAt ASC, id ASC")
    private List<DeliveryEvent> events = new ArrayList<>();

    protected Delivery() {}

    /** Ce que le vendeur saisit. Téléphones déjà normalisés. */
    record Details(Stop pickup, String pickupPhone, Stop dropoff, String recipientName, String recipientPhone,
                   String packageDescription, boolean express, long codAmount) {}

    record Stop(double lat, double lng, String address, String landmark) {}

    /** Identifiants générés à la création (références, code de remise). */
    record Identifiers(UUID id, String reference, String trackingToken, String handoverCode) {}

    static Delivery create(Identifiers ids, UUID senderId, String idempotencyKey, Details d, Quote quote, Instant now) {
        if (quote.onRequest()) throw DeliveryProblem.quoteRequired();
        var x = new Delivery();
        x.id = ids.id();
        x.reference = ids.reference();
        x.trackingToken = ids.trackingToken();
        x.handoverCode = ids.handoverCode();
        x.senderId = senderId;
        x.idempotencyKey = idempotencyKey;
        x.pickupLat = d.pickup().lat();
        x.pickupLng = d.pickup().lng();
        x.pickupAddress = d.pickup().address();
        x.pickupLandmark = d.pickup().landmark();
        x.pickupPhone = d.pickupPhone();
        x.dropoffLat = d.dropoff().lat();
        x.dropoffLng = d.dropoff().lng();
        x.dropoffAddress = d.dropoff().address();
        x.dropoffLandmark = d.dropoff().landmark();
        x.recipientName = d.recipientName();
        x.recipientPhone = d.recipientPhone();
        x.packageDescription = d.packageDescription();
        x.express = d.express();
        x.distanceMeters = quote.distanceMeters();
        x.basePrice = quote.basePrice();
        x.expressSurcharge = quote.expressSurcharge();
        x.codAmount = d.codAmount();
        x.codCommission = quote.codCommission();
        x.totalFee = quote.total();
        // Le vendeur n'avance rien si l'argent encaissé couvre les frais (décision n° 36).
        x.feePayment = d.codAmount() >= quote.total() ? FeePayment.DEDUCTED_FROM_COD : FeePayment.PREPAID;
        x.createdAt = now;
        x.status = DeliveryStatus.CREATED;
        x.record(DeliveryStatus.CREATED, senderId, null, null, now);
        return x;
    }

    // ---- Changements d'étape ----

    /** Attribution (ou réattribution) à un livreur, avant le ramassage. */
    void assign(UUID courier, UUID actor, Instant now) {
        require("assign", DeliveryStatus.CREATED, DeliveryStatus.ASSIGNED);
        this.courierId = courier;
        moveTo(DeliveryStatus.ASSIGNED, actor, null, "Livreur " + courier, now);
    }

    void pickUp(UUID actor, Position at, Instant now) {
        require("pickup", DeliveryStatus.ASSIGNED);
        moveTo(DeliveryStatus.PICKED_UP, actor, at, null, now);
    }

    void depart(UUID actor, Position at, Instant now) {
        require("depart", DeliveryStatus.PICKED_UP);
        deliveryAttempts++;
        moveTo(DeliveryStatus.IN_TRANSIT, actor, at, null, now);
    }

    /**
     * Remise au destinataire, validée par son code. Un code faux est compté
     * (l'appelant doit enregistrer la livraison même en cas d'erreur).
     */
    void deliver(String code, UUID actor, Position at, Instant now) {
        require("deliver", DeliveryStatus.IN_TRANSIT);
        if (handoverFailures >= MAX_HANDOVER_FAILURES) throw DeliveryProblem.handoverLocked();
        if (!handoverCode.equals(code)) {
            handoverFailures++;
            updatedAt = now;
            if (handoverFailures >= MAX_HANDOVER_FAILURES) throw DeliveryProblem.handoverLocked();
            throw DeliveryProblem.wrongHandoverCode(MAX_HANDOVER_FAILURES - handoverFailures);
        }
        moveTo(DeliveryStatus.DELIVERED, actor, at, null, now);
    }

    /** Destinataire absent, injoignable ou refus : motif obligatoire. */
    void fail(String reason, UUID actor, Position at, Instant now) {
        require("fail", DeliveryStatus.IN_TRANSIT);
        moveTo(DeliveryStatus.FAILED, actor, at, reason, now);
    }

    void retry(UUID actor, Position at, Instant now) {
        require("retry", DeliveryStatus.FAILED);
        if (deliveryAttempts >= MAX_DELIVERY_ATTEMPTS) {
            throw DeliveryProblem.invalid("Nombre maximal de tentatives atteint : retour au vendeur.");
        }
        deliveryAttempts++;
        moveTo(DeliveryStatus.IN_TRANSIT, actor, at, "Nouvelle tentative", now);
    }

    /** Colis rapporté au vendeur : frais de retour facturés (décision n° 36). */
    void returnToSender(long fee, UUID actor, Position at, Instant now) {
        require("return", DeliveryStatus.FAILED);
        this.returnFee = fee;
        moveTo(DeliveryStatus.RETURNED, actor, at, null, now);
    }

    /** Annulation gratuite tant que le colis n'est pas ramassé. */
    void cancel(String reason, UUID actor, Instant now) {
        require("cancel", DeliveryStatus.CREATED, DeliveryStatus.ASSIGNED);
        moveTo(DeliveryStatus.CANCELLED, actor, null, reason, now);
    }

    void unlockHandover(UUID actor, Instant now) {
        this.handoverFailures = 0;
        record(status, actor, null, "Code de remise débloqué", now);
    }

    // ---- Interne ----

    record Position(Double lat, Double lng) {}

    private void require(String action, DeliveryStatus... allowed) {
        for (var s : allowed) if (status == s) return;
        throw DeliveryProblem.invalidTransition(status.name(), action);
    }

    private void moveTo(DeliveryStatus next, UUID actor, Position at, String note, Instant now) {
        this.status = next;
        record(next, actor, at, note, now);
    }

    private void record(DeliveryStatus s, UUID actor, Position at, String note, Instant now) {
        events.add(new DeliveryEvent(this, s, actor, at == null ? null : at.lat(), at == null ? null : at.lng(), note, now));
        this.updatedAt = now;
    }

    // ---- Lecture ----

    boolean isVisibleTo(UUID userId, boolean staff) {
        return staff || senderId.equals(userId) || userId.equals(courierId);
    }

    public UUID getId() { return id; }
    public String getReference() { return reference; }
    public String getTrackingToken() { return trackingToken; }
    public UUID getSenderId() { return senderId; }
    public DeliveryStatus getStatus() { return status; }
    public double getPickupLat() { return pickupLat; }
    public double getPickupLng() { return pickupLng; }
    public String getPickupAddress() { return pickupAddress; }
    public String getPickupLandmark() { return pickupLandmark; }
    public String getPickupPhone() { return pickupPhone; }
    public double getDropoffLat() { return dropoffLat; }
    public double getDropoffLng() { return dropoffLng; }
    public String getDropoffAddress() { return dropoffAddress; }
    public String getDropoffLandmark() { return dropoffLandmark; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public String getPackageDescription() { return packageDescription; }
    public boolean isExpress() { return express; }
    public int getDistanceMeters() { return distanceMeters; }
    public long getBasePrice() { return basePrice; }
    public long getExpressSurcharge() { return expressSurcharge; }
    public long getCodAmount() { return codAmount; }
    public long getCodCommission() { return codCommission; }
    public long getTotalFee() { return totalFee; }
    public FeePayment getFeePayment() { return feePayment; }
    public Long getReturnFee() { return returnFee; }
    String getHandoverCode() { return handoverCode; }
    public int getHandoverFailures() { return handoverFailures; }
    public UUID getCourierId() { return courierId; }
    public int getDeliveryAttempts() { return deliveryAttempts; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    List<DeliveryEvent> getEvents() { return Collections.unmodifiableList(events); }

    /** Course seule (hors commission), base des frais de retour. */
    long courseFee() { return basePrice + expressSurcharge; }

    /** Ce qui revient au vendeur une fois livré : encaissé moins les frais s'ils y sont retenus. */
    public long sellerPayout() {
        return feePayment == FeePayment.DEDUCTED_FROM_COD ? codAmount - totalFee : codAmount;
    }
}
