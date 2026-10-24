package com.izimboa.delivery.courier;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CourierPositionRepository extends JpaRepository<CourierPosition, Long> {

    long countByCourierId(UUID courierId);
}
