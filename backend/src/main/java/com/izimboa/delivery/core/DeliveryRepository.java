package com.izimboa.delivery.core;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface DeliveryRepository extends JpaRepository<Delivery, UUID>, JpaSpecificationExecutor<Delivery> {

    Optional<Delivery> findBySenderIdAndIdempotencyKey(UUID senderId, String idempotencyKey);

    Optional<Delivery> findByTrackingToken(String trackingToken);
}
