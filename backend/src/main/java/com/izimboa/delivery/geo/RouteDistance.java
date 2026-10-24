package com.izimboa.delivery.geo;

/**
 * Port : distance par la route entre deux points.
 * Aujourd'hui une estimation ; demain un adaptateur vers notre moteur d'itinéraires OpenStreetMap
 * (Valhalla ou OSRM), sans rien changer au reste du code.
 */
public interface RouteDistance {

    int meters(GeoPoint from, GeoPoint to);
}
