"use client";

import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { AppShell } from "@/components/AppShell";
import { Welcome } from "@/components/Welcome";
import { StatusBadge } from "@/components/Status";
import { api, ApiError, type DeliverySummary, type Status } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { FINAL, fcfa, formatDateTime, isToday } from "@/lib/format";

type Filter = "active" | "done" | "all";
const FILTERS: { key: Filter; label: string }[] = [
  { key: "active", label: "En cours" },
  { key: "done", label: "Terminées" },
  { key: "all", label: "Toutes" },
];

export default function Home() {
  return (
    <AppShell>
      <HomeContent />
    </AppShell>
  );
}

/** Connecté : le tableau de bord. Pas encore connecté : la présentation et les deux portes d'entrée. */
function HomeContent() {
  const { user, ready } = useAuth();
  if (!ready) return <div className="container page"><div className="skeleton tall" /></div>;
  return user ? <Dashboard /> : <Welcome />;
}

function Dashboard() {
  const { token, user } = useAuth();
  const [items, setItems] = useState<DeliverySummary[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<Filter>("active");

  const load = useCallback(async () => {
    try {
      setItems(await api<DeliverySummary[]>("/v1/delivery/deliveries?scope=SENT", { token: await token() }));
      setError(null);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Erreur inattendue.");
    }
  }, [token]);

  useEffect(() => {
    load();
    const t = setInterval(load, 30_000);
    return () => clearInterval(t);
  }, [load]);

  const stats = useMemo(() => {
    const list = items ?? [];
    const active = list.filter((d) => !FINAL.includes(d.status));
    const deliveredToday = list.filter((d) => d.status === "DELIVERED" && isToday(d.updatedAt));
    const payoutToday = deliveredToday.reduce((s, d) => s + (d.codAmount >= d.totalFee ? d.codAmount - d.totalFee : d.codAmount), 0);
    return { active: active.length, deliveredToday: deliveredToday.length, payoutToday };
  }, [items]);

  const shown = (items ?? []).filter((d) =>
    filter === "all" ? true : filter === "active" ? !FINAL.includes(d.status) : FINAL.includes(d.status as Status));

  const first = user?.profile.given_name ?? user?.profile.name?.split(" ")[0];

  return (
    <div className="container page">
      <div className="page-head">
        <div>
          <h1>{first ? `Bonjour ${first}` : "Mes livraisons"}</h1>
          <p className="muted">Vos colis à Douala, et l'argent encaissé pour vous.</p>
        </div>
        <Link href="/nouvelle/" className="btn btn-sun hide-mobile">Nouvelle livraison</Link>
      </div>

      <section className="stats" aria-label="Résumé">
        <div className="stat"><span className="stat-value">{items ? stats.active : "–"}</span><span className="stat-label">en cours</span></div>
        <div className="stat"><span className="stat-value">{items ? stats.deliveredToday : "–"}</span><span className="stat-label">livrées aujourd'hui</span></div>
        <div className="stat stat-money"><span className="stat-value">{items ? fcfa(stats.payoutToday) : "–"}</span><span className="stat-label">à vous reverser aujourd'hui</span></div>
      </section>

      <div className="tabs" role="tablist" aria-label="Filtrer">
        {FILTERS.map((f) => (
          <button key={f.key} role="tab" aria-selected={filter === f.key} className={filter === f.key ? "tab active" : "tab"} onClick={() => setFilter(f.key)}>
            {f.label}
          </button>
        ))}
      </div>

      {error && <div className="alert alert-error" role="alert">{error} <button className="link" onClick={load}>Réessayer</button></div>}

      {!items && !error && <div className="list">{[0, 1, 2].map((i) => <div key={i} className="skeleton row" />)}</div>}

      {items && shown.length === 0 && (
        <div className="empty">
          <h2>{filter === "active" ? "Aucune livraison en cours" : "Rien ici pour l'instant"}</h2>
          <p className="muted">Créez une livraison : le prix s'affiche tout de suite, et votre client reçoit un lien de suivi.</p>
          <Link href="/nouvelle/" className="btn btn-sun">Créer une livraison</Link>
        </div>
      )}

      {items && shown.length > 0 && (
        <ul className="list">
          {shown.map((d) => (
            <li key={d.id}>
              <Link href={`/livraison/?id=${d.id}`} className="row-card">
                <div className="row-main">
                  <p className="row-title">{d.recipientName}{d.express && <span className="chip-express">Express</span>}</p>
                  <p className="row-sub">{d.dropoffAddress}</p>
                  <p className="row-meta">{d.reference} · {formatDateTime(d.createdAt)}</p>
                </div>
                <div className="row-side">
                  <StatusBadge status={d.status} />
                  <p className="row-amount">{d.codAmount > 0 ? `À encaisser ${fcfa(d.codAmount)}` : `Frais ${fcfa(d.totalFee)}`}</p>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
