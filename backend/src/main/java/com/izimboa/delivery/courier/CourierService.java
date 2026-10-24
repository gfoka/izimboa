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

    private int record(CourierPresence p, List<Point> points, Instant now) {
        UUID deliveryId = deliveries.activeFor(p.courierId()).map(DeliveryAssignments.Active::deliveryId).orElse(null);
        int saved = 0;
        for (var pt : points) {
            if (pt.recordedAt().isAfter(now.plus(CLOCK_SKEW)) || pt.recordedAt().isBefore(now.minus(MAX_AGE))) continue;
            positions.save(new CourierPosition(p.courierId(), deliveryId, pt.lat(), pt.lng(), pt.accuracyMeters(),
                    pt.speedMps(), pt.headingDeg(), pt.recordedAt(), now));
            p.moveTo(pt.lat(), pt.lng(), pt.accuracyMeters(), pt.recordedAt(), now);
            saved++;
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
