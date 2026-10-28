"use client";

import { useEffect, useRef, useState, type FormEvent } from "react";
import { API_URL, CONTACT_EMAIL, type Lang } from "@/config/site";
import type { Destination, Goal } from "@/content/go-types";
import type { GoUi } from "@/content/cm/go/ui";
import { track } from "@/lib/track";

export type ProgramCard = {
  key: string;
  name: string;
  destination: Destination;
  goal: Goal;
  href: string;
  status?: string;
  conditions: string[];
  source?: { label: string; url: string };
};
type Answers = { goal?: Goal; education?: string; experience?: string; language?: string; budget?: string; age?: string; destination?: string };
type Step = keyof Answers;

/** L'expérience n'est demandée que pour les projets de travail ou d'installation : on ne collecte que l'utile. */
function stepsFor(a: Answers): Step[] {
  const needsExperience = a.goal === "work" || a.goal === "permanent";
  return ["goal", "education", ...(needsExperience ? (["experience"] as Step[]) : []), "language", "budget", "age", "destination"];
}

/** Affinité de chaque programme avec chaque objectif (0 à 10). */
const AFFINITY: Record<string, Partial<Record<Goal, number>>> = {
  "ca-express-entry": { permanent: 10, work: 6 },
  "ca-study-permit": { study: 10, permanent: 3 },
  "ca-francophone-mobility": { work: 10, permanent: 6 },
  "ca-pnp": { permanent: 9, work: 5 },
  "ca-quebec": { permanent: 9, work: 4 },
  "de-study": { study: 10 },
  "de-ausbildung": { training: 10, work: 6, study: 4 },
  "de-skilled-work": { work: 9, permanent: 5 },
  "fr-study": { study: 10 },
  "fr-talent-work": { work: 8, permanent: 3 },
  "be-study": { study: 9 },
};

/** Ajustements selon le profil : une orientation simple et lisible, revue ensuite par un conseiller. */
const ADJUST: Record<string, Record<string, Record<string, number>>> = {
  language: {
    de: { "de-study": 4, "de-ausbildung": 4, "de-skilled-work": 4 },
    fr: { "ca-francophone-mobility": 3, "ca-quebec": 3, "fr-study": 2, "fr-talent-work": 2, "be-study": 2, "de-study": -3, "de-ausbildung": -2, "de-skilled-work": -3, "ca-express-entry": -1 },
    "fr-en": { "ca-express-entry": 2, "ca-pnp": 2, "ca-study-permit": 2, "ca-francophone-mobility": 2 },
    en: { "ca-express-entry": 2, "ca-pnp": 2, "ca-study-permit": 2, "de-study": 1, "de-ausbildung": -2, "de-skilled-work": -1, "ca-francophone-mobility": -10, "ca-quebec": -6, "fr-study": -3, "fr-talent-work": -4, "be-study": -3 },
  },
  education: {
    secondary: { "de-ausbildung": 3, "ca-express-entry": -6, "de-skilled-work": -6, "fr-talent-work": -6, "ca-pnp": -3, "ca-quebec": -3, "ca-study-permit": -3, "de-study": -4, "fr-study": -3, "be-study": -3 },
    bac: { "de-ausbildung": 2, "ca-express-entry": -3, "de-skilled-work": -4, "fr-talent-work": -5 },
    bachelor: { "ca-express-entry": 1, "de-skilled-work": 2 },
    master: { "ca-express-entry": 3, "de-skilled-work": 3, "fr-talent-work": 3 },
  },
  experience: {
    none: { "ca-express-entry": -4, "de-skilled-work": -4, "fr-talent-work": -4, "ca-pnp": -3, "ca-quebec": -3, "de-ausbildung": 2 },
    "3+": { "ca-express-entry": 2, "de-skilled-work": 2, "ca-pnp": 1, "ca-quebec": 1 },
  },
  age: {
    "18-24": { "ca-study-permit": 1, "de-study": 1, "fr-study": 1, "be-study": 1, "de-ausbildung": 1 },
    "36+": { "ca-express-entry": -3, "de-ausbildung": -1, "ca-study-permit": -1 },
  },
  budget: {
    low: { "ca-study-permit": -6, "de-study": -3, "fr-study": -1, "be-study": -2, "de-ausbildung": 3, "ca-francophone-mobility": 1 },
    mid: { "ca-study-permit": -2 },
    high: { "ca-study-permit": 2 },
  },
};

function score(p: ProgramCard, a: Answers) {
  let s = AFFINITY[p.key]?.[a.goal!] ?? (p.goal === a.goal ? 8 : 0);
  for (const k of ["language", "education", "experience", "age", "budget"] as const) {
    const v = a[k];
    if (v) s += ADJUST[k][v]?.[p.key] ?? 0;
  }
  if (a.destination && a.destination !== "any" && p.destination === a.destination) s += 3;
  if (p.status === "limited") s -= 1;
  return s;
}

/** Au plus 3 pistes, et seulement celles qui atteignent un score suffisant : sinon, on le dit clairement. */
function suggest(programs: ProgramCard[], a: Answers): ProgramCard[] {
  if (!a.goal) return [];
  return programs
    .map((p) => ({ p, s: score(p, a) }))
    .filter((x) => x.s >= 7)
    .sort((x, y) => y.s - x.s)
    .slice(0, 3)
    .map((x) => x.p);
}

function warningsFor(a: Answers, results: ProgramCard[]): string[] {
  const w: string[] = [];
  const has = (d: Destination) => results.some((r) => r.destination === d);
  if (a.goal === "study" && a.budget === "low") w.push("budgetStudy");
  if (has("de") && a.language !== "de") w.push("german");
  if ((has("fr") || has("be") || results.some((r) => r.key === "ca-francophone-mobility" || r.key === "ca-quebec")) && a.language === "en") w.push("french");
  if ((a.goal === "work" || a.goal === "permanent") && a.experience === "none") w.push("experience");
  if (results.some((r) => r.key === "ca-express-entry") && (a.age === "30-35" || a.age === "36+")) w.push("age");
  if ((a.goal === "study") && a.education === "secondary") w.push("secondary");
  if (a.destination && a.destination !== "any" && results.length > 0 && results[0].destination !== a.destination) w.push("destination");
  return w;
}

export function EligibilityTest({ t, lang, programs, destinations, privacyHref, defaultCity, cities }: {
  t: GoUi["test"];
  lang: Lang;
  programs: ProgramCard[];
  destinations: Record<Destination, string>;
  privacyHref: string;
  defaultCity?: string;
  cities: string[];
}) {
  const [answers, setAnswers] = useState<Answers>({});
  const [step, setStep] = useState(0);
  const [state, setState] = useState<"idle" | "sending" | "done" | "error">("idle");
  const [errorText, setErrorText] = useState<string | null>(null);
  const headingRef = useRef<HTMLHeadingElement>(null);
  const firstRender = useRef(true);

  const steps = stepsFor(answers);
  const current = steps[step];
  const finished = step >= steps.length;
  const results = finished ? suggest(programs, answers) : [];
  const warnings = finished ? warningsFor(answers, results) : [];

  // Accessibilité : à chaque question, le focus va sur l'intitulé (lecteurs d'écran, clavier).
  useEffect(() => {
    if (firstRender.current) {
      firstRender.current = false;
      return;
    }
    headingRef.current?.focus();
  }, [step]);

  useEffect(() => {
    if (finished) track("eligibility_complete", { goal: answers.goal ?? "", results: results.length });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [finished]);

  const options: Record<Step, { value: string; label: string }[]> = {
    goal: t.goalOptions,
    education: t.educationOptions,
    experience: t.experienceOptions,
    age: t.ageOptions,
    language: t.languageOptions,
    budget: t.budgetOptions,
    destination: t.destinationOptions,
  };
  const label = (k: Step) => options[k].find((o) => o.value === answers[k])?.label;

  function choose(value: string) {
    if (step === 0) track("eligibility_start", { goal: value });
    track("eligibility_step", { step: step + 1, question: current });
    setAnswers((a) => {
      const next = { ...a, [current]: value };
      if (current === "goal" && value !== "work" && value !== "permanent") delete next.experience;
      return next;
    });
    setStep((s) => s + 1);
  }

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (state === "sending") return; // pas de double envoi
    const form = e.currentTarget;
    const data = Object.fromEntries(new FormData(form).entries()) as Record<string, string>;
    const summary = [
      ...steps.filter((k) => answers[k]).map((k) => `${t.q[k]} ${label(k)}`),
      `→ ${results.map((r) => r.name).join(", ") || "-"}`,
    ].join("\n");
    setState("sending");
    setErrorText(null);
    try {
      const res = await fetch(`${API_URL}/v1/contact-requests`, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Accept-Language": lang },
        body: JSON.stringify({ ...data, product: "go", kind: "eligibility", lang, activity: label("goal"), message: summary }),
      });
      if (res.ok) {
        setState("done");
        track("lead_submit", { product: "go", kind: "eligibility" });
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

  function restart() {
    setAnswers({});
    setStep(0);
    setState("idle");
    setErrorText(null);
  }

  return (
    <div className="elig">
      {!finished ? (
        <div className="elig-question">
          <p className="elig-progress" aria-live="polite">
            {t.step} {step + 1} / {steps.length}
            <span className="elig-bar" aria-hidden="true">
              <span style={{ width: `${((step + 1) / steps.length) * 100}%` }} />
            </span>
          </p>
          <h3 className="elig-q" ref={headingRef} tabIndex={-1} id="elig-q">
            {t.q[current]}
          </h3>
          <div className="elig-options" role="group" aria-labelledby="elig-q">
            {options[current].map((o) => (
              <button key={o.value} type="button" className={"elig-option" + (answers[current] === o.value ? " is-selected" : "")} aria-pressed={answers[current] === o.value} onClick={() => choose(o.value)}>
                {o.label}
              </button>
            ))}
          </div>
          {step > 0 && (
            <button type="button" className="elig-back" onClick={() => setStep((s) => s - 1)}>
              ← {t.back}
            </button>
          )}
        </div>
      ) : (
        <div className="elig-result">
          <h3 className="elig-q" ref={headingRef} tabIndex={-1}>
            {results.length ? t.resultTitle : t.noResult}
          </h3>
          {results.length > 0 && <p className="muted">{t.resultLead}</p>}

          <details className="elig-profile">
            <summary>{t.profileTitle}</summary>
            <dl>
              {steps.filter((k) => answers[k]).map((k) => (
                <div key={k}>
                  <dt>{t.q[k]}</dt>
                  <dd>{label(k)}</dd>
                </div>
              ))}
            </dl>
          </details>

          <ul className="elig-programs">
            {results.map((r) => (
              <li key={r.key} className="elig-program">
                <p className="program-tags">
                  <span className="dest-chip">{destinations[r.destination]}</span>
                  {r.status === "limited" && <span className="goal-chip">{t.restricted}</span>}
                </p>
                <strong className="elig-program-name">{r.name}</strong>
                {r.conditions.length > 0 && (
                  <>
                    <span className="elig-small-title">{t.keyConditions}</span>
                    <ul className="dot-list">
                      {r.conditions.map((c) => (
                        <li key={c}>{c}</li>
                      ))}
                    </ul>
                  </>
                )}
                <span className="elig-links">
                  <a href={r.href} onClick={() => track("eligibility_result_click", { program: r.key })}>
                    {t.seeProgram} →
                  </a>
                  {r.source && (
                    <a href={r.source.url} target="_blank" rel="noopener">
                      {t.officialSource} : {r.source.label}
                    </a>
                  )}
                </span>
              </li>
            ))}
          </ul>

          {warnings.length > 0 && (
            <div className="elig-warnings">
              <span className="elig-small-title">{t.warningsTitle}</span>
              <ul className="dot-list">
                {warnings.map((w) => (
                  <li key={w}>{t.warnings[w]}</li>
                ))}
              </ul>
            </div>
          )}

          <div className="elig-contact">
            <h3>{t.contactTitle}</h3>
            {state === "done" ? (
              <p className="form-success-text" role="status">
                {t.success}
              </p>
            ) : (
              <form className="biz-form" onSubmit={submit}>
                <p className="muted">{t.contactLead}</p>
                <div className="field">
                  <label htmlFor="el-name">{t.name}</label>
                  <input id="el-name" name="contactName" required maxLength={120} autoComplete="name" />
                </div>
                <div className="field-row">
                  <div className="field">
                    <label htmlFor="el-phone">{t.phone}</label>
                    <input id="el-phone" name="phone" type="tel" required minLength={8} maxLength={30} autoComplete="tel" inputMode="tel" placeholder="6 99 12 34 56" aria-describedby="el-phone-hint" />
                    <small id="el-phone-hint">{t.phoneHint}</small>
                  </div>
                  <div className="field">
                    <label htmlFor="el-city">{t.city}</label>
                    <CitySelect id="el-city" defaultCity={defaultCity} lang={lang} cities={cities} />
                  </div>
                </div>
                <div className="field">
                  <label htmlFor="el-email">
                    {t.email} <span className="optional">{t.optional}</span>
                  </label>
                  <input id="el-email" name="email" type="email" maxLength={160} autoComplete="email" />
                </div>
                <div className="hp" aria-hidden="true">
                  <label htmlFor="el-website">Website</label>
                  <input id="el-website" name="website" tabIndex={-1} autoComplete="off" />
                </div>
                {state === "error" && (
                  <p className="form-error" role="alert">
                    {errorText ?? t.error.replace("{email}", CONTACT_EMAIL)}
                  </p>
                )}
                <button type="submit" className="btn btn-accent biz-submit" disabled={state === "sending"} aria-busy={state === "sending"}>
                  {state === "sending" ? t.sending : t.submit}
                </button>
                <p className="form-consent">
                  {t.consent} <a href={privacyHref}>{t.privacyLink}</a>
                </p>
              </form>
            )}
          </div>
          <button type="button" className="elig-back" onClick={restart}>
            ↺ {t.restart}
          </button>
        </div>
      )}
      <p className="elig-disclaimer">{t.disclaimer}</p>
    </div>
  );
}

function CitySelect({ id, defaultCity, lang, cities }: { id: string; defaultCity?: string; lang: Lang; cities: string[] }) {
  const other = lang === "fr" ? "Autre ville" : "Other city";
  return (
    <select id={id} name="city" defaultValue={defaultCity && cities.includes(defaultCity) ? defaultCity : cities[0]}>
      {cities.map((c) => (
        <option key={c}>{c}</option>
      ))}
      <option>{other}</option>
    </select>
  );
}
