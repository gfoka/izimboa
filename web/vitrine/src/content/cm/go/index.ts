import type { Lang } from "@/config/site";
import type { City, Destination, Goal, Program } from "@/content/go-types";
import canada from "./data/canada.json";
import germany from "./data/germany.json";
import franceBelgium from "./data/france-belgium.json";
import cities from "./data/cities.json";
import { GO_UI } from "./ui";

/**
 * Izimboa Go au Cameroun : programmes vérifiés sur les sources officielles (data/*.json, date « updated »)
 * et pages villes au contenu local. Mettre à jour un programme = modifier son JSON et sa date.
 */
export const PROGRAMS: Program[] = [...(canada as Program[]), ...(germany as Program[]), ...(franceBelgium as Program[])];
export const CITIES: City[] = cities as City[];
export const DESTINATIONS: Destination[] = ["ca", "de", "fr", "be"];
export const GOALS: Goal[] = ["study", "work", "training", "permanent"];

export function goUi(lang: Lang) {
  return GO_UI[lang];
}

export type GoSub =
  | { kind: "program"; program: Program }
  | { kind: "city"; city: City }
  | { kind: "compare" };

/** Toutes les sous-pages d'Izimboa Go, avec leur adresse dans chaque langue. */
export function goSubPages(): { sub: GoSub; slug: Record<Lang, string> }[] {
  return [
    { sub: { kind: "compare" }, slug: { fr: GO_UI.fr.compare.slug, en: GO_UI.en.compare.slug } },
    ...PROGRAMS.map((p) => ({ sub: { kind: "program" as const, program: p }, slug: p.slug })),
    ...CITIES.map((c) => ({ sub: { kind: "city" as const, city: c }, slug: c.slug })),
  ];
}

export function findGoSub(lang: Lang, slug: string) {
  return goSubPages().find((p) => p.slug[lang] === slug);
}

/** Programmes du même objectif dans les autres pays : l'esprit « meilleure voie, pas une seule destination ». */
export function relatedPrograms(p: Program): Program[] {
  const sameGoal = PROGRAMS.filter((o) => o.key !== p.key && o.goal === p.goal);
  const others = PROGRAMS.filter((o) => o.key !== p.key && o.goal !== p.goal && o.destination !== p.destination);
  return [...sameGoal, ...others].slice(0, 3);
}
