import type { Lang } from "@/config/site";

/** Destinations proposées par Izimboa Go (codes ISO). */
export type Destination = "ca" | "de" | "fr" | "be";
export type Goal = "study" | "work" | "permanent" | "training";

export type ProgramText = {
  name: string;
  metaTitle: string;
  metaDescription: string;
  h1: string;
  intro: string;
  forWho: string[];
  conditions: string[];
  steps: { title: string; text: string }[];
  costs: { label: string; amount: string; fcfa?: string }[];
  delays: string;
  biometrics: string;
  faq: { q: string; a: string }[];
};

/** Un programme d'immigration, vérifié sur les sources officielles (date `updated`). */
export type Program = {
  key: string;
  destination: Destination;
  goal: Goal;
  slug: Record<Lang, string>;
  updated: string;
  sources: { label: string; url: string }[];
  /** Statut affiché si le programme est suspendu ou limité. */
  status?: "open" | "paused" | "limited";
  fr: ProgramText;
  en: ProgramText;
};

export type CityText = {
  metaTitle: string;
  metaDescription: string;
  h1: string;
  intro: string;
  travel: string;
  local: string[];
  faq: { q: string; a: string }[];
};

/** Page ville : vrai contenu local (trajet vers les centres de visa, universités…), jamais un texte copié. */
export type City = {
  key: string;
  name: string;
  region: Record<Lang, string>;
  slug: Record<Lang, string>;
  toYaounde?: { km: number; time: Record<Lang, string> };
  toDouala?: { km: number; time: Record<Lang, string> };
  universities: string[];
  sources: { label: string; url: string }[];
  fr: CityText;
  en: CityText;
};
