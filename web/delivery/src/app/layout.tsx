import type { Metadata, Viewport } from "next";
import type { ReactNode } from "react";
import "@fontsource-variable/outfit";
import "@fontsource-variable/inter";
import "@/styles/globals.css";
import { AuthProvider } from "@/lib/auth";

export const metadata: Metadata = {
  title: { default: "Izimboa Delivery", template: "%s · Izimboa Delivery" },
  description: "Espace vendeur Izimboa Delivery : créez et suivez vos livraisons à Douala.",
  robots: { index: false, follow: false },
  icons: { icon: "/icon.svg" },
};

export const viewport: Viewport = { themeColor: "#0B2545", width: "device-width", initialScale: 1 };

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="fr">
      <body>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
