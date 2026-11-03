"use client";

import { useCallback, useEffect, useState } from "react";
import { AdminShell } from "@/components/Admin";
import { api, ApiError, type DocumentType } from "@/lib/api";
import { useAuth } from "@/lib/auth";

export default function DocumentsPage() {
  return (
    <AdminShell adminOnly>
      <Template />
    </AdminShell>
  );
}

type Draft = {
  id?: number; code: string; name: string; help: string; mandatory: boolean;
  numberRequirement: DocumentType["numberRequirement"]; expiryRequired: boolean; active: boolean; position: number;
  pages: { id?: number; name: string; camera: "BACK" | "FRONT" }[];
};

const EMPTY: Draft = {
  code: "", name: "", help: "", mandatory: true, numberRequirement: "OPTIONAL", expiryRequired: false, active: true,
  position: 10, pages: [{ name: "Recto", camera: "BACK" }, { name: "Verso", camera: "BACK" }],
};

const NUMBER: Record<Draft["numberRequirement"], string> = {
  NONE: "Pas de numéro", OPTIONAL: "Numéro facultatif", REQUIRED: "Numéro obligatoire",
};

function Template() {
  const { token } = useAuth();
  const [types, setTypes] = useState<DocumentType[] | null>(null);
  const [draft, setDraft] = useState<Draft | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    try {
      setTypes(await api<DocumentType[]>("/v1/delivery/document-types", { token: await token() }));
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Erreur inattendue.");
    }
  }, [token]);
  useEffect(() => { load(); }, [load]);

  const save = async () => {
    if (!draft) return;
    setBusy(true);
    setError(null);
    try {
      const body = { ...draft, help: draft.help || null };
      await api(draft.id ? `/v1/delivery/document-types/${draft.id}` : "/v1/delivery/document-types",
        { method: draft.id ? "PUT" : "POST", body, token: await token() });
      setDraft(null);
      await load();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Erreur inattendue.");
    } finally {
      setBusy(false);
    }
  };

  const edit = (t: DocumentType) => setDraft({ ...t, help: t.help ?? "", pages: t.pages.map((p) => ({ ...p })) });
  const set = (patch: Partial<Draft>) => setDraft((d) => (d ? { ...d, ...patch } : d));
  const setPage = (i: number, patch: Partial<Draft["pages"][number]>) =>
    setDraft((d) => (d ? { ...d, pages: d.pages.map((p, j) => (j === i ? { ...p, ...patch } : p)) } : d));

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Documents demandés</h1>
          <p className="muted">Ce que chaque livreur fournit à l&apos;inscription. Un nouveau document obligatoire est demandé aux livreurs déjà actifs, avec 7 jours pour le fournir.</p>
        </div>
        {!draft && <button className="btn btn-navy" onClick={() => setDraft({ ...EMPTY })}>Ajouter un document</button>}
      </div>
      {error && <div className="alert alert-error" role="alert">{error}</div>}

      {draft && (
        <section className="card">
          <h2 className="card-title">{draft.id ? `Modifier : ${draft.name}` : "Nouveau document"}</h2>
          <div className="fields two">
            <label className="field">
              <span className="field-label">Nom affiché au livreur</span>
              <input value={draft.name} onChange={(e) => set({ name: e.target.value })} placeholder="Ex. : Permis de conduire" />
            </label>
            <label className="field">
              <span className="field-label">Code</span>
              <input value={draft.code} disabled={!!draft.id} onChange={(e) => set({ code: e.target.value.toUpperCase().replace(/[^A-Z0-9_]/g, "") })}
                placeholder="Ex. : PERMIS" />
              <span className="field-hint">Identifiant interne, ne change plus ensuite.</span>
            </label>
          </div>
          <label className="field">
            <span className="field-label">Conseil affiché (facultatif)</span>
            <input value={draft.help} onChange={(e) => set({ help: e.target.value })} placeholder="Ex. : Photographiez le document en entier, sans reflet." />
          </label>
          <div className="fields two">
            <label className="field">
              <span className="field-label">Numéro du document</span>
              <select className="select" value={draft.numberRequirement} onChange={(e) => set({ numberRequirement: e.target.value as Draft["numberRequirement"] })}>
                {Object.entries(NUMBER).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
              </select>
            </label>
            <label className="field">
              <span className="field-label">Ordre d&apos;affichage</span>
              <input type="number" value={draft.position} onChange={(e) => set({ position: Number(e.target.value) })} />
            </label>
          </div>
          <label className="check"><input type="checkbox" checked={draft.mandatory} onChange={(e) => set({ mandatory: e.target.checked })} /> Obligatoire</label>
          <label className="check"><input type="checkbox" checked={draft.expiryRequired} onChange={(e) => set({ expiryRequired: e.target.checked })} /> Demander la date d&apos;expiration (le livreur est prévenu 30 jours avant, bloqué à l&apos;expiration)</label>
          <label className="check"><input type="checkbox" checked={draft.active} onChange={(e) => set({ active: e.target.checked })} /> Actif (décocher pour ne plus le demander)</label>

          <div className="fields">
            <span className="field-label">Pages à photographier</span>
            {draft.pages.map((p, i) => (
              <div key={i} className="page-row">
                <input value={p.name} onChange={(e) => setPage(i, { name: e.target.value })} placeholder="Ex. : Recto" aria-label={`Nom de la page ${i + 1}`} />
                <select className="select" value={p.camera} onChange={(e) => setPage(i, { camera: e.target.value as "BACK" | "FRONT" })} aria-label="Appareil photo">
                  <option value="BACK">Caméra arrière (document)</option>
                  <option value="FRONT">Caméra avant (selfie)</option>
                </select>
                <button className="btn btn-outline btn-xs" disabled={draft.pages.length <= 1}
                  onClick={() => set({ pages: draft.pages.filter((_, j) => j !== i) })}>Retirer</button>
              </div>
            ))}
            {draft.pages.length < 6 && (
              <button className="btn btn-outline btn-sm" onClick={() => set({ pages: [...draft.pages, { name: "", camera: "BACK" }] })}>Ajouter une page</button>
            )}
          </div>
          <div className="actions-row">
            <button className="btn btn-sun" disabled={busy || !draft.name.trim() || (!draft.id && !draft.code)} onClick={save}>Enregistrer</button>
            <button className="btn btn-outline" disabled={busy} onClick={() => { setDraft(null); setError(null); }}>Annuler</button>
          </div>
        </section>
      )}

      {types === null ? <div className="skeleton tall" /> : (
        <div className="list">
          {types.map((t) => (
            <button key={t.id} className="row-card row-button" onClick={() => edit(t)}>
              <div className="row-main">
                <p className="row-title">{t.name} <span className="muted small">{t.code}</span></p>
                <p className="row-sub">{t.pages.map((p) => p.name).join(" · ")}</p>
                <p className="row-meta">
                  {NUMBER[t.numberRequirement]}{t.expiryRequired ? " · date d'expiration" : ""}
                </p>
              </div>
              <div className="row-side">
                <span className={`badge ${!t.active ? "badge-off" : t.mandatory ? "badge-move" : "badge-off"}`}>
                  {!t.active ? "Désactivé" : t.mandatory ? "Obligatoire" : "Facultatif"}
                </span>
              </div>
            </button>
          ))}
        </div>
      )}
    </>
  );
}
