"use client";

import { useState } from "react";
import { api, ApiError, type Tracking } from "@/lib/api";

/**
 * Note de la livraison par le destinataire, depuis son lien de suivi, sans connexion (décision 75) :
 * de 1 à 5 étoiles, une remarque facultative, modifiable pendant 7 jours après la remise.
 */
export function RatingBox({ token, rating, onSaved }: { token: string; rating: NonNullable<Tracking["rating"]>; onSaved: (t: Tracking) => void }) {
  const [stars, setStars] = useState(rating.stars ?? 0);
  const [hover, setHover] = useState(0);
  const [comment, setComment] = useState(rating.comment ?? "");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);
  const [editing, setEditing] = useState(rating.stars == null);

  const send = async () => {
    setBusy(true);
    setError(null);
    try {
      const t = await api<Tracking>(`/v1/delivery/track/${encodeURIComponent(token)}/rating`, {
        method: "POST", body: { stars, comment: comment.trim() || null },
      });
      onSaved(t);
      setSaved(true);
      setEditing(false);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Envoi impossible. Réessayez.");
    } finally {
      setBusy(false);
    }
  };

  if (!editing && rating.stars != null) {
    return (
      <section className="card rating-card">
        <h2 className="card-title">{saved ? "Merci pour votre avis !" : "Votre avis"}</h2>
        <p className="stars-static" aria-label={`${rating.stars} étoiles sur 5`}>{"★".repeat(rating.stars)}<span>{"★".repeat(5 - rating.stars)}</span></p>
        {rating.comment && <p className="muted">« {rating.comment} »</p>}
        {rating.open && <button className="link" onClick={() => setEditing(true)}>Modifier mon avis</button>}
      </section>
    );
  }
  if (!rating.open) return null;

  const labels = ["", "Très décevant", "Décevant", "Correct", "Bien", "Excellent"];
  return (
    <section className="card rating-card">
      <h2 className="card-title">Comment s&apos;est passée votre livraison ?</h2>
      <div className="stars" role="radiogroup" aria-label="Note de 1 à 5 étoiles" onMouseLeave={() => setHover(0)}>
        {[1, 2, 3, 4, 5].map((n) => (
          <button key={n} type="button" role="radio" aria-checked={stars === n} aria-label={`${n} étoile${n > 1 ? "s" : ""} : ${labels[n]}`}
            className={(hover || stars) >= n ? "star on" : "star"} onMouseEnter={() => setHover(n)} onClick={() => setStars(n)}>★</button>
        ))}
      </div>
      <p className="muted small center">{labels[hover || stars] || "Touchez une étoile"}</p>
      {stars > 0 && (
        <>
          <label className="field">
            <span className="field-label">Une remarque ? (facultatif)</span>
            <textarea className="textarea" rows={3} maxLength={500} value={comment} onChange={(e) => setComment(e.target.value)}
              placeholder={stars <= 2 ? "Dites-nous ce qui n'a pas été : nous vous recontactons." : "Ex. : livreur ponctuel et aimable."} />
          </label>
          <button className="btn btn-sun btn-block" disabled={busy} onClick={send}>{busy ? "Envoi…" : "Envoyer mon avis"}</button>
        </>
      )}
      {error && <p className="alert alert-error" role="alert">{error}</p>}
    </section>
  );
}
