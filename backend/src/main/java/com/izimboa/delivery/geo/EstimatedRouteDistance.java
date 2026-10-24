package com.izimboa.delivery.geo;

import com.izimboa.delivery.DeliveryProperties;
import org.springframework.stereotype.Component;

/** Estimation : vol d'oiseau × facteur de détour (réglable dans application.yml). */
@Component
class EstimatedRouteDistance implements RouteDistance {

    private final double roadFactor;

    EstimatedRouteDistance(DeliveryProperties properties) {
        this.roadFactor = properties.roadFactor();
    }

    @Override
    public int meters(GeoPoint from, GeoPoint to) {
        return (int) Math.round(from.straightLineMetersTo(to) * roadFactor);
    }
}
