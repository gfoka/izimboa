import type { Metadata } from "next";
import { COUNTRIES, OPEN_COUNTRIES, SITE_URL, localeTag } from "@/config/site";
import { absoluteUrl, pathFor } from "@/config/routes";
import { ogImage, PHOTOS } from "@/config/photos";
import { organizationLd } from "@/lib/seo";
import { JsonLd } from "@/components/JsonLd";
import { Logo } from "@/components/Logo";
import { Photo } from "@/components/Photo";

const TITLE = "Izimboa : livraison, immigration, voyages | Delivery, immigration, travel";
const DESCRIPTION =
  "Izimboa au Cameroun et en Afrique centrale : choisissez votre pays et votre langue. Izimboa in Cameroon and Central Africa: choose your country and language.";

function languages() {
  const l: Record<string, string> = {};
  for (const c of OPEN_COUNTRIES) for (const lang of c.langs) l[localeTag(lang, c.code)] = absoluteUrl(pathFor(c.code, lang, "home"));
  l["x-default"] = `${SITE_URL}/`;
  return l;
}

export const metadata: Metadata = {
  metadataBase: new URL(SITE_URL),
  title: { absolute: TITLE },
  description: DESCRIPTION,
  alternates: { canonical: `${SITE_URL}/`, languages: languages() },
  openGraph: {
    type: "website",
    url: `${SITE_URL}/`,
    siteName: "Izimboa",
    title: TITLE,
    description: DESCRIPTION,
    images: [{ url: ogImage(PHOTOS.douala), width: 1200, height: 630, alt: PHOTOS.douala.alt.fr }],
  },
  twitter: { card: "summary_large_image", title: TITLE, description: DESCRIPTION, images: [ogImage(PHOTOS.douala)] },
};

export default function RootPage() {
  return (
    <main className="chooser">
      <JsonLd data={organizationLd("fr")} />
      <Photo name="douala" lang="fr" sizes="100vw" priority className="chooser-img" />
      <div className="chooser-scrim" aria-hidden="true" />
      <div className="chooser-card">
        <Logo />
        <h1 className="chooser-title">
          Choisissez votre pays
          <span lang="en">Choose your country</span>
        </h1>
        <ul className="chooser-list">
          {OPEN_COUNTRIES.map((c) => (
            <li key={c.code} className="chooser-country">
              <span className="chooser-name">{c.name.fr}</span>
              <span className="chooser-langs">
                {c.langs.map((lang) => (
                  <a key={lang} href={pathFor(c.code, lang, "home")} hrefLang={localeTag(lang, c.code)} lang={lang} className="btn btn-navy btn-sm">
                    {lang === "fr" ? "Français" : "English"}
                  </a>
                ))}
              </span>
            </li>
          ))}
        </ul>
        <p className="chooser-soon">
          Bientôt <span lang="en">/ Coming soon</span> :{" "}
          {COUNTRIES.filter((c) => !c.open)
            .map((c) => c.name.fr)
            .join(" · ")}
        </p>
      </div>
    </main>
  );
}
