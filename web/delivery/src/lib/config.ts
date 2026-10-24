/** Adresses des services. En local, surcharger par NEXT_PUBLIC_API_URL / NEXT_PUBLIC_AUTH_URL (.env.local). */
export const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "https://api.izimboa.com";
export const AUTH_URL = process.env.NEXT_PUBLIC_AUTH_URL ?? "https://auth.izimboa.com/realms/izimboa";
export const CLIENT_ID = "izimboa-web";

/** Centre de la carte par défaut : Akwa, Douala. */
export const DOUALA = { lat: 4.0511, lng: 9.7679 };
