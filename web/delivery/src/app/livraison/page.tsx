"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { AppShell, RequireLogin } from "@/components/AppShell";
import { Progress, StatusBadge, Timeline } from "@/components/Status";
import { api, ApiError, type Delivery } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { FINAL, fcfa, formatDateTime, formatPhone, km, whatsappLink } from "@/lib/format";

export default function DeliveryPage() {
  return (
    <AppShell>
      <RequireLogin>
        <DeliveryDetail />
      </RequireLogin>
    </AppShell>
  );
}

function DeliveryDetail() {
  const { token } = useAuth();
  const [id, setId] = useState<string | null>(null);
  const [justCreated, setJustCreated] = useState(false);
  const [d, setD] = useState<Delivery | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    const q = new URLSearchParams(window.location.search);
    setId(q.get("id"));
    setJustCreated(q.get("nouvelle") === "1");
  }, []);

  const load = useCallback(async () => {
    if (!id) return;
    try {
      setD(await api<Delivery>(`/v1/delivery/deliveries/${encodeURIComponent(id)}`, { token: await token() }));
      setError(null);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Erreur inattendue.");
    }
  }, [id, token]);

  useEffect(() => {
    load();
  }, [load]);

  // Rafraîchissement automatique tant que la livraison n'est pas terminée.
  useEffect(() => {
    if (!d || FINAL.includes(d.status)) return;
    const t = setInterval(load, 15_000);
    return () => clearInterval(t);
  }, [d, load]);

  const cancel = async () => {
    if (!d || !window.confirm("Annuler cette livraison ? C'est gratuit tant que le colis n'est pas ramassé.")) return;
    setBusy(true);
    try {
      setD(await api<Delivery>(`/v1/delivery/deliveries/${d.id}/cancel`, { method: "POST", token: await token(), body: { reason: "Annulée par le vendeur" } }));
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Erreur inattendue.");
    } finally {
      setBusy(false);
    }
  };

  if (id === null && !d) return <div className="container page"><div className="skeleton tall" /></div>;
  if (error && !d) {
    return (
      <div className="container page narrow center">
        <h1>Livraison introuvable</h1>
        <p className="muted">{error}</p>
        <Link href="/" className="btn btn-navy">Retour à mes livraisons</Link>
      </div>
    );
  }
  if (!d) return <div className="container page"><div className="skeleton tall" /></div>;

  const message = recipientMessage(d);
  const canCancel = d.status === "CREATED" || d.status === "ASSIGNED";

  return (
    <div className="container page">
      <p className="back"><Link href="/">← Mes livraisons</Link></p>

      {justCreated && (
        <div className="alert alert-ok" role="status">
          <strong>Livraison créée.</strong> Envoyez maintenant le lien de suivi et le code à votre client sur WhatsApp.
        </div>
      )}

      <div className="page-head">
        <div>
          <p className="eyebrow">{d.reference}{d.express && <span className="chip-express">Express</span>}</p>
          <h1>{d.recipientName}</h1>
          <p className="muted">Créée le {formatDateTime(d.createdAt)}</p>
        </div>
        <StatusBadge status={d.status} />
      </div>

      <div className="card"><Progress status={d.status} /></div>

      <div className="detail-grid">
        <div className="detail-main">
          {d.handoverCode && !FINAL.includes(d.status) && (
            <section className="card code-card">
              <div>
                <h2 className="card-title">Code de remise</h2>
                <p className="muted small">Le destinataire le donne au livreur : c'est la preuve de livraison. Ne le donnez jamais au livreur.</p>
              </div>
              <p className="code" aria-label={`Code ${d.handoverCode.split("").join(" ")}`}>{d.handoverCode}</p>
              <div className="code-actions">
                <a className="btn btn-whatsapp" href={whatsappLink(d.recipientPhone, message)} target="_blank" rel="noopener noreferrer">
                  Envoyer au client sur WhatsApp
                </a>
                <button type="button" className="btn btn-outline btn-sm" onClick={async () => {
                  try { await navigator.clipboard.writeText(d.trackingUrl); setCopied(true); setTimeout(() => setCopied(false), 2000); } catch { /* ignoré */ }
                }}>
                  {copied ? "Lien copié ✓" : "Copier le lien de suivi"}
                </button>
              </div>
            </section>
          )}

          <section className="card">
            <h2 className="card-title">Trajet</h2>
            <div className="route">
              <div className="route-stop">
                <span className="route-dot route-from" aria-hidden="true" />
                <div>
                  <p className="route-label">Ramassage</p>
                  <p>{d.pickup.address}</p>
                  {d.pickup.landmark && <p className="muted small">{d.pickup.landmark}</p>}
                  <p className="muted small"><a href={`tel:${d.pickupPhone}`}>{formatPhone(d.pickupPhone)}</a></p>
                </div>
              </div>
              <div className="route-stop">
                <span className="route-dot route-to" aria-hidden="true" />
                <div>
                  <p className="route-label">Livraison</p>
                  <p>{d.dropoff.address}</p>
                  {d.dropoff.landmark && <p className="muted small">{d.dropoff.landmark}</p>}
                  <p className="muted small">{d.recipientName} · <a href={`tel:${d.recipientPhone}`}>{formatPhone(d.recipientPhone)}</a></p>
                </div>
              </div>
            </div>
            <p className="muted small">{km(d.distanceKm)} {d.packageDescription ? `· ${d.packageDescription}` : ""}</p>
          </section>

          <section className="card">
            <h2 className="card-title">Historique</h2>
            <Timeline events={d.timeline} />
          </section>
        </div>

        <aside className="detail-side">
          <section className="card">
            <h2 className="card-title">Argent</h2>
            <dl className="price">
              {d.pricing.codAmount > 0 && <div><dt>À encaisser auprès du client</dt><dd>{fcfa(d.pricing.codAmount)}</dd></div>}
              <div><dt>Course</dt><dd>{fcfa(d.pricing.basePrice)}</dd></div>
              {d.pricing.expressSurcharge > 0 && <div><dt>Express</dt><dd>{fcfa(d.pricing.expressSurcharge)}</dd></div>}
              {d.pricing.codCommission > 0 && <div><dt>Encaissement</dt><dd>{fcfa(d.pricing.codCommission)}</dd></div>}
              <div className="price-total"><dt>Frais Izimboa</dt><dd>{fcfa(d.pricing.totalFee)}</dd></div>
              {d.pricing.returnFee !== null && <div><dt>Frais de retour</dt><dd>{fcfa(d.pricing.returnFee)}</dd></div>}
            </dl>
            {d.pricing.feePayment === "DEDUCTED_FROM_COD" ? (
              <p className="payout">Vous recevez <strong>{fcfa(d.pricing.sellerPayout)}</strong> {d.status === "DELIVERED" ? "ce soir" : "le soir de la livraison"}.</p>
            ) : (
              <p className="muted small">Frais à régler à Izimboa.</p>
            )}
          </section>

          {canCancel && (
            <button type="button" className="btn btn-outline btn-block" onClick={cancel} disabled={busy}>
              {busy ? "Annulation…" : "Annuler la livraison"}
            </button>
          )}
          {error && <p className="alert alert-error" role="alert">{error}</p>}
          <p className="muted small center">Un souci ? Le support Izimboa répond sur WhatsApp.</p>
        </aside>
      </div>
    </div>
  );
}

function recipientMessage(d: Delivery): string {
  const lines = [
    `Bonjour ${d.recipientName}, votre colis${d.packageDescription ? ` (${d.packageDescription})` : ""} est livré par Izimboa Delivery.`,
    `Suivez-le ici : ${d.trackingUrl}`,
    `Code de remise à donner au livreur à la réception : ${d.handoverCode}`,
  ];
  if (d.pricing.codAmount > 0) lines.push(`Montant à payer à la livraison : ${fcfa(d.pricing.codAmount)}`);
  return lines.join("\n");
}
