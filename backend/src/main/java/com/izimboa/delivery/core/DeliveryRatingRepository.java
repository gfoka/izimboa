package com.izimboa.delivery.core;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DeliveryRatingRepository extends JpaRepository<DeliveryRating, UUID> {

    /** Par livreur : nombre de notes et moyenne. */
    @Query("""
        select d.courierId, count(r), avg(r.stars) from DeliveryRating r, Delivery d
        where d.id = r.deliveryId and d.courierId in :couriers group by d.courierId""")
    List<Object[]> statsByCourier(@Param("couriers") Collection<UUID> couriers);
}
