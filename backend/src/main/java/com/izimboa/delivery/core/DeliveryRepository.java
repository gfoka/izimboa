package com.izimboa.delivery.core;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DeliveryRepository extends JpaRepository<Delivery, UUID>, JpaSpecificationExecutor<Delivery> {

    Optional<Delivery> findBySenderIdAndIdempotencyKey(UUID senderId, String idempotencyKey);

    Optional<Delivery> findByTrackingToken(String trackingToken);

    List<Delivery> findByStatus(DeliveryStatus status, Sort sort);

    Optional<Delivery> findFirstByCourierIdAndStatusIn(UUID courierId, Collection<DeliveryStatus> statuses);

    @Query("select distinct d.courierId from Delivery d where d.courierId is not null and d.status in :statuses")
    List<UUID> findBusyCourierIds(@Param("statuses") Collection<DeliveryStatus> statuses);
}
