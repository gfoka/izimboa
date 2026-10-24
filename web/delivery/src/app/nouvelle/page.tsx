"use client";

import { useRouter } from "next/navigation";
import { useEffect, useMemo, useRef, useState, type FormEvent } from "react";
import { AppShell, RequireLogin } from "@/components/AppShell";
import { MapPicker } from "@/components/MapPicker";
import { api, ApiError, type Delivery, type GeoPoint, type Quote } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { fcfa, km } from "@/lib/format";

const PICKUP_KEY = "izimboa.delivery.lastPickup";

type Pickup = { point: GeoPoint | null; address: string; landmark: string; phone: string };

export default function NewDeliveryPage() {
  return (
    <AppShell>
      <RequireLogin>
        <NewDelivery />
      </RequireLogin>
    </AppShell>
  );
}

function NewDelivery() {
  const router = useRouter();
  const { token } = useAuth();

  const [pickup, setPickup] = useState<Pickup>({ point: null, address: "", landmark: "", phone: "" });
  const [pickupLoaded, setPickupLoaded] = useState(false);
  const [dropoff, setDropoff] = useState<GeoPoint | null>(null);
  const [dropoffAddress, setDropoffAddress] = useState("");
  const [dropoffLandmark, setDropoffLandmark] = useState("");
  const [recipientName, setRecipientName] = useState("");
  const [recipientPhone, setRecipientPhone] = useState("");
  const [description, setDescription] = useState("");
  const [cod, setCod] = useState("");
  const [express, setExpress] = useState(false);

  const [expressPrice, setExpressPrice] = useState<number | null>(null);
  const [quote, setQuote] = useState<Quote | null>(null);
  const [quoteError, setQuoteError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [touched, setTouched] = useState(false);
  // Une clé par formulaire : un double envoi (réseau coupé) ne crée pas deux livraisons.
  const idempotencyKey = useRef(crypto.randomUUID());

  // Dernier point de ramassage, pour ne pas le ressaisir à chaque fois.
  useEffect(() => {
    try {
      const saved = localStorage.getItem(PICKUP_KEY);
      if (saved) setPickup(JSON.parse(saved));
    } catch {
      /* stockage indisponible */
    }
    setPickupLoaded(true);
    api<{ expressSurcharge: number }>("/v1/delivery/tariffs").then((t) => setExpressPrice(t.expressSurcharge)).catch(() => {});
  }, []);

  const codAmount = cod.trim() === "" ? 0 : Number(cod.replace(/\s/g, ""));
  const codValid = Number.isInteger(codAmount) && codAmount >= 0;

  // Prix en direct dès que les deux épingles sont posées.
  useEffect(() => {
    if (!pickup.point || !dropoff || !codValid) {
      setQuote(null);
      return;
    }
    const ctrl = { cancelled: false };
    const t = setTimeout(async () => {
      try {
        const q = await api<Quote>("/v1/delivery/quotes", {
          method: "POST",
          body: { pickup: pickup.point, dropoff, express, codAmount },
        });
        if (!ctrl.cancelled) { setQuote(q); setQuoteError(null); }
      } catch (e) {
        if (!ctrl.cancelled) { setQuote(null); setQuoteError(e instanceof ApiError ? e.message : "Prix indisponible."); }
      }
    }, 350);
    return () => { ctrl.cancelled = true; clearTimeout(t); };
  }, [pickup.point, dropoff, express, codAmount, codValid]);

  const missing = useMemo(() => {
    const m: string[] = [];
    if (!pickup.point) m.push("l'épingle de ramassage");
    if (!pickup.address.trim()) m.push("l'adresse de ramassage");
    if (!pickup.phone.trim()) m.push("le téléphone de ramassage");
    if (!dropoff) m.push("l'épingle de livraison");
    if (!dropoffAddress.trim()) m.push("l'adresse de livraison");
    if (!recipientName.trim()) m.push("le nom du destinataire");
    if (!recipientPhone.trim()) m.push("le téléphone du destinataire");
    if (!codValid) m.push("un montant à encaisser valide");
    return m;
  }, [pickup, dropoff, dropoffAddress, recipientName, recipientPhone, codValid]);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setTouched(true);
    if (missing.length > 0 || quote?.onRequest) return;
    setSubmitting(true);
    setError(null);
    try {
      const created = await api<Delivery>("/v1/delivery/deliveries", {
        method: "POST",
        token: await token(),
        headers: { "Idempotency-Key": idempotencyKey.current },
        body: {
          pickup: pickup.point, pickupAddress: pickup.address, pickupLandmark: pickup.landmark, pickupPhone: pickup.phone,
          dropoff, dropoffAddress, dropoffLandmark, recipientName, recipientPhone,
          packageDescription: description, express, codAmount,
        },
      });
      try { localStorage.setItem(PICKUP_KEY, JSON.stringify(pickup)); } catch { /* ignoré */ }
      router.push(`/livraison/?id=${created.id}&nouvelle=1`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Erreur inattendue. Réessayez.");
      setSubmitting(false);
    }
  };

  const payout = quote && !quote.onRequest && codAmount >= quote.total ? codAmount - quote.total : null;

  return (
    <div className="container page">
      <div className="page-head">
        <div>
          <h1>Nouvelle livraison</h1>
          <p className="muted">Le prix est fixe : ni la pluie ni les bouchons ne le changent.</p>
        </div>
      </div>

      <form className="create-grid" onSubmit={submit} noValidate>
        <div className="create-main">
          <fieldset className="card">
            <legend className="card-title"><span className="step-num">1</span> Ramassage</legend>
            {pickupLoaded && (
              <MapPicker label="point de ramassage" color="#0B2545" value={pickup.point} onChange={(p) => setPickup((s) => ({ ...s, point: p }))} />
            )}
            <div className="fields">
              <Field label="Adresse" required>
                <input value={pickup.address} onChange={(e) => setPickup({ ...pickup, address: e.target.value })} placeholder="Ex. Rue Joss, Akwa" maxLength={300} autoComplete="street-address" />
              </Field>
              <Field label="Repère" hint="Ce qui aide le livreur à trouver">
                <input value={pickup.landmark} onChange={(e) => setPickup({ ...pickup, landmark: e.target.value })} placeholder="Ex. Face à la pharmacie du Port" maxLength={300} />
              </Field>
              <Field label="Téléphone sur place" required>
                <input value={pickup.phone} onChange={(e) => setPickup({ ...pickup, phone: e.target.value })} placeholder="6 99 12 34 56" inputMode="tel" autoComplete="tel" maxLength={25} />
              </Field>
            </div>
          </fieldset>

          <fieldset className="card">
            <legend className="card-title"><span className="step-num">2</span> Livraison</legend>
            <MapPicker label="point de livraison" color="#F28C28" value={dropoff} onChange={setDropoff} />
            <div className="fields">
              <Field label="Adresse" required>
                <input value={dropoffAddress} onChange={(e) => setDropoffAddress(e.target.value)} placeholder="Ex. Bonamoussadi, carrefour Kotto" maxLength={300} />
              </Field>
              <Field label="Repère" hint="Le plus important à Douala">
                <input value={dropoffLandmark} onChange={(e) => setDropoffLandmark(e.target.value)} placeholder="Ex. Derrière la boulangerie, portail bleu" maxLength={300} />
              </Field>
            </div>
          </fieldset>

          <fieldset className="card">
            <legend className="card-title"><span className="step-num">3</span> Destinataire et colis</legend>
            <div className="fields">
              <Field label="Nom du destinataire" required>
                <input value={recipientName} onChange={(e) => setRecipientName(e.target.value)} placeholder="Ex. Awa Ngo" maxLength={120} autoComplete="off" />
              </Field>
              <Field label="Téléphone (WhatsApp)" required hint="Il recevra le lien de suivi et le code de remise">
                <input value={recipientPhone} onChange={(e) => setRecipientPhone(e.target.value)} placeholder="6 77 12 34 56" inputMode="tel" maxLength={25} autoComplete="off" />
              </Field>
              <Field label="Contenu du colis">
                <input value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Ex. Une paire de chaussures" maxLength={200} />
              </Field>
              <Field label="Montant à encaisser (FCFA)" hint="Laissez vide si le client a déjà payé">
                <input value={cod} onChange={(e) => setCod(e.target.value.replace(/[^\d\s]/g, ""))} placeholder="0" inputMode="numeric" aria-invalid={!codValid} />
              </Field>
            </div>
            <label className="switch">
              <input type="checkbox" checked={express} onChange={(e) => setExpress(e.target.checked)} />
              <span className="switch-ui" aria-hidden="true" />
              <span><strong>Express</strong> — prise en charge prioritaire{expressPrice !== null ? ` (+${fcfa(expressPrice)})` : ""}</span>
            </label>
          </fieldset>
        </div>

        <aside className="create-side">
          <div className="card summary">
            <h2 className="card-title">Prix de la course</h2>
            {!quote && !quoteError && <p className="muted small">Placez les deux épingles pour voir le prix.</p>}
            {quoteError && <p className="field-error">{quoteError}</p>}
            {quote?.onRequest && (
              <p className="alert alert-warn">Distance de {km(quote.distanceKm)} : au-delà de notre grille, le prix est sur devis. Contactez-nous sur WhatsApp.</p>
            )}
            {quote && !quote.onRequest && (
              <dl className="price">
                <div><dt>Course ({km(quote.distanceKm)})</dt><dd>{fcfa(quote.basePrice)}</dd></div>
                {quote.expressSurcharge > 0 && <div><dt>Express</dt><dd>{fcfa(quote.expressSurcharge)}</dd></div>}
                {quote.codCommission > 0 && <div><dt>Encaissement (1,5 %)</dt><dd>{fcfa(quote.codCommission)}</dd></div>}
                <div className="price-total"><dt>Total</dt><dd>{fcfa(quote.total)}</dd></div>
              </dl>
            )}
            {payout !== null && (
              <p className="payout">Rien à avancer : les frais sont retenus sur l'encaissement. Vous recevez <strong>{fcfa(payout)}</strong> le soir même.</p>
            )}
            {quote && !quote.onRequest && payout === null && (
              <p className="muted small">Frais à régler à Izimboa (Mobile Money bientôt disponible dans l'application).</p>
            )}

            {touched && missing.length > 0 && (
              <p className="field-error" role="alert">Il manque : {missing.join(", ")}.</p>
            )}
            {error && <p className="alert alert-error" role="alert">{error}</p>}

            <button type="submit" className="btn btn-sun btn-block" disabled={submitting || quote?.onRequest === true}>
              {submitting ? "Création…" : "Créer la livraison"}
            </button>
            <p className="muted small center">Annulation gratuite tant que le colis n'est pas ramassé.</p>
          </div>
        </aside>
      </form>
    </div>
  );
}

function Field({ label, hint, required, children }: { label: string; hint?: string; required?: boolean; children: React.ReactNode }) {
  return (
    <label className="field">
      <span className="field-label">{label}{required && <span className="req" aria-hidden="true"> *</span>}</span>
      {children}
      {hint && <span className="field-hint">{hint}</span>}
    </label>
  );
}
