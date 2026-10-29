"use client";

import { useState, type ClipboardEvent } from "react";
import { api, ApiError, type GeoPoint } from "@/lib/api";

/**
 * Champ « Localisation du client » : le vendeur colle la localisation reçue sur WhatsApp
 * (lien Google Maps, lien court maps.app.goo.gl, coordonnées) et l'épingle se place toute seule.
 */
export function LocationPaste({ token, onFound }: { token: () => Promise<string | null>; onFound: (p: GeoPoint) => void }) {
  const [text, setText] = useState("");
  const [state, setState] = useState<"idle" | "busy" | "ok" | "error">("idle");
  const [message, setMessage] = useState<string | null>(null);

  const resolve = async (value: string) => {
    const v = value.trim();
    if (!v) return;
    setState("busy");
    setMessage(null);
    try {
      const p = await api<GeoPoint>("/v1/delivery/locations/resolve", { method: "POST", token: await token(), body: { text: v } });
      onFound(p);
      setState("ok");
      setMessage("Épingle placée sur la localisation du client. Vérifiez-la sur la carte.");
    } catch (e) {
      setState("error");
      setMessage(e instanceof ApiError ? e.message : "Lecture impossible. Réessayez.");
    }
  };

  // Coller suffit : pas besoin d'appuyer sur un bouton.
  const onPaste = (e: ClipboardEvent<HTMLInputElement>) => {
    const pasted = e.clipboardData.getData("text");
    if (pasted) setTimeout(() => resolve(pasted), 0);
  };

  return (
    <div className="paste">
      <label className="field">
        <span className="field-label">Localisation envoyée par le client <span className="muted">(facultatif)</span></span>
        <div className="paste-row">
          <input
            value={text}
            onChange={(e) => { setText(e.target.value); setState("idle"); }}
            onPaste={onPaste}
            onKeyDown={(e) => { if (e.key === "Enter") { e.preventDefault(); resolve(text); } }}
            placeholder="Collez ici le lien WhatsApp ou Google Maps"
            inputMode="url"
            autoComplete="off"
            maxLength={2000}
            aria-invalid={state === "error"}
            aria-describedby="paste-help"
          />
          <button type="button" className="btn btn-navy btn-sm" onClick={() => resolve(text)} disabled={state === "busy" || !text.trim()}>
            {state === "busy" ? "Lecture…" : "Placer"}
          </button>
        </div>
        <span id="paste-help" className="field-hint">
          Sur WhatsApp, touchez la localisation du client → Google Maps s&apos;ouvre → Partager → Copier, puis collez ici.
        </span>
      </label>
      {message && <p className={state === "ok" ? "map-hint ok" : "field-error"} role={state === "error" ? "alert" : "status"}>{message}</p>}
    </div>
  );
}
