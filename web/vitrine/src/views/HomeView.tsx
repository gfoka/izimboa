import { COUNTRIES, PRODUCT_COLORS, PRODUCT_DARK, PRODUCT_KEYS, type Lang } from "@/config/site";
import { pathFor } from "@/config/routes";
import type { Dict } from "@/content/types";
import { Photo, PhotoCredit } from "@/components/Photo";
import { ProductSymbol } from "@/components/Logo";
import { Icon } from "@/components/Icon";

export function HomeView({ country, lang, dict }: { country: string; lang: Lang; dict: Dict }) {
  const h = dict.home;
  return (
    <>
      <section className="hero">
        <Photo name="douala" lang={lang} sizes="100vw" priority className="hero-img" />
        <div className="hero-scrim" aria-hidden="true" />
        <div className="container hero-content">
          <p className="eyebrow eyebrow-light">{h.eyebrow}</p>
          <h1 className="hero-title">{h.h1}</h1>
          <p className="hero-lead">{h.lead}</p>
          <div className="hero-actions">
            <a className="btn btn-sun" href="#services">
              {h.primary}
            </a>
            <a className="btn btn-ghost-light" href={pathFor(country, lang, "contact")}>
              {h.secondary}
            </a>
          </div>
        </div>
        <div className="container hero-credit">
          <PhotoCredit name="douala" label={dict.common.photoBy} platform={dict.common.onUnsplash} />
        </div>
      </section>

      <section id="services" className="section">
        <div className="container">
          <div className="section-head">
            <h2 className="section-title">{h.servicesTitle}</h2>
            <p className="section-lead">{h.servicesLead}</p>
          </div>
          <ul className="service-grid">
            {PRODUCT_KEYS.map((k) => {
              const p = dict.products[k];
              return (
                <li key={k} className="service-card" style={{ ["--accent" as string]: PRODUCT_COLORS[k], ["--accent-strong" as string]: PRODUCT_DARK[k] }}>
                  <a href={pathFor(country, lang, k)} className="service-link">
                    <div className="service-media">
                      <Photo name={p.photo} lang={lang} sizes="(min-width: 1100px) 25vw, (min-width: 640px) 50vw, 100vw" />
                    </div>
                    <div className="service-body">
                      <div className="service-name">
                        <ProductSymbol product={k} size={30} color={PRODUCT_COLORS[k]} />
                        <span>{p.name}</span>
                      </div>
                      <h3 className="service-title">{p.navLabel}</h3>
                      <p>{p.cardText}</p>
                      <span className="service-more">
                        {dict.common.discover} <Icon name="arrow" />
                      </span>
                    </div>
                  </a>
                </li>
              );
            })}
          </ul>
        </div>
      </section>

      <section className="section section-tint">
        <div className="container">
          <div className="section-head">
            <h2 className="section-title">{h.pillarsTitle}</h2>
            <p className="section-lead">{h.pillarsLead}</p>
          </div>
          <ul className="pillars">
            {h.pillars.map((p, i) => (
              <li key={p.title} className="pillar">
                <span className="pillar-icon">
                  <Icon name={(["route", "wallet", "chat"] as const)[i]} />
                </span>
                <h3>{p.title}</h3>
                <p>{p.text}</p>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section className="section">
        <div className="container split">
          <div className="split-media">
            <Photo name="street" lang={lang} sizes="(min-width: 900px) 50vw, 100vw" className="rounded-img" />
            <PhotoCredit name="street" label={dict.common.photoBy} platform={dict.common.onUnsplash} />
          </div>
          <div className="split-text">
            <h2 className="section-title">{h.accountTitle}</h2>
            <p className="section-lead">{h.accountText}</p>
            <ul className="checklist">
              {h.accountPoints.map((pt) => (
                <li key={pt}>
                  <Icon name="check" />
                  {pt}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </section>

      <section className="section section-navy">
        <div className="container region">
          <div>
            <h2 className="section-title">{h.regionTitle}</h2>
            <p className="section-lead">{h.regionText}</p>
          </div>
          <ul className="country-chips">
            {COUNTRIES.map((c) => (
              <li key={c.code} className={c.open ? "chip chip-open" : "chip"}>
                <span>{c.name[lang]}</span>
                <small>{c.open ? dict.common.openLabel : dict.common.soonLabel}</small>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section className="section">
        <div className="container cta-band">
          <div>
            <h2 className="section-title">{h.ctaTitle}</h2>
            <p className="section-lead">{h.ctaText}</p>
          </div>
          <a className="btn btn-navy" href={pathFor(country, lang, "contact")}>
            {h.ctaButton}
          </a>
        </div>
      </section>
    </>
  );
}
