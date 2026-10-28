import { CONTACT_EMAIL, WHATSAPP_NUMBER, type Lang } from "@/config/site";
import { pathFor } from "@/config/routes";
import type { Dict } from "@/content/types";
import { Photo, PhotoCredit } from "@/components/Photo";
import { Icon } from "@/components/Icon";
import { Breadcrumbs } from "@/components/Breadcrumbs";

type P = { country: string; lang: Lang; dict: Dict };

export function AboutView({ country, lang, dict }: P) {
  const a = dict.about;
  return (
    <>
      <Breadcrumbs
        label={dict.common.home}
        items={[
          { name: dict.common.home, href: pathFor(country, lang, "home") },
          { name: dict.common.about, href: pathFor(country, lang, "about") },
        ]}
      />
      <section className="container page-head">
        <h1 className="page-title">{a.h1}</h1>
        <p className="page-lead">{a.lead}</p>
      </section>
      <section className="container about-media">
        <Photo name="douala" lang={lang} sizes="100vw" priority className="rounded-img wide-img" />
        <PhotoCredit name="douala" label={dict.common.photoBy} platform={dict.common.onUnsplash} />
      </section>
      <section className="section">
        <div className="container about-grid">
          {a.sections.map((s) => (
            <article key={s.title} className="about-block">
              <h2>{s.title}</h2>
              <p>{s.text}</p>
            </article>
          ))}
        </div>
      </section>
      <section className="section section-navy">
        <div className="container">
          <h2 className="section-title">{a.valuesTitle}</h2>
          <ul className="pillars pillars-dark">
            {a.values.map((v) => (
              <li key={v.title} className="pillar">
                <h3>{v.title}</h3>
                <p>{v.text}</p>
              </li>
            ))}
          </ul>
        </div>
      </section>
    </>
  );
}

export function ContactView({ country, lang, dict }: P) {
  const c = dict.contact;
  return (
    <>
      <Breadcrumbs
        label={dict.common.home}
        items={[
          { name: dict.common.home, href: pathFor(country, lang, "home") },
          { name: dict.common.contact, href: pathFor(country, lang, "contact") },
        ]}
      />
      <section className="container page-head">
        <h1 className="page-title">{c.h1}</h1>
        <p className="page-lead">{c.lead}</p>
      </section>
      <section className="section section-first">
        <div className="container contact-grid">
          {c.channels.map((ch) => (
            <article key={ch.title} className="contact-card">
              <h2>{ch.title}</h2>
              <p>{ch.text}</p>
              <a className="btn btn-navy" href={`mailto:${CONTACT_EMAIL}?subject=${encodeURIComponent(ch.subject)}`}>
                <Icon name="mail" /> {ch.button}
              </a>
            </article>
          ))}
        </div>
      </section>
      <section className="section section-tint">
        <div className="container direct">
          <h2 className="section-title section-title-sm">{c.directTitle}</h2>
          <dl>
            <div>
              <dt>{c.emailLabel}</dt>
              <dd>
                <a href={`mailto:${CONTACT_EMAIL}`}>{CONTACT_EMAIL}</a>
              </dd>
            </div>
            {WHATSAPP_NUMBER && (
              <div>
                <dt>{c.whatsappLabel}</dt>
                <dd>
                  <a href={`https://wa.me/${WHATSAPP_NUMBER}`} rel="noopener">
                    +{WHATSAPP_NUMBER}
                  </a>
                </dd>
              </div>
            )}
          </dl>
          <p className="muted">{c.responseNote}</p>
        </div>
      </section>
    </>
  );
}

export function LegalView({ country, lang, dict, page = "legal" }: P & { page?: "legal" | "privacy" }) {
  const l = dict[page];
  return (
    <>
      <Breadcrumbs
        label={dict.common.home}
        items={[
          { name: dict.common.home, href: pathFor(country, lang, "home") },
          { name: l.h1, href: pathFor(country, lang, page) },
        ]}
      />
      <section className="container page-head narrow">
        <h1 className="page-title">{l.h1}</h1>
      </section>
      <section className="section section-first">
        <div className="container narrow legal">
          {l.sections.map((s) => (
            <section key={s.title} id={s.id}>
              <h2>{s.title}</h2>
              {s.text.split("\n\n").map((p) => <p key={p}>{p}</p>)}
            </section>
          ))}
        </div>
      </section>
    </>
  );
}
