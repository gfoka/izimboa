import type { NextConfig } from "next";

/**
 * Espace vendeur : export statique (fichiers servis par Caddy sur le VPS).
 * Toute la logique tourne dans le navigateur : connexion Keycloak (PKCE) puis appels à api.izimboa.com.
 */
const nextConfig: NextConfig = {
  output: "export",
  trailingSlash: true,
  images: { unoptimized: true },
  poweredByHeader: false,
  reactStrictMode: true,
};

export default nextConfig;
