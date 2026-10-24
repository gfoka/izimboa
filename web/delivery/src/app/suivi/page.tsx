"use client";

import { useCallback, useEffect, useState } from "react";
import { LiveMap, type LivePosition } from "@/components/LiveMap";
import { Logo } from "@/components/Logo";
import { Progress, Timeline } from "@/components/Status";
import { api, ApiError, type Tracking } from "@/lib/api";
import { FINAL, fcfa, STATUS } from "@/lib/format";

/**
 * Suivi public du destinataire : delivery.izimboa.com/suivi/<jeton>/ (lien envoyé sur WhatsApp).
 * Sans connexion et sans données personnelles. Caddy sert cette page pour toute adresse /suivi/…
 */
export default function TrackingPage() {
  const [token, setToken] = useState<string | null>(null);
  const [t, setT] = useState<Tracking | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [courier, setCourier] = useState<LivePosition | null>(null);

  useEffect(() => {
    const fromPath = window.location.pathname.split("/").filter(Boolean)[1];
    setToken(fromPath ?? new URLSearchParams(window.location.search).get("t") ?? "");
  }, []);

  const load = useCallback(async () => {
    if (!token) return;
    try {
      setT(await api<Tracking>(`/v1/delivery/track/${encodeURIComponent(token)}`));
      setError(null);
    } catch (e) {
      setError(e instanceof ApiError && e.status === 404 ? "Ce lien de suivi n'existe pas ou a expiré." : "Suivi momentanément indisponible.");
    }
  }, [token]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    if (!t || FINAL.includes(t.status)) return;
    const i = setInterval(load, 20_000);
    return () => clearInterval(i);
  }, [t, load]);

  // Le destinataire voit le livreur quand le colis est en route vers lui.
  const enRoute = t?.status === "IN_TRANSIT";
  useEffect(() => {
    if (!enRoute || !token) { setCourier(null); return; }
    let stop = false;
    const poll = async () => {
      try {
        const p = await api<LivePosition | undefined>(`/v1/delivery/track/${encodeURIComponent(token)}/courier-position`);
        if (!stop) setCourier(p ?? null);
      } catch { /* prochain tour */ }
    };
    poll();
    const i = setInterval(poll, 10_000);
    return () => { stop = true; clearInterval(i); };
  }, [enRoute, token]);

  return (
    <div className="track">
      <header className="track-header">
        <div className="container"><Logo inverted /></div>
      </header>
      <main className="container page narrow">
        {token === "" && <Message title="Lien incomplet" text="Ouvrez le lien reçu sur WhatsApp en entier." />}
        {error && !t && <Message title="Suivi indisponible" text={error} />}
        {!t && !error && token !== "" && <div className="skeleton tall" />}
        {t && (
          <>
            <p className="eyebrow">Colis {t.reference}{t.express && <span className="chip-express">Express</span>}</p>
            <h1 className="track-status">{headline(t)}</h1>
            <div className="card"><Progress status={t.status} /></div>
            {enRoute && courier && (
              <div className="card">
                <h2 className="card-title"><span className="live-dot on">Votre livreur en direct</span></h2>
                <LiveMap courier={courier} />
              </div>
            )}
            {t.amountToPay > 0 && !FINAL.includes(t.status) && (
              <div className="card track-pay">
                <p className="muted small">À payer à la réception</p>
                <p className="track-amount">{fcfa(t.amountToPay)}</p>
              </div>
            )}
            {!FINAL.includes(t.status) && (
              <p className="alert alert-info">À l&apos;arrivée du livreur, donnez-lui le <strong>code de remise</strong> reçu sur WhatsApp. Ne le communiquez à personne d&apos;autre.</p>
            )}
            <section className="card">
              <h2 className="card-title">Étapes</h2>
              <Timeline events={t.timeline} />
            </section>
          </>
        )}
        <p className="muted small center track-foot">Livré par Izimboa Delivery · <a href="https://izimboa.com/">izimboa.com</a></p>
      </main>
    </div>
  );
}

function headline(t: Tracking): string {
  switch (t.status) {
    case "CREATED": return "Votre colis est enregistré";
    case "ASSIGNED": return "Un livreur va récupérer votre colis";
    case "PICKED_UP": return "Votre colis a été récupéré";
    case "IN_TRANSIT": return "Votre colis est en route";
    case "DELIVERED": return "Votre colis est livré 🎉";
    default: return STATUS[t.status].label;
  }
}

function Message({ title, text }: { title: string; text: string }) {
  return (
    <div className="center">
      <h1>{title}</h1>
      <p className="muted">{text}</p>
    </div>
  );
}
