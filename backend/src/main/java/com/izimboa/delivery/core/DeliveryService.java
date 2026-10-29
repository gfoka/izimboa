package com.izimboa.delivery.core;

import com.izimboa.common.CurrentUser;
import com.izimboa.common.Phones;
import com.izimboa.common.Roles;
import com.izimboa.delivery.DeliveryProblem;
import com.izimboa.delivery.DeliveryProperties;
import com.izimboa.delivery.geo.GeoPoint;
import com.izimboa.delivery.pricing.PricingService;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cas d'usage d'une livraison : création au prix de la grille, puis chaque étape,
 * avec le contrôle de qui a le droit de faire quoi.
 */
@Service
class DeliveryService {

    static final int LIST_LIMIT = 100;

    private final DeliveryRepository repository;
    private final PricingService pricing;
    private final IdentifierGenerator identifiers;
    private final Clock clock;
    private final String trackingBaseUrl;
    private final DeliveryProperties properties;

    DeliveryService(DeliveryRepository repository, PricingService pricing, IdentifierGenerator identifiers,
                    Clock clock, DeliveryProperties properties) {
        this.repository = repository;
        this.pricing = pricing;
        this.identifiers = identifiers;
        this.clock = clock;
        this.trackingBaseUrl = properties.trackingBaseUrl();
        this.properties = properties;
    }

    // ---- Création ----

    record NewDelivery(GeoPoint pickup, String pickupAddress, String pickupLandmark, String pickupPhone,
                       GeoPoint dropoff, String dropoffAddress, String dropoffLandmark,
                       String recipientName, String recipientPhone, String packageDescription,
                       boolean express, long codAmount, boolean dropoffByClient) {}

    record Created(DeliveryViews.DeliveryView view, boolean replayed) {}

    /**
     * Crée une livraison. Avec la même clé d'idempotence, un deuxième envoi (réseau mobile instable)
     * renvoie la livraison déjà créée au lieu d'en créer une autre.
     */
    @Transactional
    Created create(NewDelivery in, String idempotencyKey, CurrentUser sender) {
        if (idempotencyKey != null) {
            var existing = repository.findBySenderIdAndIdempotencyKey(sender.id(), idempotencyKey);
            if (existing.isPresent()) return new Created(view(existing.get(), sender), true);
        }
        String pickupPhone = Phones.normalize(in.pickupPhone())
                .orElseThrow(() -> DeliveryProblem.invalid("Téléphone de ramassage invalide."));
        String recipientPhone = Phones.normalize(in.recipientPhone())
                .orElseThrow(() -> DeliveryProblem.invalid("Téléphone du destinataire invalide."));

        var quote = pricing.quote(in.pickup(), in.dropoff(), in.express(), in.codAmount());
        var details = new Delivery.Details(
                new Delivery.Stop(in.pickup().lat(), in.pickup().lng(), in.pickupAddress().strip(), blankToNull(in.pickupLandmark())),
                pickupPhone,
                new Delivery.Stop(in.dropoff().lat(), in.dropoff().lng(), in.dropoffAddress().strip(), blankToNull(in.dropoffLandmark())),
                in.recipientName().strip(), recipientPhone, blankToNull(in.packageDescription()),
                in.express(), in.codAmount());
        var delivery = Delivery.create(identifiers.next(), sender.id(), idempotencyKey, details, quote,
                in.dropoffByClient(), clock.instant());
        return new Created(view(repository.save(delivery), sender), false);
    }

    // ---- Lecture ----

    enum Scope { SENT, ASSIGNED, ALL }

    @Transactional(readOnly = true)
    List<DeliveryViews.DeliverySummary> list(CurrentUser user, Scope requested, DeliveryStatus status) {
        Scope scope = requested != null ? requested
                : user.isStaff() ? Scope.ALL
                : user.hasRole(Roles.LIVREUR) ? Scope.ASSIGNED
                : Scope.SENT;
        Specification<Delivery> spec = switch (scope) {
            case SENT -> (r, q, cb) -> cb.equal(r.get("senderId"), user.id());
            case ASSIGNED -> (r, q, cb) -> cb.equal(r.get("courierId"), user.id());
            case ALL -> {
                if (!user.isStaff()) throw DeliveryProblem.invalid("Vue réservée aux opérateurs.");
                yield (r, q, cb) -> cb.conjunction();
            }
        };
        if (status != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), status));
        var page = PageRequest.of(0, LIST_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findAll(spec, page).map(DeliveryViews::summary).getContent();
    }

    @Transactional(readOnly = true)
    DeliveryViews.DeliveryView get(UUID id, CurrentUser user) {
        return view(load(id, user), user);
    }

    @Transactional(readOnly = true)
    DeliveryViews.TrackingView track(String token) {
        return repository.findByTrackingToken(token).map(DeliveryViews::tracking)
                .orElseThrow(DeliveryProblem::notFound);
    }

    /**
     * Le destinataire indique le lieu exact depuis son lien de suivi (décision 72) : dans la ville du ramassage,
     * à 3 km au plus du quartier choisi par le vendeur, avant le ramassage.
     */
    @Transactional
    DeliveryViews.TrackingView confirmDropoff(String token, GeoPoint point, String landmark) {
        var d = repository.findByTrackingToken(token).orElseThrow(DeliveryProblem::notFound);
        var area = properties.areaOf(d.getPickupLat(), d.getPickupLng());
        if (area.isPresent() && !area.get().contains(point.lat(), point.lng())) {
            throw DeliveryProblem.outsideServiceArea(area.get().name());
        }
        d.setDropoffByClient(point.lat(), point.lng(), blankToNull(landmark), clock.instant());
        return DeliveryViews.tracking(d);
    }

    // ---- Étapes ----

    @Transactional
    DeliveryViews.DeliveryView assign(UUID id, UUID courierId, CurrentUser staff) {
        var d = load(id, staff);
        requireStaff(staff);
        d.assign(courierId, staff.id(), clock.instant());
        return view(d, staff);
    }

    @Transactional
    DeliveryViews.DeliveryView pickUp(UUID id, Delivery.Position at, CurrentUser user) {
        var d = loadForCourier(id, user);
        d.pickUp(user.id(), at, clock.instant());
        return view(d, user);
    }

    @Transactional
    DeliveryViews.DeliveryView depart(UUID id, Delivery.Position at, CurrentUser user) {
        var d = loadForCourier(id, user);
        d.depart(user.id(), at, clock.instant());
        return view(d, user);
    }

    /** Un code faux est enregistré (compteur d'essais) : pas d'annulation de la transaction. */
    @Transactional(noRollbackFor = DeliveryProblem.class)
    DeliveryViews.DeliveryView deliver(UUID id, String code, Delivery.Position at, CurrentUser user) {
        var d = loadForCourier(id, user);
        d.deliver(code == null ? "" : code.strip(), user.id(), at, clock.instant());
        return view(d, user);
    }

    @Transactional
    DeliveryViews.DeliveryView fail(UUID id, String reason, Delivery.Position at, CurrentUser user) {
        var d = loadForCourier(id, user);
        d.fail(reason.strip(), user.id(), at, clock.instant());
        return view(d, user);
    }

    @Transactional
    DeliveryViews.DeliveryView retry(UUID id, Delivery.Position at, CurrentUser user) {
        var d = loadForCourier(id, user);
        d.retry(user.id(), at, clock.instant());
        return view(d, user);
    }

    @Transactional
    DeliveryViews.DeliveryView returnToSender(UUID id, Delivery.Position at, CurrentUser user) {
        var d = loadForCourier(id, user);
        long fee = pricing.currentTariff().returnFee(d.courseFee());
        d.returnToSender(fee, user.id(), at, clock.instant());
        return view(d, user);
    }

    @Transactional
    DeliveryViews.DeliveryView cancel(UUID id, String reason, CurrentUser user) {
        var d = load(id, user);
        if (!user.isStaff() && !d.getSenderId().equals(user.id())) throw DeliveryProblem.notFound();
        d.cancel(blankToNull(reason), user.id(), clock.instant());
        return view(d, user);
    }

    @Transactional
    DeliveryViews.DeliveryView unlockHandover(UUID id, CurrentUser staff) {
        var d = load(id, staff);
        requireStaff(staff);
        d.unlockHandover(staff.id(), clock.instant());
        return view(d, staff);
    }

    // ---- Interne ----

    /** Une livraison que l'on n'a pas le droit de voir est « introuvable » (on ne révèle pas qu'elle existe). */
    private Delivery load(UUID id, CurrentUser user) {
        return repository.findById(id).filter(d -> d.isVisibleTo(user.id(), user.isStaff()))
                .orElseThrow(DeliveryProblem::notFound);
    }

    /** Actions du terrain : le livreur attribué, ou un opérateur qui corrige. */
    private Delivery loadForCourier(UUID id, CurrentUser user) {
        var d = load(id, user);
        if (!user.isStaff() && !user.id().equals(d.getCourierId())) throw DeliveryProblem.notFound();
        return d;
    }

    private static void requireStaff(CurrentUser user) {
        if (!user.isStaff()) throw DeliveryProblem.notFound();
    }

    /** Le code de remise est visible du vendeur et des opérateurs, jamais du livreur. */
    private DeliveryViews.DeliveryView view(Delivery d, CurrentUser user) {
        boolean showCode = user.isStaff() || d.getSenderId().equals(user.id());
        return DeliveryViews.full(d, trackingBaseUrl, showCode);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }
}
