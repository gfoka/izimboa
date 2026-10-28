import type { ReactNode } from "react";
import { PRODUCT_COLORS, PRODUCT_DARK, type Lang } from "@/config/site";
import { absoluteUrl, pathFor, subPathFor } from "@/config/routes";
import type { Dict } from "@/content/types";
import type { City, Program } from "@/content/go-types";
import { CITIES, DESTINATIONS, GOALS, PROGRAMS, goUi, relatedPrograms } from "@/content/cm/go";
import { Breadcrumbs } from "@/components/Breadcrumbs";
import { JsonLd } from "@/components/JsonLd";
import { Icon } from "@/components/Icon";
import { ProductSymbol } from "@/components/Logo";
import { ProgramGrid, type GridItem } from "@/components/ProgramGrid";
import { EligibilityTest, type ProgramCard } from "@/components/EligibilityTest";
import { faqLd, goServiceLd } from "@/lib/seo";

type Ctx = { country: string; lang: Lang; dict: Dict };

const firstSentence = (s: string) => {
  const i = s.indexOf(". ");
  return i > 0 && i < 220 ? s.slice(0, i + 1) : s.length > 220 ? s.slice(0, 217) + "…" : s;
};

function formatDate(iso: string, lang: Lang) {
  return new Date(iso + "T12:00:00Z").toLocaleDateString(lang === "fr" ? "fr-FR" : "en-GB", { day: "numeric", month: "long", year: "numeric" });
}

export const programHref = (country: string, lang: Lang, p: Program) => subPathFor(country, lang, "go", p.slug[lang]);
export const cityHref = (country: string, lang: Lang, c: City) => subPathFor(country, lang, "go", c.slug[lang]);

function programCards(country: string, lang: Lang): ProgramCard[] {
  return PROGRAMS.filter((p) => p.status !== "paused").map((p) => ({ key: p.key, name: p[lang].name, destination: p.destination, goal: p.goal, href: programHref(country, lang, p) }));
}

/** Test d'éligibilité, présent sur la page Go, chaque programme et chaque ville (ancre #test). */
export function GoTest({ country, lang, city }: Ctx & { city?: string }) {
  const ui = goUi(lang);
  return (
    <section id="test" className="section section-tint" aria-labelledby="test-titre">
      <div className="container biz-grid">
        <div>
          <h2 id="test-titre" className="section-title">{ui.test.title}</h2>
          <p className="section-lead">{ui.test.lead}</p>
        </div>
        <div className="biz-card">
          <EligibilityTest
            t={ui.test}
            lang={lang}
            programs={programCards(country, lang)}
            destinations={ui.destinations}
            privacyHref={pathFor(country, lang, "privacy")}
            defaultCity={city}
            cities={ui.cityOptions}
          />
        </div>
      </div>
    </section>
  );
}

function CityLinks({ country, lang, exclude }: Ctx & { exclude?: string }) {
  return (
    <ul className="city-links">
      {CITIES.filter((c) => c.key !== exclude).map((c) => (
        <li key={c.key}>
          <a href={cityHref(country, lang, c)}>
            <strong>{c.name}</strong>
            <small>{c.region[lang]}</small>
          </a>
        </li>
      ))}
    </ul>
  );
}

/** Sections propres à Izimboa Go, insérées dans sa page produit : programmes, comparatif, test, villes. */
export function GoHub(ctx: Ctx) {
  const { country, lang } = ctx;
  const ui = goUi(lang);
  const items: GridItem[] = PROGRAMS.map((p) => ({
    key: p.key,
    name: p[lang].name,
    intro: firstSentence(p[lang].intro),
    destination: p.destination,
    goal: p.goal,
    href: programHref(country, lang, p),
    status: p.status,
  }));
  return (
    <>
      <section id="programmes" className="section">
        <div className="container">
          <div className="section-head">
            <h2 className="section-title">{ui.hub.title}</h2>
            <p className="section-lead">{ui.hub.lead}</p>
          </div>
          <ProgramGrid
            items={items}
            goals={GOALS}
            goalLabels={ui.goals}
            destinations={ui.destinations}
            all={ui.hub.all}
            filterLabel={ui.hub.filterLabel}
            readMore={ui.hub.readMore}
            statusLabels={{ paused: ui.program.paused, limited: ui.program.limited }}
          />
          <div className="compare-band">
            <div>
              <h3>{ui.hub.compareTitle}</h3>
              <p>{ui.hub.compareText}</p>
            </div>
            <a className="btn btn-outline" href={subPathFor(country, lang, "go", ui.compare.slug)}>
              {ui.hub.compareLink}
            </a>
          </div>
        </div>
      </section>
      <GoTest {...ctx} />
      {CITIES.length > 0 && (
        <section className="section" aria-labelledby="villes-titre">
          <div className="container">
            <h2 id="villes-titre" className="section-title">{ui.hub.citiesTitle}</h2>
            <p className="section-lead">{ui.hub.citiesLead}</p>
            <CityLinks {...ctx} />
          </div>
        </section>
      )}
    </>
  );
}

function GoPageHead({ ctx, crumbs, title, intro, children }: { ctx: Ctx; crumbs: { name: string; href: string }[]; title: string; intro: string; children?: ReactNode }) {
  const { country, lang, dict } = ctx;
  const go = dict.products.go;
  return (
    <>
      <Breadcrumbs
        label={dict.common.home}
        items={[{ name: dict.common.home, href: pathFor(country, lang, "home") }, { name: go.navLabel, href: pathFor(country, lang, "go") }, ...crumbs]}
      />
      <section className="container page-head go-head">
        <p className="product-badge">
          <ProductSymbol product="go" size={32} color={PRODUCT_COLORS.go} />
          <span>{go.name}</span>
        </p>
        <h1 className="page-title">{title}</h1>
        <p className="page-lead">{intro}</p>
        {children}
      </section>
    </>
  );
}

function Faq({ title, faq }: { title: string; faq: { q: string; a: string }[] }) {
  if (!faq.length) return null;
  return (
    <section className="section">
      <div className="container narrow">
        <h2 className="section-title">{title}</h2>
        <div className="faq">
          {faq.map((f) => (
            <details key={f.q} className="faq-item">
              <summary>
                <h3>{f.q}</h3>
                <Icon name="plus" />
              </summary>
              <p>{f.a}</p>
            </details>
          ))}
        </div>
      </div>
      <JsonLd data={faqLd(faq)} />
    </section>
  );
}

function accent() {
  return { ["--accent" as string]: PRODUCT_COLORS.go, ["--accent-strong" as string]: PRODUCT_DARK.go };
}

export function ProgramView(ctx: Ctx & { program: Program }) {
  const { country, lang, program: p } = ctx;
  const ui = goUi(lang);
  const t = p[lang];
  const url = absoluteUrl(programHref(country, lang, p));
  return (
    <div style={accent()}>
      <JsonLd data={goServiceLd({ name: t.name, description: t.metaDescription, url, country, lang, cities: CITIES.map((c) => c.name), about: ui.destinations[p.destination] })} />
      <GoPageHead ctx={ctx} crumbs={[{ name: t.name, href: programHref(country, lang, p) }]} title={t.h1} intro={t.intro}>
        <p className="program-tags program-tags-lg">
          <span className="dest-chip">{ui.destinations[p.destination]}</span>
          <span className="goal-chip">{ui.goals[p.goal]}</span>
          <span className="updated-chip">
            {ui.program.updated} {formatDate(p.updated, lang)}
          </span>
        </p>
        {p.status && p.status !== "open" && (
          <p className="notice" role="note">
            <Icon name="info" />
            <span>{p.status === "paused" ? ui.program.paused : ui.program.limited}</span>
          </p>
        )}
        <div className="hero-actions">
          <a className="btn btn-accent" href="#test">{ui.test.title}</a>
          <a className="btn btn-outline" href={pathFor(country, lang, "go") + "#programmes"}>{ui.program.hubLink}</a>
        </div>
      </GoPageHead>

      <section className="section section-tint">
        <div className="container two-cols">
          <div>
            <h2 className="section-title section-title-sm">{ui.program.forWho}</h2>
            <ul className="check-list">
              {t.forWho.map((x) => (
                <li key={x}><span className="feature-check"><Icon name="check" /></span><span>{x}</span></li>
              ))}
            </ul>
          </div>
          <div>
            <h2 className="section-title section-title-sm">{ui.program.conditions}</h2>
            <ul className="dot-list">
              {t.conditions.map((x) => (
                <li key={x}>{x}</li>
              ))}
            </ul>
          </div>
        </div>
      </section>

      <section className="section">
        <div className="container">
          <h2 className="section-title">{ui.program.steps}</h2>
          <ol className="steps">
            {t.steps.map((s, i) => (
              <li key={s.title} className="step">
                <span className="step-num" aria-hidden="true">{i + 1}</span>
                <h3>{s.title}</h3>
                <p>{s.text}</p>
              </li>
            ))}
          </ol>
        </div>
      </section>

      <section className="section section-tint">
        <div className="container two-cols">
          <div>
            <h2 className="section-title section-title-sm">{ui.program.costs}</h2>
            {t.costs.length > 0 && (
              <div className="table-wrap">
                <table className="costs">
                  <thead>
                    <tr><th scope="col">{ui.program.costLabel}</th><th scope="col">{ui.program.costAmount}</th><th scope="col">{ui.program.costFcfa}</th></tr>
                  </thead>
                  <tbody>
                    {t.costs.map((c) => (
                      <tr key={c.label}><th scope="row">{c.label}</th><td>{c.amount}</td><td>{c.fcfa ?? "-"}</td></tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
            <p className="small-note">{ui.program.costsNote}</p>
          </div>
          <div className="stack">
            <div>
              <h2 className="section-title section-title-sm">{ui.program.delays}</h2>
              <p>{t.delays}</p>
            </div>
            <div>
              <h2 className="section-title section-title-sm">{ui.program.biometrics}</h2>
              <p>{t.biometrics}</p>
            </div>
          </div>
        </div>
      </section>

      <Faq title={ui.program.faq} faq={t.faq} />

      <section className="section">
        <div className="container">
          <h2 className="section-title section-title-sm">{ui.program.related}</h2>
          <ul className="mini-grid">
            {relatedPrograms(p).map((o) => (
              <li key={o.key}>
                <a href={programHref(country, lang, o)} className="mini-card">
                  <span className="dest-chip">{ui.destinations[o.destination]}</span>
                  <span><strong>{o[lang].name}</strong><small>{ui.goals[o.goal]}</small></span>
                  <Icon name="arrow" />
                </a>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <GoTest {...ctx} />

      <section className="section">
        <div className="container narrow">
          <h2 className="section-title section-title-sm">{ui.program.sources}</h2>
          <ul className="sources">
            {p.sources.map((s) => (
              <li key={s.url}><a href={s.url} rel="noopener" target="_blank">{s.label}</a></li>
            ))}
          </ul>
          <p className="notice" role="note">
            <Icon name="info" />
            <span>{ui.program.disclaimer}</span>
          </p>
        </div>
      </section>
    </div>
  );
}

export function CityView(ctx: Ctx & { city: City }) {
  const { country, lang, city: c } = ctx;
  const ui = goUi(lang);
  const t = c[lang];
  const url = absoluteUrl(cityHref(country, lang, c));
  const popular = ["ca-study-permit", "de-ausbildung", "ca-express-entry", "fr-study", "de-study", "ca-francophone-mobility"]
    .map((k) => PROGRAMS.find((p) => p.key === k))
    .filter((p): p is Program => !!p && p.status !== "paused")
    .slice(0, 4);
  return (
    <div style={accent()}>
      <JsonLd data={goServiceLd({ name: t.h1, description: t.metaDescription, url, country, lang, cities: [c.name] })} />
      <GoPageHead ctx={ctx} crumbs={[{ name: c.name, href: cityHref(country, lang, c) }]} title={t.h1} intro={t.intro}>
        <div className="hero-actions">
          <a className="btn btn-accent" href="#test">{ui.test.title}</a>
          <a className="btn btn-outline" href={pathFor(country, lang, "go") + "#programmes"}>{ui.program.hubLink}</a>
        </div>
      </GoPageHead>

      <section className="section section-tint">
        <div className="container two-cols">
          <div>
            <h2 className="section-title section-title-sm">{ui.city.travel}</h2>
            <p>{t.travel}</p>
            <dl className="travel">
              {c.toYaounde && (<><dt>{ui.city.toYaounde}</dt><dd>{c.toYaounde.km} km · {c.toYaounde.time[lang]}</dd></>)}
              {c.toDouala && (<><dt>{ui.city.toDouala}</dt><dd>{c.toDouala.km} km · {c.toDouala.time[lang]}</dd></>)}
            </dl>
          </div>
          <div>
            <h2 className="section-title section-title-sm">{ui.city.local}</h2>
            <ul className="check-list">
              {t.local.map((x) => (
                <li key={x}><span className="feature-check"><Icon name="check" /></span><span>{x}</span></li>
              ))}
            </ul>
            {c.universities.length > 0 && (
              <>
                <h3 className="sub-title">{ui.city.universities}</h3>
                <ul className="dot-list">
                  {c.universities.map((u) => <li key={u}>{u}</li>)}
                </ul>
              </>
            )}
          </div>
        </div>
      </section>

      <section className="section">
        <div className="container">
          <h2 className="section-title section-title-sm">{ui.city.programs}</h2>
          <ul className="mini-grid">
            {popular.map((o) => (
              <li key={o.key}>
                <a href={programHref(country, lang, o)} className="mini-card">
                  <span className="dest-chip">{ui.destinations[o.destination]}</span>
                  <span><strong>{o[lang].name}</strong><small>{ui.goals[o.goal]}</small></span>
                  <Icon name="arrow" />
                </a>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <GoTest {...ctx} city={lang === "en" && c.name === "Limbé" ? "Limbe" : c.name} />
      <Faq title={ui.city.faq} faq={t.faq} />

      <section className="section section-tint">
        <div className="container">
          <h2 className="section-title section-title-sm">{ui.city.others}</h2>
          <CityLinks {...ctx} exclude={c.key} />
        </div>
      </section>
    </div>
  );
}

export function CompareView(ctx: Ctx) {
  const { country, lang } = ctx;
  const ui = goUi(lang);
  const c = ui.compare;
  const h = c.headers;
  return (
    <div style={accent()}>
      <GoPageHead ctx={ctx} crumbs={[{ name: c.h1.split(":")[0].trim(), href: subPathFor(country, lang, "go", c.slug) }]} title={c.h1} intro={c.intro}>
        <div className="hero-actions">
          <a className="btn btn-accent" href="#test">{ui.test.title}</a>
        </div>
      </GoPageHead>

      <section className="section section-tint">
        <div className="container">
          <div className="table-wrap">
            <table className="compare">
              <caption>{c.tableCaption}</caption>
              <thead>
                <tr>
                  <th scope="col">{h.destination}</th><th scope="col">{h.language}</th><th scope="col">{h.study}</th>
                  <th scope="col">{h.work}</th><th scope="col">{h.permanent}</th><th scope="col">{h.budget}</th>
                </tr>
              </thead>
              <tbody>
                {DESTINATIONS.map((d) => {
                  const r = c.rows[d];
                  return (
                    <tr key={d}>
                      <th scope="row">{ui.destinations[d]}</th><td>{r.language}</td><td>{r.study}</td><td>{r.work}</td><td>{r.permanent}</td><td>{r.budget}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section className="section">
        <div className="container">
          <h2 className="section-title">{c.adviceTitle}</h2>
          <ul className="features">
            {c.advice.map((a) => (
              <li key={a.title} className="feature">
                <span className="feature-check"><Icon name="check" /></span>
                <div><h3>{a.title}</h3><p>{a.text}</p></div>
              </li>
            ))}
          </ul>
          {DESTINATIONS.map((d) => {
            const list = PROGRAMS.filter((p) => p.destination === d);
            if (!list.length) return null;
            return (
              <div key={d} className="dest-block">
                <h3 className="sub-title">{ui.destinations[d]}</h3>
                <ul className="mini-grid">
                  {list.map((o) => (
                    <li key={o.key}>
                      <a href={programHref(country, lang, o)} className="mini-card">
                        <span className="goal-chip">{ui.goals[o.goal]}</span>
                        <span><strong>{o[lang].name}</strong></span>
                        <Icon name="arrow" />
                      </a>
                    </li>
                  ))}
                </ul>
              </div>
            );
          })}
        </div>
      </section>

      <GoTest {...ctx} />
      <Faq title={ui.program.faq} faq={c.faq} />
    </div>
  );
}
