"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { AdminShell, FileBadge } from "@/components/Admin";
import { api, ApiError, type CourierSummary } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { formatDateTime, formatPhone } from "@/lib/format";

const FILTERS = [
  { key: "PENDING", label: "À vérifier" },
  { key: "TO_REVIEW", label: "Documents à revoir" },
  { key: "ACTIVE", label: "Actifs" },
  { key: "DRAFT", label: "En cours de saisie" },
  { key: "REJECTED", label: "Refusés" },
  { key: "SUSPENDED", label: "Suspendus" },
  { key: "ALL", label: "Tous" },
] as const;

export default function CouriersPage() {
  return (
    <AdminShell>
      <CourierList />
    </AdminShell>
  );
}

function CourierList() {
  const { token } = useAuth();
  const [filter, setFilter] = useState<string>("PENDING");
  const [rows, setRows] = useState<CourierSummary[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const f = new URLSearchParams(window.location.search).get("filtre");
    if (f && FILTERS.some((x) => x.key === f)) setFilter(f);
  }, []);

  const load = useCallback(async () => {
    setRows(null);
    try {
      setRows(await api<CourierSummary[]>(`/v1/delivery/couriers?status=${filter}`, { token: await token() }));
      setError(null);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Erreur inattendue.");
      setRows([]);
    }
  }, [filter, token]);

  useEffect(() => { load(); }, [load]);

  const choose = (key: string) => {
    setFilter(key);
    window.history.replaceState(null, "", `?filtre=${key}`);
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Livreurs</h1>
          <p className="muted">Vérifiez l&apos;identité et les documents avant d&apos;activer un compte.</p>
        </div>
      </div>
      <div className="tabs" role="tablist" aria-label="Filtrer">
        {FILTERS.map((f) => (
          <button key={f.key} role="tab" aria-selected={filter === f.key} className={filter === f.key ? "tab active" : "tab"} onClick={() => choose(f.key)}>
            {f.label}
          </button>
        ))}
      </div>
      {error && <div className="alert alert-error" role="alert">{error}</div>}
      {rows === null ? (
        <div className="skeleton tall" />
      ) : rows.length === 0 ? (
        <div className="empty"><p className="muted">Aucun livreur dans cette liste.</p></div>
      ) : (
        <div className="list">
          {rows.map((r) => (
            <Link key={r.userId} href={`/admin/livreur/?id=${r.userId}`} className="row-card">
              <div className="row-main">
                <p className="row-title">{r.name ?? "Nom non renseigné"}</p>
                <p className="row-sub">{r.phone ? formatPhone(r.phone) : "Sans numéro"}</p>
                <p className="row-meta">
                  {r.submittedAt ? `Envoyé le ${formatDateTime(r.submittedAt)}` : `Mis à jour le ${formatDateTime(r.updatedAt)}`}
                  {r.missing > 0 && ` · ${r.missing} document${r.missing > 1 ? "s" : ""} manquant${r.missing > 1 ? "s" : ""}`}
                </p>
              </div>
              <div className="row-side">
                <FileBadge status={r.status} />
                {r.ratingCount > 0 && <span className="row-amount">★ {r.ratingAverage?.toFixed(1)} ({r.ratingCount})</span>}
                {r.toReview && r.status === "ACTIVE" && <span className="badge badge-wait">Documents à revoir</span>}
              </div>
            </Link>
          ))}
        </div>
      )}
    </>
  );
}
