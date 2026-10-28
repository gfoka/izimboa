import { SITE_URL, OPEN_COUNTRIES, type Lang, type ProductKey } from "./site";

/**
 * Table des pages et de leurs adresses traduites.
 * Une page = une clé ; chaque langue a son propre slug (meilleur pour les recherches dans chaque langue).
 * Modifier un slug publié = prévoir une redirection 301 (voir docs/08-referencement.md).
 */
export type PageKey = "home" | ProductKey | "about" | "contact" | "legal" | "privacy";

export const SLUGS: Record<Exclude<PageKey, "home">, Record<Lang, string>> = {
  delivery: { fr: "livraison-colis", en: "parcel-delivery" },
  go: { fr: "accompagnement-immigration", en: "immigration-support" },
  fly: { fr: "billets-avion", en: "flight-tickets" },
  buy: { fr: "achat-international", en: "international-shopping" },
  about: { fr: "a-propos", en: "about" },
  contact: { fr: "contact", en: "contact" },
  legal: { fr: "mentions-legales", en: "legal-notice" },
  privacy: { fr: "confidentialite", en: "privacy-policy" },
};

/** Pages exclues de l'index Google (contenu juridique, sans valeur de recherche). */
export const NOINDEX_PAGES: PageKey[] = ["legal"];

export function pageKeyFromSlug(lang: Lang, slug: string): Exclude<PageKey, "home"> | undefined {
  return (Object.keys(SLUGS) as Exclude<PageKey, "home">[]).find((k) => SLUGS[k][lang] === slug);
}

/** Chemin relatif, toujours avec la barre finale (forme canonique unique). */
export function pathFor(country: string, lang: Lang, key: PageKey): string {
  if (key === "home") return `/${country}/${lang}/`;
  return `/${country}/${lang}/${SLUGS[key][lang]}/`;
}

export function absoluteUrl(path: string): string {
  return `${SITE_URL}${path}`;
}

/** Toutes les pages publiées : la source unique pour le build, le sitemap et les hreflang. */
export function allPublishedPages(): { country: string; lang: Lang; key: PageKey }[] {
  const keys: PageKey[] = ["home", ...(Object.keys(SLUGS) as Exclude<PageKey, "home">[])];
  return OPEN_COUNTRIES.flatMap((c) => c.langs.flatMap((lang) => keys.map((key) => ({ country: c.code, lang, key }))));
}
