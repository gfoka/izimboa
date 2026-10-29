"use client";

import { useState, type FormEvent } from "react";
import { api, ApiError, type GeoPoint, type Place } from "@/lib/api";

/** Recherche d'un quartier, d'une rue ou d'un lieu connu (OpenStreetMap), proche de {@code near}. */
export function PlaceSearchBox({ near, onPick }: { near?: GeoPoint; onPick: (p: Place) => void }) {
  const [q, setQ] = useState("");
  const [results, setResults] = useState<Place[] | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const search = async (e?: FormEvent) => {
    e?.preventDefault();
    if (q.trim().length < 3) return setError("Tapez au moins 3 lettres.");
    setBusy(true);
    setError(null);
    try {
      const params = new URLSearchParams({ q: q.trim() });
      if (near) { params.set("lat", String(near.lat)); params.set("lng", String(near.lng)); }
      setResults(await api<Place[]>(`/v1/delivery/places?${params}`));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Recherche indisponible.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="place-search">
      <form className="paste-row" onSubmit={search} role="search">
        <label className="sr-only" htmlFor="place-q">Chercher un lieu</label>
        <input id="place-q" value={q} onChange={(e) => setQ(e.target.value)} placeholder="Ex. Carrefour Kotto, Total Bonamoussadi" maxLength={120} autoComplete="off" />
        <button type="submit" className="btn btn-navy btn-sm" disabled={busy}>{busy ? "…" : "Chercher"}</button>
      </form>
      {error && <p className="field-error">{error}</p>}
      {results && results.length === 0 && (
        <p className="muted small">Aucun lieu trouvé. Essayez un autre nom, ou placez l&apos;épingle sur la carte.</p>
      )}
      {results && results.length > 0 && (
        <ul className="place-results">
          {results.map((r, i) => (
            <li key={`${r.lat},${r.lng},${i}`}>
              <button type="button" onClick={() => { onPick(r); setResults(null); }}>
                <strong>{r.name}</strong>
                {r.detail && <span className="muted small">{r.detail}</span>}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
