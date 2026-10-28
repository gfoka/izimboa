import type { ReactNode } from "react";
import { COUNTRIES, CONTACT_EMAIL, PRODUCT_APPS, PRODUCT_COLORS, PRODUCT_KEYS, getCountry, localeTag, type Lang } from "@/config/site";
import { pathFor, type PageKey } from "@/config/routes";
import type { Dict } from "@/content/types";
import { Logo, ProductSymbol } from "./Logo";

type Ctx = { country: string; lang: Lang; dict: Dict; pageKey: PageKey };

function LangSwitch({ country, lang, dict, pageKey }: Ctx) {
  const c = getCountry(country)!;
  const others = c.langs.filter((l) => l !== lang);
  return (
    <>
      {others.map((l) => (
        <a key={l} className="lang-switch" href={pathFor(country, l, pageKey)} hrefLang={localeTag(l, country)} lang={l}>
          {dict.common.otherLanguage}
        </a>
      ))}
    </>
  );
}

export function Header(ctx: Ctx) {
  const { country, lang, dict, pageKey } = ctx;
  const navLinks = (
    <ul className="nav-list">
      {PRODUCT_KEYS.map((k) => (
        <li key={k}>
          <a href={pathFor(country, lang, k)} aria-current={pageKey === k ? "page" : undefined}>
            {dict.products[k].navLabel}
          </a>
        </li>
      ))}
      <li>
        <a href={pathFor(country, lang, "about")} aria-current={pageKey === "about" ? "page" : undefined}>
          {dict.common.about}
        </a>
      </li>
      <li>
        <a href={pathFor(country, lang, "contact")} aria-current={pageKey === "contact" ? "page" : undefined}>
          {dict.common.contact}
        </a>
      </li>
      {PRODUCT_APPS.delivery.live && (
        <li className="nav-mobile-only">
          <a href={PRODUCT_APPS.delivery.url}>{dict.common.sellerSpace}</a>
        </li>
      )}
    </ul>
  );

  return (
    <header className="site-header">
      <div className="container header-inner">
        <a href={pathFor(country, lang, "home")} className="brand" aria-label={`Izimboa, ${dict.common.home}`}>
          <Logo />
        </a>
        <nav className="nav-desktop" aria-label={dict.common.menu}>
          {navLinks}
        </nav>
        <div className="header-actions">
          {PRODUCT_APPS.delivery.live && (
            <a className="btn btn-sm btn-navy header-cta" href={PRODUCT_APPS.delivery.url}>
              {dict.common.sellerSpace}
            </a>
          )}
          <LangSwitch {...ctx} />
          <details className="nav-mobile">
            <summary aria-label={dict.common.menu}>
              <span className="burger" aria-hidden="true" />
              <span className="sr-only">{dict.common.menu}</span>
            </summary>
            <nav aria-label={dict.common.menu}>{navLinks}</nav>
          </details>
        </div>
      </div>
    </header>
  );
}

export function Footer({ country, lang, dict, pageKey }: Ctx) {
  const year = new Date().getFullYear();
  return (
    <footer className="site-footer">
      <div className="container footer-grid">
        <div className="footer-brand">
          <Logo inverted />
          <p>{dict.common.footerTagline}</p>
        </div>
        <div>
          <h2 className="footer-title">{dict.common.services}</h2>
          <ul>
            {PRODUCT_KEYS.map((k) => (
              <li key={k}>
                <a href={pathFor(country, lang, k)} className="footer-product">
                  <ProductSymbol product={k} size={20} stem="#fff" color={PRODUCT_COLORS[k]} />
                  {dict.products[k].name}
                </a>
              </li>
            ))}
          </ul>
        </div>
        <div>
          <h2 className="footer-title">{dict.common.footerCompany}</h2>
          <ul>
            <li><a href={pathFor(country, lang, "about")}>{dict.common.about}</a></li>
            <li><a href={pathFor(country, lang, "contact")}>{dict.common.contact}</a></li>
            <li><a href={`mailto:${CONTACT_EMAIL}`}>{CONTACT_EMAIL}</a></li>
            <li><a href={pathFor(country, lang, "legal")}>{dict.common.legal}</a></li>
            <li><a href={pathFor(country, lang, "privacy")}>{dict.common.privacy}</a></li>
            {PRODUCT_APPS.delivery.live && <li><a href={PRODUCT_APPS.delivery.url}>{dict.common.sellerSpace}</a></li>}
          </ul>
        </div>
        <div>
          <h2 className="footer-title">{dict.common.countriesTitle}</h2>
          <ul className="footer-countries">
            {COUNTRIES.map((c) => (
              <li key={c.code}>
                {c.open ? (
                  <a href={pathFor(c.code, c.langs.includes(lang) ? lang : c.langs[0], "home")}>{c.name[lang]}</a>
                ) : (
                  <span className="muted">{c.name[lang]}</span>
                )}
                <span className={c.open ? "tag tag-open" : "tag"}>{c.open ? dict.common.openLabel : dict.common.soonLabel}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>
      <div className="container footer-bottom">
        <p>© {year} Izimboa. {dict.common.rights}</p>
        <LangSwitch country={country} lang={lang} dict={dict} pageKey={pageKey} />
      </div>
    </footer>
  );
}

export function Shell({ children, ...ctx }: Ctx & { children: ReactNode }) {
  return (
    <>
      <a href="#contenu" className="skip-link">
        {ctx.dict.common.skipToContent}
      </a>
      <Header {...ctx} />
      <main id="contenu">{children}</main>
      <Footer {...ctx} />
    </>
  );
}
