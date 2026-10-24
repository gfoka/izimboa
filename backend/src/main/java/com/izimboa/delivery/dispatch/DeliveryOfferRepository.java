package com.izimboa.delivery.dispatch;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DeliveryOfferRepository extends JpaRepository<DeliveryOffer, UUID> {

    List<DeliveryOffer> findByStatus(DeliveryOffer.Status status);

    Optional<DeliveryOffer> findFirstByCourierIdAndStatus(UUID courierId, DeliveryOffer.Status status);

    /** Livreurs qui ont déjà eu cette course (refusée ou laissée passer) : on ne la leur repropose pas. */
    @Query("select o.courierId from DeliveryOffer o where o.deliveryId = :deliveryId and o.status in :statuses")
    List<UUID> couriersWhoPassed(@Param("deliveryId") UUID deliveryId, @Param("statuses") Collection<DeliveryOffer.Status> statuses);

    List<DeliveryOffer> findByStatusAndExpiresAtBefore(DeliveryOffer.Status status, Instant limit);
}
