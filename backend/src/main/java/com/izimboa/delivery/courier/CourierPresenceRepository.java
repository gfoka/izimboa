package com.izimboa.delivery.courier;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CourierPresenceRepository extends JpaRepository<CourierPresence, UUID> {

    List<CourierPresence> findByOnlineTrue();
}
