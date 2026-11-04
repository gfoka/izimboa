import { COURIER_APP_URL, PRODUCT_APPS, PRODUCT_COLORS, PRODUCT_DARK, type Lang } from "@/config/site";
import { pathFor, subPathFor } from "@/config/routes";
import type { CourierPage, Dict } from "@/content/types";
import { Photo, PhotoCredit } from "@/components/Photo";
import { Icon } from "@/components/Icon";
import { Breadcrumbs } from "@/components/Breadcrumbs";
import { JsonLd } from "@/components/JsonLd";
import { TrackedLink } from "@/components/TrackedLink";
import { faqLd } from "@/lib/seo";

/** Page « Devenir livreur » : recrutement des livreurs, installation de l'application Android. */
export function CourierView({ country, lang, dict, page }: { country: string; lang: Lang; dict: Dict; page: CourierPage }) {
  const delivery = dict.products.delivery;
  const crumbs = [
    { name: dict.common.home, href: pathFor(country, lang, "home") },
    { name: delivery.navLabel, href: pathFor(country, lang, "delivery") },
    { name: page.navLabel, href: subPathFor(country, lang, "delivery", page.slug) },
  ];
  const playButton = (cta: string, light = false) => (
    <TrackedLink className={light ? "btn btn-light" : "btn btn-accent"} href={COURIER_APP_URL} cta={cta}>
      {page.playButton} <Icon name="arrow" />
    </TrackedLink>
  );

  return (
    <div style={{ ["--accent" as string]: PRODUCT_COLORS.delivery, ["--accent-strong" as string]: PRODUCT_DARK.delivery }}>
      <JsonLd
        data={[
          faqLd(page.faq),
          {
            "@context": "https://schema.org",
            "@type": "MobileApplication",
            name: "Izimboa Livreur",
            operatingSystem: "Android",
            applicationCategory: "BusinessApplication",
            downloadUrl: COURIER_APP_URL,
            offers: { "@type": "Offer", price: "0", priceCurrency: "XAF" },
            publisher: { "@id": "https://izimboa.com/#organization" },
          },
        ]}
      />
      <Breadcrumbs label={dict.common.home} items={crumbs} />

      <section className="product-hero container">
        <div className="product-hero-text">
          <h1 className="page-title">{page.h1}</h1>
          <p className="page-lead">{page.lead}</p>
          <div className="hero-actions">
            {playButton("courier_hero_play")}
            <a className="btn btn-outline" href="#inscription">
              {page.stepsTitle}
            </a>
          </div>
          <p className="play-note">{page.playNote}</p>
          <ul className="hero-points">
            {page.heroPoints.map((x) => (
              <li key={x}>
                <Icon name="check" /> {x}
              </li>
            ))}
          </ul>
        </div>
        <figure className="product-hero-media">
          <Photo name="moto" lang={lang} sizes="(min-width: 960px) 50vw, 100vw" priority className="rounded-img" />
          <figcaption>
            <PhotoCredit name="moto" label={dict.common.photoBy} platform={dict.common.onUnsplash} />
          </figcaption>
        </figure>
      </section>

      <section className="section section-tint">
        <div className="container">
          <h2 className="section-title">{page.whyTitle}</h2>
          <ul className="features">
            {page.why.map((f) => (
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
        </div>
      </section>

      <section className="section">
        <div className="container">
          <h2 className="section-title">{page.needsTitle}</h2>
          <ul className="needs">
            {page.needs.map((n) => (
              <li key={n}>
                <span className="feature-check">
                  <Icon name="check" />
                </span>
                {n}
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section id="inscription" className="section section-tint">
        <div className="container">
          <h2 className="section-title">{page.stepsTitle}</h2>
          <ol className="steps">
            {page.steps.map((s, i) => (
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
        <div className="container narrow">
          <h2 className="section-title">{page.faqTitle}</h2>
          <div className="faq">
            {page.faq.map((f) => (
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
            <h2 className="section-title">{page.ctaTitle}</h2>
            <p className="section-lead">{page.ctaText}</p>
          </div>
          {playButton("courier_cta_play", true)}
        </div>
      </section>

      {PRODUCT_APPS.delivery.live && (
        <section className="section section-tint">
          <div className="container cta-band">
            <div>
              <h2 className="section-title section-title-sm">{page.sellerTitle}</h2>
              <p className="section-lead">{page.sellerText}</p>
            </div>
            <TrackedLink className="btn btn-navy" href={PRODUCT_APPS.delivery.url} cta="courier_page_seller">
              {page.sellerButton}
            </TrackedLink>
          </div>
        </section>
      )}
    </div>
  );
}
