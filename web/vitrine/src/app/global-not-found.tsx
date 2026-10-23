import type { Metadata } from "next";
import "@fontsource-variable/outfit";
import "@fontsource-variable/inter";
import "@/styles/globals.css";
import { pathFor } from "@/config/routes";
import { Logo } from "@/components/Logo";

export const metadata: Metadata = {
  title: "Page introuvable | Page not found - Izimboa",
  robots: { index: false, follow: true },
};

/** Page 404 unique (servie avec le statut HTTP 404 par l'hébergement). */
export default function GlobalNotFound() {
  return (
    <html lang="fr">
      <body>
        <main className="chooser">
          <div className="chooser-card">
            <Logo />
            <h1 className="chooser-title">
              Page introuvable
              <span lang="en">Page not found</span>
            </h1>
            <p className="page-lead">
              Cette adresse n&apos;existe pas ou a changé.{" "}
              <span lang="en">This address doesn&apos;t exist or has changed.</span>
            </p>
            <div className="hero-actions">
              <a className="btn btn-navy" href={pathFor("cm", "fr", "home")}>
                Accueil
              </a>
              <a className="btn btn-outline" href={pathFor("cm", "en", "home")} lang="en">
                Home
              </a>
            </div>
          </div>
        </main>
      </body>
    </html>
  );
}
