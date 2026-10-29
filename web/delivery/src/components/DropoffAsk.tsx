"use client";

import { useState } from "react";
import { MapPicker } from "@/components/MapPicker";
import { PlaceSearchBox } from "@/components/PlaceSearchBox";
import { api, ApiError, type GeoPoint, type Tracking } from "@/lib/api";

type Mode = "gps" | "search" | "map";

/**
 * « Où devons-nous livrer ? » dans le lien de suivi du destinataire (décision 72) : il est sur place (GPS),
 * il cherche le lieu, ou il place l'épingle. Possible jusqu'au ramassage du colis.
 */
export function DropoffAsk({ token, request, onSaved }: {
  token: string;
  request: NonNullable<Tracking["dropoffRequest"]>;
  onSaved: (t: Tracking) => void;
}) {
  const confirmed = request.state === "CONFIRMED";
  const [open, setOpen] = useState(!confirmed);
  const [mode, setMode] = useState<Mode | null>(null);
  const [point, setPoint] = useState<GeoPoint | null>(null);
  const [landmark, setLandmark] = useState("");
  const [locating, setLocating] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);
  const area: GeoPoint = { lat: request.lat, lng: request.lng };

  const useGps = () => {
    setMode("gps");
    setError(null);
    if (!navigator.geolocation) return setError("Votre téléphone ne donne pas sa position. Cherchez le lieu ou placez l'épingle.");
    setLocating(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLocating(false);
        setPoint({ lat: round(pos.coords.latitude), lng: round(pos.coords.longitude) });
      },
      () => {
        setLocating(false);
        setError("Position indisponible : autorisez la localisation, ou placez l'épingle sur la carte.");
      },
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 },
    );
  };

  const save = async () => {
    if (!point) return;
    setBusy(true);
    setError(null);
    try {
      const t = await api<Tracking>(`/v1/delivery/track/${encodeURIComponent(token)}/dropoff`, {
        method: "POST",
        body: { lat: point.lat, lng: point.lng, landmark: landmark.trim() || undefined },
      });
      setSaved(true);
      setOpen(false);
      setMode(null);
      onSaved(t);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Enregistrement impossible. Réessayez.");
    } finally {
      setBusy(false);
    }
  };

  if (!open) {
    return (
      <section className="card dropoff-ask done" aria-live="polite">
        <h2 className="card-title">✓ Lieu de livraison enregistré</h2>
        <p className="muted">
          {saved ? "Merci ! Le livreur viendra à ce lieu." : "Le livreur viendra au lieu que vous avez indiqué."}
          {request.editable && " Vous pouvez le modifier jusqu'au ramassage du colis."}
        </p>
        {request.editable && (
          <button type="button" className="btn btn-outline btn-sm" onClick={() => { setOpen(true); setSaved(false); }}>Modifier le lieu</button>
        )}
      </section>
    );
  }

  return (
    <section className="card dropoff-ask" aria-labelledby="dropoff-ask-title">
      <div>
        <h2 id="dropoff-ask-title" className="card-title">Où devons-nous livrer ?</h2>
        <p className="muted small">
          Le vendeur a indiqué : <strong>{request.area}</strong>. Montrez-nous le lieu exact pour que le livreur vous trouve facilement.
          Vous pouvez aussi transférer ce lien à la personne qui recevra le colis.
        </p>
      </div>

      <div className="ask-options" role="group" aria-label="Comment indiquer le lieu">
        <button type="button" className={mode === "gps" ? "ask-option on" : "ask-option"} onClick={useGps} aria-pressed={mode === "gps"}>
          <span aria-hidden="true">📍</span><strong>Je suis sur place</strong><span className="muted small">Position de mon téléphone</span>
        </button>
        <button type="button" className={mode === "search" ? "ask-option on" : "ask-option"} onClick={() => { setMode("search"); setError(null); }} aria-pressed={mode === "search"}>
          <span aria-hidden="true">🔎</span><strong>Chercher le lieu</strong><span className="muted small">Quartier, carrefour, commerce</span>
        </button>
        <button type="button" className={mode === "map" ? "ask-option on" : "ask-option"} onClick={() => { setMode("map"); setError(null); }} aria-pressed={mode === "map"}>
          <span aria-hidden="true">🗺️</span><strong>Placer sur la carte</strong><span className="muted small">Je déplace l&apos;épingle</span>
        </button>
      </div>

      {mode === "gps" && locating && <p className="muted small" role="status">Localisation en cours… acceptez la demande de votre téléphone.</p>}
      {mode === "search" && <PlaceSearchBox near={area} onPick={(p) => setPoint({ lat: round(p.lat), lng: round(p.lng) })} />}

      {mode && (mode !== "gps" || point) && (
        <MapPicker
          label="lieu de livraison"
          color="#F28C28"
          value={point}
          center={area}
          onChange={setPoint}
          hint={mode === "map" ? "Touchez la carte à l'endroit exact" : "Le lieu s'affichera ici"}
        />
      )}

      {point && (
        <>
          <label className="field">
            <span className="field-label">Repère <span className="muted">(facultatif)</span></span>
            <input value={landmark} onChange={(e) => setLandmark(e.target.value)} placeholder="Ex. Portail bleu, derrière la boulangerie" maxLength={300} />
          </label>
          <button type="button" className="btn btn-sun btn-block" onClick={save} disabled={busy}>
            {busy ? "Enregistrement…" : "Livrer à cet endroit"}
          </button>
        </>
      )}
      {error && <p className="alert alert-error" role="alert">{error}</p>}
      {confirmed && (
        <button type="button" className="link small" onClick={() => setOpen(false)}>Garder le lieu déjà indiqué</button>
      )}
    </section>
  );
}

const round = (n: number) => Math.round(n * 1e6) / 1e6;
