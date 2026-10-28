"use client";

import { useState, type FormEvent } from "react";
import { API_URL, CONTACT_EMAIL, type Lang } from "@/config/site";
import { track } from "@/lib/track";

export type CallbackTexts = {
  title: string;
  lead: string;
  name: string;
  phone: string;
  phoneHint: string;
  city: string;
  submit: string;
  sending: string;
  success: string;
  error: string;
  phoneError: string;
  consent: string;
  privacyLink: string;
};

/**
 * Demande de rappel rapide (nom, téléphone, ville) : visible tôt sur la page, sans passer par le test.
 * `interest` = ce que la personne regardait (programme, ville, guide), transmis au conseiller.
 */
export function QuickCallback({ t, lang, cities, interest, defaultCity, privacyHref, place }: {
  t: CallbackTexts;
  lang: Lang;
  cities: string[];
  interest: string;
  defaultCity?: string;
  privacyHref: string;
  place: string;
}) {
  const [state, setState] = useState<"idle" | "sending" | "done" | "error">("idle");
  const [errorText, setErrorText] = useState<string | null>(null);
  const id = (s: string) => `cb-${place}-${s}`;

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (state === "sending") return;
    const data = Object.fromEntries(new FormData(e.currentTarget).entries()) as Record<string, string>;
    setState("sending");
    setErrorText(null);
    try {
      const res = await fetch(`${API_URL}/v1/contact-requests`, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Accept-Language": lang },
        body: JSON.stringify({ ...data, product: "go", kind: "callback", lang, activity: interest }),
      });
      if (res.ok) {
        setState("done");
        track("lead_submit", { product: "go", kind: "callback", place });
        return;
      }
      const problem = await res.json().catch(() => null);
      setErrorText(res.status === 422 && /téléphone|phone/i.test(problem?.detail ?? "") ? t.phoneError : null);
      track("lead_error", { product: "go", status: res.status });
      setState("error");
    } catch {
      track("lead_error", { product: "go", status: 0 });
      setState("error");
    }
  }

  return (
    <div className="callback">
      <div className="callback-head">
        <h2 className="callback-title">{t.title}</h2>
        <p>{t.lead}</p>
      </div>
      {state === "done" ? (
        <p className="form-success-text" role="status">{t.success}</p>
      ) : (
        <form className="callback-form" onSubmit={submit}>
          <div className="field">
            <label htmlFor={id("name")}>{t.name}</label>
            <input id={id("name")} name="contactName" required maxLength={120} autoComplete="name" />
          </div>
          <div className="field">
            <label htmlFor={id("phone")}>{t.phone}</label>
            <input id={id("phone")} name="phone" type="tel" required minLength={8} maxLength={30} autoComplete="tel" inputMode="tel" placeholder="6 99 12 34 56" aria-describedby={id("hint")} />
            <small id={id("hint")}>{t.phoneHint}</small>
          </div>
          <div className="field">
            <label htmlFor={id("city")}>{t.city}</label>
            <select id={id("city")} name="city" defaultValue={defaultCity && cities.includes(defaultCity) ? defaultCity : cities[0]}>
              {cities.map((c) => (
                <option key={c}>{c}</option>
              ))}
              <option>{lang === "fr" ? "Autre ville" : "Other city"}</option>
            </select>
          </div>
          <div className="hp" aria-hidden="true">
            <label htmlFor={id("website")}>Website</label>
            <input id={id("website")} name="website" tabIndex={-1} autoComplete="off" />
          </div>
          <button type="submit" className="btn btn-accent callback-submit" disabled={state === "sending"} aria-busy={state === "sending"}>
            {state === "sending" ? t.sending : t.submit}
          </button>
          {state === "error" && (
            <p className="form-error callback-error" role="alert">
              {errorText ?? t.error.replace("{email}", CONTACT_EMAIL)}
            </p>
          )}
          <p className="form-consent callback-consent">
            {t.consent} <a href={privacyHref}>{t.privacyLink}</a>
          </p>
        </form>
      )}
    </div>
  );
}
