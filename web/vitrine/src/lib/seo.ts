import type { Metadata } from "next";
import { BRAND, CONTACT_EMAIL, SITE_URL, getCountry, localeTag, type Lang } from "@/config/site";
import { NOINDEX_PAGES, absoluteUrl, pathFor, type PageKey } from "@/config/routes";
import { ogImage, PHOTOS, type PhotoKey } from "@/config/photos";
import type { Meta, ProductContent } from "@/content/types";

/**
 * Métadonnées d'une page : canonical absolu (forme unique, barre finale),
 * hreflang vers chaque langue publiée du pays + x-default vers la racine,
 * Open Graph pour les partages (WhatsApp, Facebook, LinkedIn).
 */
export function pageMetadata(opts: {
  country: string;
  lang: Lang;
  key: PageKey;
  meta: Meta;
  photo: PhotoKey;
}): Metadata {
  const { country, lang, key, meta, photo } = opts;
  const c = getCountry(country)!;
  const paths = Object.fromEntries(c.langs.map((l) => [l, pathFor(country, l, key)])) as Record<Lang, string>;
  return pathMetadata({ country, lang, paths, meta, photo, noindex: NOINDEX_PAGES.includes(key), absoluteTitle: key === "home" });
}

/** Métadonnées à partir des chemins de la page dans chaque langue (pages et sous-pages). */
export function pathMetadata(opts: {
  country: string;
  lang: Lang;
  paths: Record<Lang, string>;
  meta: Meta;
  photo: PhotoKey;
  noindex?: boolean;
  absoluteTitle?: boolean;
}): Metadata {
  const { country, lang, paths, meta, photo, noindex = false, absoluteTitle = false } = opts;
  const c = getCountry(country)!;
  const url = absoluteUrl(paths[lang]);

  const languages: Record<string, string> = {};
  for (const l of c.langs) languages[localeTag(l, country)] = absoluteUrl(paths[l]);
  languages["x-default"] = `${SITE_URL}/`;

  const p = PHOTOS[photo];

  return {
    metadataBase: new URL(SITE_URL),
    title: absoluteTitle ? { absolute: meta.title } : meta.title,
    description: meta.description,
    alternates: { canonical: url, languages },
    robots: noindex ? { index: false, follow: true } : { index: true, follow: true },
    openGraph: {
      type: "website",
      url,
      siteName: BRAND,
      title: meta.title,
      description: meta.description,
      locale: localeTag(lang, country).replace("-", "_"),
      alternateLocale: c.langs.filter((l) => l !== lang).map((l) => localeTag(l, country).replace("-", "_")),
      images: [{ url: ogImage(p), width: 1200, height: 630, alt: p.alt[lang] }],
    },
    twitter: {
      card: "summary_large_image",
      title: meta.title,
      description: meta.description,
      images: [ogImage(p)],
    },
  };
}

/* ---------- Données structurées (schema.org, JSON-LD) ---------- */

const ORG_ID = `${SITE_URL}/#organization`;

export function organizationLd(lang: Lang) {
  return {
    "@context": "https://schema.org",
    "@type": "Organization",
    "@id": ORG_ID,
    name: BRAND,
    url: `${SITE_URL}/`,
    logo: `${SITE_URL}/logo-izimboa.svg`,
    email: CONTACT_EMAIL,
    areaServed: { "@type": "Country", name: lang === "fr" ? "Cameroun" : "Cameroon" },
    contactPoint: [
      {
        "@type": "ContactPoint",
        contactType: "customer service",
        email: CONTACT_EMAIL,
        areaServed: "CM",
        availableLanguage: ["French", "English"],
      },
    ],
  };
}

export function websiteLd(country: string, lang: Lang) {
  return {
    "@context": "https://schema.org",
    "@type": "WebSite",
    "@id": `${SITE_URL}/#website`,
    name: BRAND,
    url: absoluteUrl(pathFor(country, lang, "home")),
    inLanguage: localeTag(lang, country),
    publisher: { "@id": ORG_ID },
  };
}

export function serviceLd(country: string, lang: Lang, key: PageKey, p: ProductContent) {
  const c = getCountry(country)!;
  return {
    "@context": "https://schema.org",
    "@type": "Service",
    name: p.name,
    serviceType: p.navLabel,
    description: p.meta.description,
    url: absoluteUrl(pathFor(country, lang, key)),
    provider: { "@id": ORG_ID },
    areaServed: { "@type": "Country", name: c.name[lang] },
    availableLanguage: c.langs.map((l) => (l === "fr" ? "French" : "English")),
  };
}

export function breadcrumbLd(items: { name: string; url: string }[]) {
  return {
    "@context": "https://schema.org",
    "@type": "BreadcrumbList",
    itemListElement: items.map((it, i) => ({ "@type": "ListItem", position: i + 1, name: it.name, item: it.url })),
  };
}

export function faqLd(faq: { q: string; a: string }[]) {
  return {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    mainEntity: faq.map((f) => ({ "@type": "Question", name: f.q, acceptedAnswer: { "@type": "Answer", text: f.a } })),
  };
}

/** Service d'accompagnement rendu dans tout le pays, avec la liste des villes desservies. */
export function goServiceLd(opts: { name: string; description: string; url: string; country: string; lang: Lang; cities: string[]; about?: string }) {
  const c = getCountry(opts.country)!;
  return {
    "@context": "https://schema.org",
    "@type": "Service",
    name: opts.name,
    serviceType: opts.lang === "fr" ? "Accompagnement immigration" : "Immigration support",
    description: opts.description,
    url: opts.url,
    provider: { "@id": ORG_ID },
    areaServed: [{ "@type": "Country", name: c.name[opts.lang] }, ...opts.cities.map((n) => ({ "@type": "City", name: n }))],
    ...(opts.about ? { about: { "@type": "Thing", name: opts.about } } : {}),
  };
}
