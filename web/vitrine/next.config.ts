import type { NextConfig } from "next";

/**
 * Vitrine izimboa.com : export 100 % statique.
 * - Chaque page est générée en HTML complet au build (lisible par Google sans JavaScript).
 * - trailingSlash : une seule forme d'URL (/cm/fr/contact/), identique aux balises canonical.
 * - Servie par Cloudflare / un serveur statique : aucune erreur 5XX possible côté rendu.
 */
const nextConfig: NextConfig = {
  output: "export",
  trailingSlash: true,
  images: { unoptimized: true },
  poweredByHeader: false,
  reactStrictMode: true,
};

export default nextConfig;
