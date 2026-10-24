import type { Status } from "./api";

const money = new Intl.NumberFormat("fr-FR", { maximumFractionDigits: 0 });
export const fcfa = (n: number) => `${money.format(n)} FCFA`;

const dateTime = new Intl.DateTimeFormat("fr-FR", { day: "numeric", month: "short", hour: "2-digit", minute: "2-digit", timeZone: "Africa/Douala" });
const time = new Intl.DateTimeFormat("fr-FR", { hour: "2-digit", minute: "2-digit", timeZone: "Africa/Douala" });
const day = new Intl.DateTimeFormat("fr-FR", { year: "numeric", month: "2-digit", day: "2-digit", timeZone: "Africa/Douala" });

export const formatDateTime = (iso: string) => dateTime.format(new Date(iso));
export const formatTime = (iso: string) => time.format(new Date(iso));
export const isToday = (iso: string) => day.format(new Date(iso)) === day.format(new Date());

export const km = (n: number) => `${n.toLocaleString("fr-FR", { maximumFractionDigits: 1 })} km`;

export const STATUS: Record<Status, { label: string; tone: "wait" | "move" | "ok" | "bad" | "off" }> = {
  CREATED: { label: "En attente d'un livreur", tone: "wait" },
  ASSIGNED: { label: "Livreur attribué", tone: "move" },
  PICKED_UP: { label: "Colis ramassé", tone: "move" },
  IN_TRANSIT: { label: "En route", tone: "move" },
  DELIVERED: { label: "Livré", tone: "ok" },
  FAILED: { label: "Échec de livraison", tone: "bad" },
  RETURNED: { label: "Retourné au vendeur", tone: "off" },
  CANCELLED: { label: "Annulé", tone: "off" },
};

export const FINAL: Status[] = ["DELIVERED", "RETURNED", "CANCELLED"];

/** Étapes du parcours normal, pour la barre de progression. */
export const PROGRESS: Status[] = ["CREATED", "ASSIGNED", "PICKED_UP", "IN_TRANSIT", "DELIVERED"];

/** Lien WhatsApp « cliquer pour discuter » vers un numéro E.164. */
export const whatsappLink = (phone: string, text: string) =>
  `https://wa.me/${phone.replace(/\D/g, "")}?text=${encodeURIComponent(text)}`;

/** « +237699123456 » → « +237 6 99 12 34 56 » (lisible au téléphone). */
export const formatPhone = (e164: string) => {
  const m = /^\+237(\d)(\d{2})(\d{2})(\d{2})(\d{2})$/.exec(e164);
  return m ? `+237 ${m[1]} ${m[2]} ${m[3]} ${m[4]} ${m[5]}` : e164;
};
