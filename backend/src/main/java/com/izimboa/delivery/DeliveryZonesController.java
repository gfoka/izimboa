package com.izimboa.delivery;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Zones desservies par Izimboa Delivery (public). Données en dur tant qu'il n'y a qu'une ville. */
@RestController
@RequestMapping("/v1/delivery/zones")
class DeliveryZonesController {

    record Zone(String city, String country, String status) {}

    @GetMapping
    List<Zone> zones() {
        return List.of(new Zone("Douala", "CM", "coming_soon"));
    }
}
