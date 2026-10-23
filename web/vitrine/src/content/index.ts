import type { Lang } from "@/config/site";
import type { Dict } from "./types";
import cmFr from "./cm/fr";
import cmEn from "./cm/en";

/**
 * Contenus par pays ET par langue : chaque pays a ses propres textes
 * (FAQ locales, villes, moyens de paiement), jamais une copie d'un autre pays.
 * Plus tard : ces contenus viendront d'un CMS, toujours rendus en HTML statique au build.
 */
const CONTENT: Record<string, Partial<Record<Lang, Dict>>> = {
  cm: { fr: cmFr, en: cmEn },
};

export function getDict(country: string, lang: Lang): Dict {
  const d = CONTENT[country]?.[lang];
  if (!d) throw new Error(`Aucun contenu pour ${country}/${lang}`);
  return d;
}

export type { Dict } from "./types";
