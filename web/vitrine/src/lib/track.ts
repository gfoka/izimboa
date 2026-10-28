/**
 * Événements de mesure d'audience (Google Analytics 4), envoyés uniquement si le visiteur a accepté
 * (gtag n'est chargé qu'après accord). Jamais de donnée personnelle : pas de nom, téléphone, e-mail ni ville.
 * Liste des événements : docs/11-mesure-audience.md.
 */
export type TrackEvent =
  | "cta_click"
  | "eligibility_start"
  | "eligibility_step"
  | "eligibility_complete"
  | "eligibility_result_click"
  | "lead_submit"
  | "lead_error";

export function track(event: TrackEvent, params: Record<string, string | number | boolean> = {}) {
  try {
    const w = window as unknown as { gtag?: (...args: unknown[]) => void };
    w.gtag?.("event", event, params);
  } catch {
    /* la mesure ne doit jamais casser la page */
  }
}
