"use client";

import { useCallback, useEffect, useState } from "react";
import { AdminShell } from "@/components/Admin";
import { api, ApiError, type TeamMember } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { formatPhone } from "@/lib/format";

export default function TeamPage() {
  return (
    <AdminShell adminOnly>
      <Team />
    </AdminShell>
  );
}

const ROLES: Record<string, { label: string; help: string }> = {
  validateur: { label: "Validateur", help: "vérifie les documents et active, refuse ou suspend les livreurs" },
  operateur: { label: "Opérateur", help: "suit les courses, réattribue, aide les clients" },
  admin: { label: "Administrateur", help: "règle les documents demandés et gère l'équipe" },
};

function Team() {
  const { token, user } = useAuth();
  const [members, setMembers] = useState<TeamMember[] | null>(null);
  const [login, setLogin] = useState("");
  const [role, setRole] = useState("validateur");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const run = useCallback(async (path: string, method = "GET", body?: unknown) => {
    setBusy(true);
    setError(null);
    try {
      setMembers(await api<TeamMember[]>(path, { method, body, token: await token() }));
      return true;
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Erreur inattendue.");
      setMembers((m) => m ?? []);
      return false;
    } finally {
      setBusy(false);
    }
  }, [token]);

  useEffect(() => { run("/v1/admin/team"); }, [run]);

  const add = async () => {
    if (await run("/v1/admin/team", "POST", { login, role })) setLogin("");
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Équipe Izimboa</h1>
          <p className="muted">Donnez un rôle à une personne qui s&apos;est déjà connectée une fois à Izimboa (par WhatsApp ou e-mail).</p>
        </div>
      </div>
      {error && <div className="alert alert-error" role="alert">{error}</div>}

      <section className="card">
        <h2 className="card-title">Ajouter un membre</h2>
        <div className="fields two">
          <label className="field">
            <span className="field-label">Numéro WhatsApp ou e-mail</span>
            <input value={login} onChange={(e) => setLogin(e.target.value)} placeholder="Ex. : 6 99 00 00 00" />
          </label>
          <label className="field">
            <span className="field-label">Rôle</span>
            <select className="select" value={role} onChange={(e) => setRole(e.target.value)}>
              {Object.entries(ROLES).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}
            </select>
            <span className="field-hint">{ROLES[role].help}</span>
          </label>
        </div>
        <div className="actions-row">
          <button className="btn btn-sun" disabled={busy || !login.trim()} onClick={add}>Donner le rôle</button>
        </div>
      </section>

      {members === null ? <div className="skeleton tall" /> : members.length === 0 ? (
        <div className="empty"><p className="muted">Personne dans l&apos;équipe pour l&apos;instant.</p></div>
      ) : (
        <div className="list">
          {members.map((m) => (
            <div key={m.id} className="row-card">
              <div className="row-main">
                <p className="row-title">{m.name ?? m.login ?? "Sans nom"}</p>
                <p className="row-sub">{[m.phone && formatPhone(m.phone), m.email].filter(Boolean).join(" · ") || m.login}</p>
              </div>
              <div className="row-side">
                {m.roles.map((r) => (
                  <span key={r} className="role-chip">
                    {ROLES[r]?.label ?? r}
                    {!(r === "admin" && m.id === user?.profile.sub) && (
                      <button className="role-remove" disabled={busy} aria-label={`Retirer le rôle ${ROLES[r]?.label ?? r}`}
                        onClick={() => run(`/v1/admin/team/${m.id}/roles/${r}`, "DELETE")}>×</button>
                    )}
                  </span>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
