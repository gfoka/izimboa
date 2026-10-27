package com.izimboa.delivery.courier;

import com.izimboa.delivery.DeliveryProblem;
import com.izimboa.delivery.core.DeliveryAssignments;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Présence et positions des livreurs. On n'enregistre la position que lorsque le livreur est en ligne
 * (il en est informé dans l'application, décision n° 42).
 */
@Service
public class CourierService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CourierService.class);

    /** Une position plus ancienne ne sert ni à l'attribution ni au suivi. */
    public static final Duration FRESH = Duration.ofMinutes(2);
    /** Points d'un téléphone trop en avance sur l'heure du serveur : refusés. */
    private static final Duration CLOCK_SKEW = Duration.ofMinutes(5);
    /** Points trop anciens (téléphone resté hors réseau des heures) : ignorés. */
    private static final Duration MAX_AGE = Duration.ofHours(24);

    public record Position(double lat, double lng, Float accuracyMeters, Instant at) {}

    public record OnlineCourier(UUID courierId, Position position) {}

    public record Point(double lat, double lng, Float accuracyMeters, Float speedMps, Float headingDeg, Instant recordedAt) {}

    public record Status(boolean online, Instant onlineSince, Position lastPosition) {}

    public record TracePoint(double lat, double lng, Instant at) {}

    /** Au-delà, on garde un point sur n : la carte reste légère sur les téléphones et les petits forfaits. */
    static final int MAX_TRACE_POINTS = 500;

    private final CourierPresenceRepository presences;
    private final CourierPositionRepository positions;
    private final DeliveryAssignments deliveries;
    private final Clock clock;

    CourierService(CourierPresenceRepository presences, CourierPositionRepository positions,
                   DeliveryAssignments deliveries, Clock clock) {
        this.presences = presences;
        this.positions = positions;
        this.deliveries = deliveries;
        this.clock = clock;
    }

    @Transactional
    public Status goOnline(UUID courierId, Point at) {
        Instant now = clock.instant();
        var p = presences.findById(courierId).orElseGet(() -> presences.save(new CourierPresence(courierId, now)));
        p.goOnline(now);
        if (at != null) record(p, List.of(at), now);
        return status(p);
    }

    @Transactional
    public Status goOffline(UUID courierId) {
        Instant now = clock.instant();
        var p = presences.findById(courierId).orElseGet(() -> presences.save(new CourierPresence(courierId, now)));
        p.goOffline(now);
        return status(p);
    }

    /** Points envoyés par le téléphone (groupés après une coupure réseau). Retourne le nombre enregistré. */
    @Transactional
    public int report(UUID courierId, List<Point> points) {
        var p = presences.findById(courierId).filter(CourierPresence::online)
                .orElseThrow(() -> DeliveryProblem.invalid("Passez en ligne avant d'envoyer votre position."));
        return record(p, points, clock.instant());
    }

    @Transactional(readOnly = true)
    public Status status(UUID courierId) {
        return presences.findById(courierId).map(this::status).orElse(new Status(false, null, null));
    }

    /** Livreurs en ligne avec une position récente : candidats à l'attribution. */
    @Transactional(readOnly = true)
    public List<OnlineCourier> onlineWithFreshPosition() {
        Instant limit = clock.instant().minus(FRESH);
        return presences.findByOnlineTrue().stream()
                .filter(p -> p.positionAt() != null && p.positionAt().isAfter(limit))
                .map(p -> new OnlineCourier(p.courierId(), position(p)))
                .toList();
    }

    /** Position récente d'un livreur en ligne (suivi en direct), sinon rien. */
    @Transactional(readOnly = true)
    public Optional<Position> livePosition(UUID courierId) {
        Instant limit = clock.instant().minus(FRESH);
        return presences.findById(courierId)
                .filter(p -> p.online() && p.positionAt() != null && p.positionAt().isAfter(limit))
                .map(this::position);
    }

    /** Chemin parcouru par un livreur entre deux instants, allégé si besoin. */
    @Transactional(readOnly = true)
    public List<TracePoint> trace(UUID courierId, Instant from, Instant to) {
        var all = positions.trace(courierId, from, to);
        if (all.size() <= MAX_TRACE_POINTS) return all;
        int step = (int) Math.ceil(all.size() / (double) MAX_TRACE_POINTS);
        var thinned = new java.util.ArrayList<TracePoint>();
        for (int i = 0; i < all.size(); i += step) thinned.add(all.get(i));
        if (!thinned.getLast().equals(all.getLast())) thinned.add(all.getLast());
        return thinned;
    }

    private int record(CourierPresence p, List<Point> points, Instant now) {
        UUID deliveryId = deliveries.activeFor(p.courierId()).map(DeliveryAssignments.Active::deliveryId).orElse(null);
        int saved = 0;
        int clamped = 0, dropped = 0;
        for (var pt : points) {
            Instant at = pt.recordedAt();
            if (at.isBefore(now.minus(MAX_AGE))) { dropped++; continue; }
            // Horloge du téléphone en avance (réglage manuel) : la position reste juste, on prend l'heure de réception.
            if (at.isAfter(now.plus(CLOCK_SKEW))) { at = now; clamped++; }
            positions.save(new CourierPosition(p.courierId(), deliveryId, pt.lat(), pt.lng(), pt.accuracyMeters(),
                    pt.speedMps(), pt.headingDeg(), at, now));
            p.moveTo(pt.lat(), pt.lng(), pt.accuracyMeters(), at, now);
            saved++;
        }
        if (clamped + dropped > 0) {
            log.warn("Livreur {} : {} point(s) à l'heure corrigée, {} trop ancien(s) ignoré(s)", p.courierId(), clamped, dropped);
        }
        return saved;
    }

    private Status status(CourierPresence p) {
        return new Status(p.online(), p.onlineSince(), p.positionAt() == null ? null : position(p));
    }

    private Position position(CourierPresence p) {
        return new Position(p.lat(), p.lng(), p.accuracyMeters(), p.positionAt());
    }
}
