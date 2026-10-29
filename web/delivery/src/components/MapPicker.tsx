"use client";

import { useEffect, useRef, useState } from "react";
import type { Map as LeafletMap, Marker } from "leaflet";
import "leaflet/dist/leaflet.css";
import type { GeoPoint } from "@/lib/api";
import { DOUALA } from "@/lib/config";

/**
 * Carte pour épingler une adresse : un appui pose l'épingle, on peut la glisser, ou utiliser sa position.
 * Fond de carte OpenStreetMap (sera remplacé par nos propres tuiles, lot 5).
 */
export function MapPicker({ value, onChange, color, label, center, hint }: {
  value: GeoPoint | null;
  onChange: (p: GeoPoint) => void;
  color: string;
  label: string;
  /** Centre de départ quand aucune épingle n'est posée (ex. le quartier du client). */
  center?: GeoPoint;
  /** Texte d'aide à la place de « Touchez la carte… ». */
  hint?: string;
}) {
  const box = useRef<HTMLDivElement>(null);
  const map = useRef<LeafletMap | null>(null);
  const marker = useRef<Marker | null>(null);
  const change = useRef(onChange);
  change.current = onChange;
  const [locating, setLocating] = useState(false);
  const [geoError, setGeoError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    import("leaflet").then((L) => {
      if (cancelled || !box.current || map.current) return;
      const m = L.map(box.current, { zoomControl: true, attributionControl: true })
        .setView([value?.lat ?? center?.lat ?? DOUALA.lat, value?.lng ?? center?.lng ?? DOUALA.lng], value ? 16 : center ? 15 : 13);
      L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      }).addTo(m);
      const icon = L.divIcon({
        className: "pin",
        html: `<span style="--pin:${color}"></span>`,
        iconSize: [30, 40],
        iconAnchor: [15, 38],
      });
      const place = (lat: number, lng: number) => {
        if (!marker.current) {
          marker.current = L.marker([lat, lng], { draggable: true, icon, keyboard: true, title: label }).addTo(m);
          marker.current.on("dragend", () => {
            const p = marker.current!.getLatLng();
            change.current({ lat: round(p.lat), lng: round(p.lng) });
          });
        } else {
          marker.current.setLatLng([lat, lng]);
        }
      };
      if (value) place(value.lat, value.lng);
      m.on("click", (e) => {
        place(e.latlng.lat, e.latlng.lng);
        change.current({ lat: round(e.latlng.lat), lng: round(e.latlng.lng) });
      });
      (m as LeafletMap & { _place?: typeof place })._place = place;
      map.current = m;
    });
    return () => {
      cancelled = true;
      map.current?.remove();
      map.current = null;
      marker.current = null;
    };
    // La carte est créée une seule fois ; la valeur initiale suffit.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Épingle placée depuis l'extérieur (localisation collée, lieu cherché) : on la montre et on recentre.
  useEffect(() => {
    const m = map.current as (LeafletMap & { _place?: (lat: number, lng: number) => void }) | null;
    if (!m || !value) return;
    const cur = marker.current?.getLatLng();
    if (cur && Math.abs(cur.lat - value.lat) < 1e-6 && Math.abs(cur.lng - value.lng) < 1e-6) return;
    m._place?.(value.lat, value.lng);
    m.setView([value.lat, value.lng], Math.max(m.getZoom(), 16), { animate: false });
  }, [value]);

  const locate = () => {
    if (!navigator.geolocation) return setGeoError("Votre appareil ne donne pas sa position.");
    setLocating(true);
    setGeoError(null);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLocating(false);
        const p = { lat: round(pos.coords.latitude), lng: round(pos.coords.longitude) };
        const m = map.current as (LeafletMap & { _place?: (lat: number, lng: number) => void }) | null;
        m?._place?.(p.lat, p.lng);
        m?.setView([p.lat, p.lng], 17);
        change.current(p);
      },
      () => {
        setLocating(false);
        setGeoError("Position indisponible. Touchez la carte pour placer l'épingle.");
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 30000 },
    );
  };

  return (
    <div className="map-picker">
      <div ref={box} className="map" role="application" aria-label={`Carte : ${label}`} />
      <div className="map-bar">
        <span className={value ? "map-hint ok" : "map-hint"}>
          {value ? "Épingle placée — glissez-la pour ajuster" : hint ?? "Touchez la carte pour placer l'épingle"}
        </span>
        <button type="button" className="btn btn-outline btn-xs" onClick={locate} disabled={locating}>
          {locating ? "Localisation…" : "Ma position"}
        </button>
      </div>
      {geoError && <p className="field-error">{geoError}</p>}
    </div>
  );
}

const round = (n: number) => Math.round(n * 1e6) / 1e6;
