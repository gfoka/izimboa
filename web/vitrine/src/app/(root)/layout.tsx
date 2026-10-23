import type { ReactNode } from "react";
import type { Viewport } from "next";
import "@fontsource-variable/outfit";
import "@fontsource-variable/inter";
import "@/styles/globals.css";

export const viewport: Viewport = { themeColor: "#0B2545", width: "device-width", initialScale: 1 };

/** Racine izimboa.com : page de choix du pays et de la langue (cible hreflang x-default). */
export default function RootChooserLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="fr">
      <head>
        <link rel="preconnect" href="https://images.unsplash.com" />
      </head>
      <body>{children}</body>
    </html>
  );
}
