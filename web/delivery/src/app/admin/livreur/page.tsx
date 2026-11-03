"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { AdminShell, FileBadge } from "@/components/Admin";
import { api, ApiError, type CourierDetail, type DocRequirement } from "@/lib/api";
import { API_URL } from "@/lib/config";
import { useAuth } from "@/lib/auth";
import { formatDateTime, formatPhone } from "@/lib/format";

export default function CourierPage() {
  return (
    <AdminShell>
      <CourierFile />
    </AdminShell>
  );
}

const STATE: Record<DocRequirement["state"], { label: string; badge: string }> = {
  OK: { label: "Fourni", badge: "badge-ok" },
  EXPIRING_SOON: { label: "Expire bientôt", badge: "badge-wait" },
  EXPIRED: { label: "Expiré", badge: "badge-bad" },
  MISSING: { label: "Manquant", badge: "badge-off" },
};

function CourierFile() {
  const { token, roles } = useAuth();
  const canDecide = roles.includes("validateur") || roles.includes("admin");
  const [id, setId] = useState<string | null>(null);
  const [c, setC] = useState<CourierDetail | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [refusing, setRefusing] = useState<"reject" | "suspend" | null>(null);
  const [reason, setReason] = useState("");
  const [done, setDone] = useState<string | null>(null);

  useEffect(() => setId(new URLSearchParams(window.location.search).get("id")), []);

  const load = useCallback(async () => {
    if (!id) return;
    try {
      setC(await api<CourierDetail>(`/v1/delivery/couriers/${encodeURIComponent(id)}`, { token: await token() }));
      setError(null);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Erreur inattendue.");
    }
  }, [id, token]);

  useEffect(() => { load(); }, [load]);

  const decide = async (action: "activate" | "reject" | "suspend") => {
    if (!id) return;
    setBusy(true);
    setError(null);
    try {
      const body = action === "activate" ? undefined : { reason };
      setC(await api<CourierDetail>(`/v1/delivery/couriers/${id}/${action}`, { method: "POST", body, token: await token() }));
      setRefusing(null);
      setReason("");
      setDone(action === "activate" ? "Compte validé : la personne peut maintenant recevoir des courses."
        : action === "reject" ? "Dossier refusé : la personne voit le motif et peut corriger."
        : "Compte suspendu : la personne ne reçoit plus de courses.");
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Erreur inattendue.");
    } finally {
      setBusy(false);
    }
  };

  if (error && !c) return <div className="alert alert-error" role="alert">{error}</div>;
  if (!c) return <div className="skeleton tall" />;

  const name = [c.firstName, c.lastName].filter(Boolean).join(" ") || "Nom non renseigné";
  const toReview = c.documents.some((d) => d.submitted && !d.submitted.reviewed);

  return (
    <>
      <p className="back"><Link href="/admin/livreurs/">← Livreurs</Link></p>
      <div className="page-head">
        <div>
          <h1>{name}</h1>
          <p className="muted">
            {c.phone ? formatPhone(c.phone) : "Sans numéro"}{c.email ? ` · ${c.email}` : ""}
            {c.submittedAt ? ` · envoyé le ${formatDateTime(c.submittedAt)}` : ""}
          </p>
        </div>
        <FileBadge status={c.status} />
      </div>

      {done && <div className="alert alert-ok" role="status">{done}</div>}
      {error && <div className="alert alert-error" role="alert">{error}</div>}
      {c.reason && (c.status === "REJECTED" || c.status === "SUSPENDED") && (
        <div className="alert alert-warn">Motif : {c.reason}</div>
      )}
      {c.warnings.map((w) => <div key={w} className="alert alert-warn" role="note">⚠ {w}</div>)}
      {!c.complete && <div className="alert alert-info">Dossier incomplet : il manque des informations ou des documents obligatoires.</div>}

      {c.documents.map((d) => (
        <section key={d.type.id} className="card">
          <div className="page-head">
            <h2 className="card-title">{d.type.name}{!d.type.mandatory && <span className="muted small"> (facultatif)</span>}</h2>
            <span className={`badge ${STATE[d.state].badge}`}>{STATE[d.state].label}</span>
          </div>
          {d.submitted ? (
            <>
              <p className="muted small">
                {d.submitted.number && <>Numéro : <strong>{d.submitted.number}</strong> · </>}
                {d.submitted.expiresOn && <>Expire le <strong>{new Date(d.submitted.expiresOn).toLocaleDateString("fr-FR")}</strong> · </>}
                Envoyé le {formatDateTime(d.submitted.submittedAt)}
                {!d.submitted.reviewed && " · nouveau, à vérifier"}
              </p>
              <div className="doc-pages">
                {d.type.pages.filter((p) => d.submitted!.pages.includes(p.id)).map((p) => (
                  <PagePhoto key={p.id} userId={c.userId} documentId={d.submitted!.id} pageId={p.id} label={p.name} />
                ))}
              </div>
            </>
          ) : (
            <p className="muted">Pas encore fourni.</p>
          )}
        </section>
      ))}

      {canDecide && (
        <section className="card">
          <h2 className="card-title">Décision</h2>
          {refusing ? (
            <div className="fields">
              <label className="field">
                <span className="field-label">{refusing === "reject" ? "Motif du refus (montré au livreur)" : "Motif de la suspension (montré au livreur)"}</span>
                <textarea className="textarea" rows={3} value={reason} onChange={(e) => setReason(e.target.value)}
                  placeholder={refusing === "reject" ? "Ex. : photo du verso illisible, reprenez-la." : "Ex. : colis non remis le 3 novembre."} />
              </label>
              <div className="actions-row">
                <button className="btn btn-navy" disabled={busy || !reason.trim()} onClick={() => decide(refusing)}>
                  {refusing === "reject" ? "Refuser le dossier" : "Suspendre le compte"}
                </button>
                <button className="btn btn-outline" disabled={busy} onClick={() => setRefusing(null)}>Annuler</button>
              </div>
            </div>
          ) : (
            <div className="actions-row">
              {(c.status !== "ACTIVE" || toReview) && (
                <button className="btn btn-sun" disabled={busy || !c.complete} onClick={() => decide("activate")}>
                  {c.status === "ACTIVE" ? "Documents vérifiés" : "Valider : activer comme livreur"}
                </button>
              )}
              {c.status !== "REJECTED" && c.status !== "SUSPENDED" && c.status !== "ACTIVE" && (
                <button className="btn btn-outline" disabled={busy} onClick={() => setRefusing("reject")}>Refuser</button>
              )}
              {c.status === "ACTIVE" && (
                <button className="btn btn-outline" disabled={busy} onClick={() => setRefusing("suspend")}>Suspendre</button>
              )}
              {c.status === "SUSPENDED" && (
                <button className="btn btn-sun" disabled={busy || !c.complete} onClick={() => decide("activate")}>Réactiver</button>
              )}
            </div>
          )}
        </section>
      )}
    </>
  );
}

/** Photo protégée : chargée avec le jeton, jamais mise en cache, ouverte en grand au clic. */
function PagePhoto({ userId, documentId, pageId, label }: { userId: string; documentId: string; pageId: number; label: string }) {
  const { token } = useAuth();
  const [url, setUrl] = useState<string | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let objectUrl: string | null = null;
    let cancelled = false;
    (async () => {
      try {
        const res = await fetch(`${API_URL}/v1/delivery/couriers/${userId}/documents/${documentId}/pages/${pageId}`, {
          headers: { Authorization: `Bearer ${await token()}` },
          cache: "no-store",
        });
        if (!res.ok) throw new Error();
        objectUrl = URL.createObjectURL(await res.blob());
        if (!cancelled) setUrl(objectUrl);
      } catch {
        if (!cancelled) setFailed(true);
      }
    })();
    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [userId, documentId, pageId, token]);

  return (
    <figure className="doc-page">
      {url ? (
        <a href={url} target="_blank" rel="noopener noreferrer" title="Ouvrir en grand">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={url} alt={label} />
        </a>
      ) : (
        <div className="doc-page-empty">{failed ? "Photo indisponible" : "Chargement…"}</div>
      )}
      <figcaption>{label}</figcaption>
    </figure>
  );
}
