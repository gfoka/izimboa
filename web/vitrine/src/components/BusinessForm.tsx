"use client";

import { useState, type FormEvent } from "react";
import { API_URL, CONTACT_EMAIL, type Lang, type ProductKey } from "@/config/site";
import type { BusinessForm as Texts } from "@/content/types";

type State = "idle" | "sending" | "done" | "error";

/**
 * Formulaire « Entreprises » : la demande part à l'API (enregistrée, e-mail à l'équipe et accusé de réception).
 * Sans JavaScript ou en cas d'échec, l'adresse e-mail reste proposée.
 */
export function BusinessForm({ t, lang, product, privacyHref }: { t: Texts; lang: Lang; product: ProductKey; privacyHref: string }) {
  const [state, setState] = useState<State>("idle");
  const [message, setMessage] = useState<string | null>(null);

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = e.currentTarget;
    const data = Object.fromEntries(new FormData(form).entries()) as Record<string, string>;
    setState("sending");
    setMessage(null);
    try {
      const res = await fetch(`${API_URL}/v1/contact-requests`, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Accept-Language": lang },
        body: JSON.stringify({ ...data, product, kind: "business", lang }),
      });
      if (res.ok) {
        setState("done");
        form.reset();
        return;
      }
      const problem = await res.json().catch(() => null);
      // Les messages de l'API sont en français ; en anglais on garde le message générique.
      setMessage(lang === "fr" && problem?.detail && res.status !== 400 ? problem.detail : null);
      setState("error");
    } catch {
      setState("error");
    }
  }

  if (state === "done") {
    return (
      <div className="form-success" role="status">
        <p>{t.success}</p>
        <button type="button" className="btn btn-outline btn-sm" onClick={() => setState("idle")}>
          {t.successAgain}
        </button>
      </div>
    );
  }

  return (
    <form className="biz-form" onSubmit={submit} noValidate={false}>
      <div className="field">
        <label htmlFor="bf-company">{t.company}</label>
        <input id="bf-company" name="company" required maxLength={120} autoComplete="organization" />
      </div>
      <div className="field">
        <label htmlFor="bf-name">{t.contactName}</label>
        <input id="bf-name" name="contactName" required maxLength={120} autoComplete="name" />
      </div>
      <div className="field">
        <label htmlFor="bf-phone">{t.phone}</label>
        <input id="bf-phone" name="phone" type="tel" required maxLength={30} autoComplete="tel" inputMode="tel" placeholder="6 99 12 34 56" aria-describedby="bf-phone-hint" />
        <small id="bf-phone-hint">{t.phoneHint}</small>
      </div>
      <div className="field">
        <label htmlFor="bf-email">
          {t.email} <span className="optional">{t.optional}</span>
        </label>
        <input id="bf-email" name="email" type="email" maxLength={160} autoComplete="email" aria-describedby="bf-email-hint" />
        <small id="bf-email-hint">{t.emailHint}</small>
      </div>
      <div className="field-row">
        <div className="field">
          <label htmlFor="bf-city">{t.city}</label>
          <select id="bf-city" name="city" defaultValue={t.cities[0]}>
            {t.cities.map((c) => (
              <option key={c}>{c}</option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="bf-volume">{t.volume}</label>
          <select id="bf-volume" name="volume" defaultValue="">
            <option value="">-</option>
            {t.volumes.map((v) => (
              <option key={v.value} value={v.value}>
                {v.label}
              </option>
            ))}
          </select>
        </div>
      </div>
      <div className="field">
        <label htmlFor="bf-activity">{t.activity}</label>
        <input id="bf-activity" name="activity" maxLength={120} placeholder={t.activityPlaceholder} />
      </div>
      <div className="field">
        <label htmlFor="bf-message">
          {t.message} <span className="optional">{t.optional}</span>
        </label>
        <textarea id="bf-message" name="message" rows={3} maxLength={2000} />
      </div>
      {/* Piège à robots : invisible pour les personnes. */}
      <div className="hp" aria-hidden="true">
        <label htmlFor="bf-website">Website</label>
        <input id="bf-website" name="website" tabIndex={-1} autoComplete="off" />
      </div>
      {state === "error" && (
        <p className="form-error" role="alert">
          {message ?? t.error.replace("{email}", CONTACT_EMAIL)}
        </p>
      )}
      <button type="submit" className="btn btn-accent biz-submit" disabled={state === "sending"}>
        {state === "sending" ? t.sending : t.submit}
      </button>
      <p className="form-consent">
        {t.consent} <a href={privacyHref}>{t.privacyLink}</a>
      </p>
    </form>
  );
}
