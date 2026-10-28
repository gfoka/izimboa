"use client";

import { useEffect, useState } from "react";
import type { Lang } from "@/config/site";

const KEY = "izimboa-consent";
type Choice = "granted" | "denied";

const TEXT: Record<Lang, { text: string; accept: string; refuse: string; more: string }> = {
  fr: {
    text: "Nous utilisons des mesures d'audience (Google Analytics) pour savoir quelles pages vous sont utiles. Rien n'est activé sans votre accord.",
    accept: "Accepter",
    refuse: "Refuser",
    more: "En savoir plus",
  },
  en: {
    text: "We use audience measurement (Google Analytics) to learn which pages are useful to you. Nothing is enabled without your consent.",
    accept: "Accept",
    refuse: "Decline",
    more: "Learn more",
  },
};

function readChoice(): Choice | null {
  try {
    const v = localStorage.getItem(KEY);
    return v === "granted" || v === "denied" ? v : null;
  } catch {
    return null;
  }
}

function loadGa(id: string) {
  if (document.getElementById("ga4")) return;
  const w = window as unknown as { dataLayer: unknown[]; gtag: (...args: unknown[]) => void };
  w.dataLayer = w.dataLayer || [];
  w.gtag = function gtag() {
    // eslint-disable-next-line prefer-rest-params
    w.dataLayer.push(arguments);
  };
  w.gtag("consent", "default", { ad_storage: "denied", ad_user_data: "denied", ad_personalization: "denied", analytics_storage: "granted" });
  w.gtag("js", new Date());
  w.gtag("config", id, { anonymize_ip: true });
  const s = document.createElement("script");
  s.id = "ga4";
  s.async = true;
  s.src = `https://www.googletagmanager.com/gtag/js?id=${encodeURIComponent(id)}`;
  document.head.appendChild(s);
}

/** Bandeau de consentement + Google Analytics 4 chargé uniquement après « Accepter ». */
export function Analytics({ id, lang, privacyHref }: { id: string | null; lang: Lang; privacyHref: string }) {
  const [choice, setChoice] = useState<Choice | null | undefined>(undefined);

  useEffect(() => {
    const c = readChoice();
    setChoice(c);
    if (id && c === "granted") loadGa(id);
  }, [id]);

  if (!id || choice !== null) return null;
  const t = TEXT[lang];

  function decide(c: Choice) {
    try {
      localStorage.setItem(KEY, c);
    } catch {
      /* navigation privée : le choix vaut pour cette page */
    }
    setChoice(c);
    if (c === "granted" && id) loadGa(id);
  }

  return (
    <div className="consent" role="dialog" aria-live="polite" aria-label="Cookies">
      <p>
        {t.text} <a href={privacyHref}>{t.more}</a>
      </p>
      <div className="consent-actions">
        <button type="button" className="btn btn-sm btn-outline" onClick={() => decide("denied")}>
          {t.refuse}
        </button>
        <button type="button" className="btn btn-sm btn-navy" onClick={() => decide("granted")}>
          {t.accept}
        </button>
      </div>
    </div>
  );
}
