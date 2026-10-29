/**
 * Configuration centrale de la vitrine.
 * Ouvrir un pays = l'ajouter dans COUNTRIES avec `open: true` ET fournir ses contenus propres
 * (voir docs/08-referencement.md : jamais de copie d'un autre pays en changeant seulement le nom).
 */

export const SITE_URL = "https://izimboa.com";
export const BRAND = "Izimboa";

/** À confirmer : adresse de contact publique. */
export const CONTACT_EMAIL = "contact@izimboa.com";
/**
 * Google Analytics 4 (identifiant « G-XXXXXXX »). null = aucun suivi. Chargé seulement après accord du visiteur
 * (bandeau de consentement), avec le mode consentement de Google.
 */
export const GA_MEASUREMENT_ID: string | null = "G-PWHLTW9CYG";
/** Jeton de validation Google Search Console (balise meta), si la validation DNS n'est pas utilisée. */
export const GOOGLE_SITE_VERIFICATION: string | null = null;

/** API Izimboa (formulaires du site). */
export const API_URL = "https://api.izimboa.com";

/** Numéro WhatsApp Business au format international (ex. "2376XXXXXXXX"). null = masqué. */
export const WHATSAPP_NUMBER: string | null = null;

export type Lang = "fr" | "en";

export type Country = {
  code: string; // ISO 3166-1 alpha-2, minuscules
  open: boolean;
  langs: Lang[]; // langues publiées pour ce pays (la première est la langue par défaut)
  name: Record<Lang, string>;
};

export const COUNTRIES: Country[] = [
  { code: "cm", open: true, langs: ["fr", "en"], name: { fr: "Cameroun", en: "Cameroon" } },
  { code: "ga", open: false, langs: ["fr"], name: { fr: "Gabon", en: "Gabon" } },
  { code: "cg", open: false, langs: ["fr"], name: { fr: "Congo", en: "Congo" } },
  { code: "td", open: false, langs: ["fr"], name: { fr: "Tchad", en: "Chad" } },
  { code: "cf", open: false, langs: ["fr"], name: { fr: "Centrafrique", en: "Central African Republic" } },
  { code: "gq", open: false, langs: ["fr"], name: { fr: "Guinée équatoriale", en: "Equatorial Guinea" } },
];

export const OPEN_COUNTRIES = COUNTRIES.filter((c) => c.open);

export function getCountry(code: string): Country | undefined {
  return COUNTRIES.find((c) => c.code === code);
}

/** Code hreflang / attribut lang : fr-CM, en-CM… */
export function localeTag(lang: Lang, country: string): string {
  return `${lang}-${country.toUpperCase()}`;
}

/** Applications produits : `live: false` tant qu'elles ne sont pas en ligne (aucun lien sortant cassé). */
export const PRODUCT_APPS = {
  delivery: { url: "https://delivery.izimboa.com", live: true },
  go: { url: "https://go.izimboa.com", live: false },
  fly: { url: "https://fly.izimboa.com", live: false },
  buy: { url: "https://buy.izimboa.com", live: false },
} as const;

export type ProductKey = keyof typeof PRODUCT_APPS;
export const PRODUCT_KEYS: ProductKey[] = ["delivery", "go", "fly", "buy"];

export const PRODUCT_COLORS: Record<ProductKey, string> = {
  delivery: "#E0640E",
  go: "#1E9E6A",
  fly: "#2B8FD6",
  buy: "#7A4CC2",
};

/** Teintes foncées pour le texte blanc (contraste AA ≥ 4,5:1 : boutons, liens). */
export const PRODUCT_DARK: Record<ProductKey, string> = {
  delivery: "#A94A07",
  go: "#147A50",
  fly: "#1D6BA3",
  buy: "#6A3DB0",
};
