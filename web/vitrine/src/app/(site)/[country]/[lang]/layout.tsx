import type { ReactNode } from "react";
import type { Viewport } from "next";
import { notFound } from "next/navigation";
import "@fontsource-variable/outfit";
import "@fontsource-variable/inter";
import "@/styles/globals.css";
import { OPEN_COUNTRIES, getCountry, localeTag, type Lang } from "@/config/site";

export const dynamicParams = false;

export function generateStaticParams() {
  return OPEN_COUNTRIES.flatMap((c) => c.langs.map((lang) => ({ country: c.code, lang })));
}

export const viewport: Viewport = { themeColor: "#0B2545", width: "device-width", initialScale: 1 };

export default async function SiteLayout({ children, params }: { children: ReactNode; params: Promise<{ country: string; lang: string }> }) {
  const { country, lang } = await params;
  const c = getCountry(country);
  if (!c || !c.open || !c.langs.includes(lang as Lang)) notFound();
  return (
    <html lang={localeTag(lang as Lang, country)}>
      <head>
        <link rel="preconnect" href="https://images.unsplash.com" />
      </head>
      <body>{children}</body>
    </html>
  );
}
