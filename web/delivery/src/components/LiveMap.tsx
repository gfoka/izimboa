"use client";

import { useEffect, useRef } from "react";
import type { LayerGroup, Map as LeafletMap, Marker, Polyline } from "leaflet";
import "leaflet/dist/leaflet.css";
import type { GeoPoint } from "@/lib/api";

export type LivePosition = { lat: number; lng: number; accuracyMeters: number | null; at: string };
export type TracePoint = { lat: number; lng: number; at: string };

/**
 * Carte de suivi : ramassage, livraison, chemin parcouru depuis le ramassage (trait orange)
 * et position du livreur en direct (mise à jour sans recharger).
 */
export function LiveMap({ pickup, dropoff, courier, trace = [] }: {
  pickup?: GeoPoint; dropoff?: GeoPoint; courier: LivePosition | null; trace?: TracePoint[];
}) {
  const box = useRef<HTMLDivElement>(null);
  const map = useRef<LeafletMap | null>(null);
  const courierMarker = useRef<Marker | null>(null);
  const path = useRef<Polyline | null>(null);
  const lib = useRef<typeof import("leaflet") | null>(null);
  const stops = useRef<LayerGroup | null>(null);
  const fitted = useRef(false);

  useEffect(() => {
    let cancelled = false;
    import("leaflet").then((L) => {
      if (cancelled || !box.current || map.current) return;
      lib.current = L;
      const m = L.map(box.current, { zoomControl: true, scrollWheelZoom: false }).setView([4.0511, 9.7679], 13);
      L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      }).addTo(m);
      stops.current = L.layerGroup().addTo(m);
      map.current = m;
      draw();
    });
    return () => {
      cancelled = true;
      map.current?.remove();
      map.current = null;
      courierMarker.current = null;
      path.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => { draw(); });

  function draw() {
    const L = lib.current;
    const m = map.current;
    if (!L || !m || !stops.current) return;
    stops.current.clearLayers();
    const pin = (color: string) => L.divIcon({ className: "pin", html: `<span style="--pin:${color}"></span>`, iconSize: [30, 40], iconAnchor: [15, 38] });
    const points: [number, number][] = [];
    if (pickup) { L.marker([pickup.lat, pickup.lng], { icon: pin("#0B2545"), title: "Ramassage" }).addTo(stops.current); points.push([pickup.lat, pickup.lng]); }
    if (dropoff) { L.marker([dropoff.lat, dropoff.lng], { icon: pin("#F28C28"), title: "Livraison" }).addTo(stops.current); points.push([dropoff.lat, dropoff.lng]); }
    // Chemin parcouru, jusqu'à la position actuelle du livreur.
    const line: [number, number][] = trace.map((p) => [p.lat, p.lng]);
    if (courier && line.length > 0) line.push([courier.lat, courier.lng]);
    if (line.length >= 2) {
      if (!path.current) path.current = L.polyline(line, { color: "#F28C28", weight: 5, opacity: 0.9, lineCap: "round", lineJoin: "round" }).addTo(m);
      else path.current.setLatLngs(line);
      line.forEach((p) => points.push(p));
    } else if (path.current) {
      path.current.remove();
      path.current = null;
    }
    if (courier) {
      const icon = L.divIcon({ className: "courier-pin", html: "<span></span>", iconSize: [26, 26], iconAnchor: [13, 13] });
      if (!courierMarker.current) courierMarker.current = L.marker([courier.lat, courier.lng], { icon, title: "Livreur", zIndexOffset: 1000 }).addTo(m);
      else courierMarker.current.setLatLng([courier.lat, courier.lng]);
      points.push([courier.lat, courier.lng]);
    } else if (courierMarker.current) {
      courierMarker.current.remove();
      courierMarker.current = null;
    }
    if (!fitted.current && points.length > 0) {
      if (points.length === 1) m.setView(points[0], 15);
      else m.fitBounds(L.latLngBounds(points), { padding: [36, 36], maxZoom: 16 });
      fitted.current = true;
    }
  }

  return <div ref={box} className="map map-live" role="img" aria-label="Carte du trajet et position du livreur" />;
}
