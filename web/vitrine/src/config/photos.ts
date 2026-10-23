import type { Lang } from "./site";

/**
 * Photos Unsplash (licence Unsplash : usage commercial libre, crédit apprécié).
 * Servies depuis le CDN Unsplash (redimensionnement à la volée). À terme : copies auto-hébergées
 * derrière Cloudflare pour maîtriser le cache.
 */
export type Photo = {
  id: string; // identifiant d'image Unsplash (photo-…)
  width: number;
  height: number;
  alt: Record<Lang, string>;
  credit: { name: string; profile: string; page: string };
  /** Point focal pour object-position, ex. "50% 40%" */
  focus?: string;
};

export const PHOTOS = {
  douala: {
    id: "photo-1594386479412-fa62932f4cdc",
    width: 5628,
    height: 4000,
    alt: { fr: "Scène de vie dans une rue animée de Douala", en: "Everyday life on a busy street in Douala" },
    credit: { name: "Edouard Tamba", profile: "https://unsplash.com/@tamba", page: "https://unsplash.com/photos/bZnciM7XIYw" },
    focus: "50% 45%",
  },
  moto: {
    id: "photo-1601979107535-46367552bc25",
    width: 6000,
    height: 4000,
    alt: { fr: "Moto-taxi en circulation à Douala", en: "Motorbike taxi riding through Douala" },
    credit: { name: "Edouard Tamba", profile: "https://unsplash.com/@tamba", page: "https://unsplash.com/photos/g-I1MUt16dc" },
    focus: "50% 50%",
  },
  passport: {
    id: "photo-1655722725332-9925c96dd627",
    width: 6720,
    height: 4480,
    alt: { fr: "Passeport ouvert couvert de tampons de voyage", en: "Open passport covered in travel stamps" },
    credit: { name: "Global Residence Index", profile: "https://unsplash.com/@globalresidenceindex", page: "https://unsplash.com/photos/LPdaW746WAw" },
    focus: "50% 50%",
  },
  wing: {
    id: "photo-1530469641172-8ac15d0a7d6a",
    width: 5550,
    height: 4000,
    alt: { fr: "Aile d'avion au-dessus des nuages", en: "Aircraft wing above the clouds" },
    credit: { name: "Blake Guidry", profile: "https://unsplash.com/@blakeguidry", page: "https://unsplash.com/photos/p9vr45T2scg" },
    focus: "50% 50%",
  },
  parcel: {
    id: "photo-1740842028123-56fd319de33a",
    width: 4032,
    height: 3024,
    alt: { fr: "Colis en carton prêt à être livré", en: "Cardboard parcel ready for delivery" },
    credit: { name: "Anastasiya Doicheva", profile: "https://unsplash.com/@an_doich", page: "https://unsplash.com/photos/2w-EQD1SkuY" },
    focus: "50% 50%",
  },
  street: {
    id: "photo-1601979107569-731e84cee3e8",
    width: 5388,
    height: 4000,
    alt: { fr: "Rue de Douala en mouvement", en: "A Douala street on the move" },
    credit: { name: "Edouard Tamba", profile: "https://unsplash.com/@tamba", page: "https://unsplash.com/photos/Nh8OWMPhHJ4" },
    focus: "50% 50%",
  },
} satisfies Record<string, Photo>;

export type PhotoKey = keyof typeof PHOTOS;

const WIDTHS = [480, 768, 1080, 1440, 1920];

export function photoSrc(p: Photo, w: number): string {
  return `https://images.unsplash.com/${p.id}?auto=format&fit=crop&q=75&w=${w}`;
}

export function photoSrcSet(p: Photo): string {
  return WIDTHS.map((w) => `${photoSrc(p, w)} ${w}w`).join(", ");
}

/** Image Open Graph (1200×630) pour les partages sur WhatsApp, Facebook, LinkedIn… */
export function ogImage(p: Photo): string {
  return `https://images.unsplash.com/${p.id}?auto=format&fit=crop&q=75&w=1200&h=630`;
}
