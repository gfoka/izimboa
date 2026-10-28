import type { MetadataRoute } from "next";
import { SITE_URL, getCountry, localeTag } from "@/config/site";
import { NOINDEX_PAGES, absoluteUrl, allPublishedPages, pathFor, subPathFor } from "@/config/routes";
import { goSubPages } from "@/content/cm/go";

export const dynamic = "force-static";

/**
 * Plan du site : uniquement les pages publiées et indexables, chacune avec ses équivalents
 * dans les autres langues (hreflang), pour que Google relie les versions FR et EN.
 */
export default function sitemap(): MetadataRoute.Sitemap {
  const pages = allPublishedPages().filter((p) => !NOINDEX_PAGES.includes(p.key));
  const entries: MetadataRoute.Sitemap = pages.map(({ country, lang, key }) => {
    const c = getCountry(country)!;
    const languages: Record<string, string> = {};
    for (const l of c.langs) languages[localeTag(l, country)] = absoluteUrl(pathFor(country, l, key));
    languages["x-default"] = `${SITE_URL}/`;
    return {
      url: absoluteUrl(pathFor(country, lang, key)),
      changeFrequency: key === "home" ? "weekly" : "monthly",
      priority: key === "home" ? 1 : 0.8,
      alternates: { languages },
    };
  });
  // Sous-pages d'Izimboa Go au Cameroun : programmes, comparatif, villes.
  const cm = getCountry("cm")!;
  const goEntries: MetadataRoute.Sitemap = cm.open
    ? cm.langs.flatMap((lang) =>
        goSubPages().map((p) => {
          const languages: Record<string, string> = {};
          for (const l of cm.langs) languages[localeTag(l, "cm")] = absoluteUrl(subPathFor("cm", l, "go", p.slug[l]));
          return { url: absoluteUrl(subPathFor("cm", lang, "go", p.slug[lang])), changeFrequency: "weekly" as const, priority: 0.7, alternates: { languages } };
        }),
      )
    : [];
  return [{ url: `${SITE_URL}/`, changeFrequency: "monthly", priority: 0.5 }, ...entries, ...goEntries];
}
