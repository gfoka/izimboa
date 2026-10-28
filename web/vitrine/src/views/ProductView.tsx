import type { ReactNode } from "react";
import { CONTACT_EMAIL, PRODUCT_APPS, PRODUCT_COLORS, PRODUCT_DARK, PRODUCT_KEYS, type Lang, type ProductKey } from "@/config/site";
import { pathFor } from "@/config/routes";
import type { Dict } from "@/content/types";
import { Photo, PhotoCredit } from "@/components/Photo";
import { ProductSymbol } from "@/components/Logo";
import { Icon } from "@/components/Icon";
import { Breadcrumbs } from "@/components/Breadcrumbs";
import { JsonLd } from "@/components/JsonLd";
import { serviceLd } from "@/lib/seo";
import { BusinessForm } from "@/components/BusinessForm";

export function ProductView({ country, lang, dict, product, extra }: { country: string; lang: Lang; dict: Dict; product: ProductKey; extra?: ReactNode }) {
  const p = dict.products[product];
  const color = PRODUCT_COLORS[product];
  const mailto = `mailto:${CONTACT_EMAIL}?subject=${encodeURIComponent(p.cta.subject)}`;
  const appUrl = PRODUCT_APPS[product].live && p.app ? PRODUCT_APPS[product].url : null;
  const teamHref = p.business ? "#entreprises" : p.testAnchor ? "#test" : mailto;

  return (
    <div style={{ ["--accent" as string]: color, ["--accent-strong" as string]: PRODUCT_DARK[product] }}>
      <JsonLd data={serviceLd(country, lang, product, p)} />
      <Breadcrumbs
        label={dict.common.home}
        items={[
          { name: dict.common.home, href: pathFor(country, lang, "home") },
          { name: p.navLabel, href: pathFor(country, lang, product) },
        ]}
      />

      <section className="product-hero container">
        <div className="product-hero-text">
          <p className="product-badge">
            <ProductSymbol product={product} size={32} color={color} />
            <span>{p.name}</span>
          </p>
          <h1 className="page-title">{p.h1}</h1>
          <p className="page-lead">{p.intro}</p>
          <p className="status">
            <span className="status-dot" aria-hidden="true" />
            {p.status}
          </p>
          <div className="hero-actions">
            {appUrl && p.app ? (
              <>
                <a className="btn btn-accent" href={appUrl}>
                  {p.app.button}
                </a>
                <a className="btn btn-outline" href={teamHref}>
                  {p.app.team}
                </a>
              </>
            ) : (
              <>
                <a className="btn btn-accent" href={teamHref}>
                  {p.cta.button}
                </a>
                <a className="btn btn-outline" href="#comment">
                  {p.stepsTitle}
                </a>
              </>
            )}
          </div>
        </div>
        <figure className="product-hero-media">
          <Photo name={p.photo} lang={lang} sizes="(min-width: 960px) 50vw, 100vw" priority className="rounded-img" />
          <figcaption>
            <PhotoCredit name={p.photo} label={dict.common.photoBy} platform={dict.common.onUnsplash} />
          </figcaption>
        </figure>
      </section>

      {extra}

      <section id="comment" className="section section-tint">
        <div className="container">
          <h2 className="section-title">{p.stepsTitle}</h2>
          <ol className="steps">
            {p.steps.map((s, i) => (
              <li key={s.title} className="step">
                <span className="step-num" aria-hidden="true">
                  {i + 1}
                </span>
                <h3>{s.title}</h3>
                <p>{s.text}</p>
              </li>
            ))}
          </ol>
        </div>
      </section>

      <section className="section">
        <div className="container">
          <h2 className="section-title">{p.featuresTitle}</h2>
          <ul className="features">
            {p.features.map((f) => (
              <li key={f.title} className="feature">
                <span className="feature-check">
                  <Icon name="check" />
                </span>
                <div>
                  <h3>{f.title}</h3>
                  <p>{f.text}</p>
                </div>
              </li>
            ))}
          </ul>
          {p.notice && (
            <p className="notice" role="note">
              <Icon name="info" />
              <span>{p.notice}</span>
            </p>
          )}
        </div>
      </section>

      {p.business && (
        <section id="entreprises" className="section section-tint biz-section" aria-labelledby="entreprises-titre">
          <div className="container biz-grid">
            <div>
              <h2 id="entreprises-titre" className="section-title">{p.business.title}</h2>
              <p className="section-lead">{p.business.lead}</p>
              <ul className="biz-points">
                {p.business.points.map((pt) => (
                  <li key={pt}>
                    <span className="feature-check">
                      <Icon name="check" />
                    </span>
                    <span>{pt}</span>
                  </li>
                ))}
              </ul>
            </div>
            <div className="biz-card">
              <BusinessForm t={p.business} lang={lang} product={product} privacyHref={pathFor(country, lang, "privacy")} />
            </div>
          </div>
        </section>
      )}

      <section className={p.business ? "section" : "section section-tint"}>
        <div className="container narrow">
          <h2 className="section-title">{p.faqTitle}</h2>
          <div className="faq">
            {p.faq.map((f) => (
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
      </section>

      <section className="section">
        <div className="container cta-band cta-accent">
          <div>
            <h2 className="section-title">{p.cta.title}</h2>
            <p className="section-lead">{p.cta.text}</p>
          </div>
          <a className="btn btn-light" href={appUrl ?? (p.testAnchor ? "#test" : mailto)}>
            {appUrl || p.testAnchor ? <Icon name="arrow" /> : <Icon name="mail" />} {p.cta.button}
          </a>
        </div>
      </section>

      <section className="section section-tint" aria-labelledby="autres">
        <div className="container">
          <h2 id="autres" className="section-title section-title-sm">
            {dict.common.otherServices}
          </h2>
          <ul className="mini-grid">
            {PRODUCT_KEYS.filter((k) => k !== product).map((k) => (
              <li key={k}>
                <a href={pathFor(country, lang, k)} className="mini-card">
                  <ProductSymbol product={k} size={36} color={PRODUCT_COLORS[k]} />
                  <span>
                    <strong>{dict.products[k].navLabel}</strong>
                    <small>{dict.products[k].name}</small>
                  </span>
                  <Icon name="arrow" />
                </a>
              </li>
            ))}
          </ul>
        </div>
      </section>
    </div>
  );
}
