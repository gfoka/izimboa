package com.izimboa.delivery.courier;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface CourierPositionRepository extends JpaRepository<CourierPosition, Long> {

    long countByCourierId(UUID courierId);

    /** Tracé d'un livreur sur une période, dans l'ordre (points envoyés en retard compris). */
    @Query("""
            select new com.izimboa.delivery.courier.CourierService$TracePoint(p.lat, p.lng, p.recordedAt)
            from CourierPosition p
            where p.courierId = :courierId and p.recordedAt between :from and :to
            order by p.recordedAt""")
    List<CourierService.TracePoint> trace(@Param("courierId") UUID courierId, @Param("from") Instant from, @Param("to") Instant to);
}
