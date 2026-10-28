import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { OPEN_COUNTRIES, getCountry, type Lang } from "@/config/site";
import { SLUGS, subPathFor } from "@/config/routes";
import { getDict } from "@/content";
import { findGoSub, goSubPages, goUi } from "@/content/cm/go";
import { pathMetadata } from "@/lib/seo";
import { Shell } from "@/components/Shell";
import { CityView, CompareView, ProgramView } from "@/views/GoViews";

export const dynamicParams = false;

type Params = Promise<{ country: string; lang: string; slug: string; sub: string }>;

/** Sous-pages d'Izimboa Go (Cameroun) : programmes, comparatif des pays, villes. */
export function generateStaticParams() {
  return OPEN_COUNTRIES.filter((c) => c.code === "cm").flatMap((c) =>
    c.langs.flatMap((lang) => goSubPages().map((p) => ({ country: c.code, lang, slug: SLUGS.go[lang], sub: p.slug[lang] }))),
  );
}

async function resolve(params: Params) {
  const { country, lang: l, slug, sub } = await params;
  const lang = l as Lang;
  if (country !== "cm" || slug !== SLUGS.go[lang]) notFound();
  const page = findGoSub(lang, sub);
  if (!page) notFound();
  const c = getCountry(country)!;
  const alt = Object.fromEntries(c.langs.map((x) => [x, subPathFor(country, x, "go", page.slug[x])])) as Record<Lang, string>;
  return { country, lang, page, alt };
}

export async function generateMetadata({ params }: { params: Params }): Promise<Metadata> {
  const { country, lang, page, alt } = await resolve(params);
  const s = page.sub;
  const meta =
    s.kind === "program"
      ? { title: s.program[lang].metaTitle, description: s.program[lang].metaDescription }
      : s.kind === "city"
        ? { title: s.city[lang].metaTitle, description: s.city[lang].metaDescription }
        : goUi(lang).compare.meta;
  return pathMetadata({ country, lang, paths: alt, meta: { title: meta.title.length <= 47 ? `${meta.title} | Izimboa Go` : meta.title, description: meta.description }, photo: "passport" });
}

export default async function Page({ params }: { params: Params }) {
  const { country, lang, page, alt } = await resolve(params);
  const dict = getDict(country, lang);
  const ctx = { country, lang, dict };
  const s = page.sub;
  return (
    <Shell {...ctx} pageKey="go" alt={alt}>
      {s.kind === "program" ? <ProgramView {...ctx} program={s.program} /> : s.kind === "city" ? <CityView {...ctx} city={s.city} /> : <CompareView {...ctx} />}
    </Shell>
  );
}
