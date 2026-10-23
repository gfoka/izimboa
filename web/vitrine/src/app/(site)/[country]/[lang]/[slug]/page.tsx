import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { PRODUCT_KEYS, type Lang, type ProductKey } from "@/config/site";
import { SLUGS, allPublishedPages, pageKeyFromSlug } from "@/config/routes";
import type { PhotoKey } from "@/config/photos";
import { getDict } from "@/content";
import { pageMetadata } from "@/lib/seo";
import { Shell } from "@/components/Shell";
import { ProductView } from "@/views/ProductView";
import { AboutView, ContactView, LegalView } from "@/views/InfoViews";

export const dynamicParams = false;

type Params = Promise<{ country: string; lang: string; slug: string }>;

/** Une page HTML par (pays, langue, page) publiée : rien d'autre n'existe. */
export function generateStaticParams() {
  return allPublishedPages()
    .filter((p) => p.key !== "home")
    .map((p) => ({ country: p.country, lang: p.lang, slug: SLUGS[p.key as keyof typeof SLUGS][p.lang] }));
}

function resolve(lang: Lang, slug: string) {
  const key = pageKeyFromSlug(lang, slug);
  if (!key) notFound();
  return key;
}

export async function generateMetadata({ params }: { params: Params }): Promise<Metadata> {
  const { country, lang: l, slug } = await params;
  const lang = l as Lang;
  const key = resolve(lang, slug);
  const dict = getDict(country, lang);
  const isProduct = (PRODUCT_KEYS as string[]).includes(key);
  const meta = isProduct ? dict.products[key as ProductKey].meta : dict[key as "about" | "contact" | "legal"].meta;
  const photo: PhotoKey = isProduct ? dict.products[key as ProductKey].photo : "douala";
  return pageMetadata({ country, lang, key, meta, photo });
}

export default async function Page({ params }: { params: Params }) {
  const { country, lang: l, slug } = await params;
  const lang = l as Lang;
  const key = resolve(lang, slug);
  const dict = getDict(country, lang);
  const ctx = { country, lang, dict };

  let body;
  if ((PRODUCT_KEYS as string[]).includes(key)) body = <ProductView {...ctx} product={key as ProductKey} />;
  else if (key === "about") body = <AboutView {...ctx} />;
  else if (key === "contact") body = <ContactView {...ctx} />;
  else body = <LegalView {...ctx} />;

  return (
    <Shell {...ctx} pageKey={key}>
      {body}
    </Shell>
  );
}
