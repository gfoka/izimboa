import type { Metadata } from "next";
import type { Lang } from "@/config/site";
import { getDict } from "@/content";
import { pageMetadata, organizationLd, websiteLd } from "@/lib/seo";
import { Shell } from "@/components/Shell";
import { JsonLd } from "@/components/JsonLd";
import { HomeView } from "@/views/HomeView";

type Params = Promise<{ country: string; lang: string }>;

export async function generateMetadata({ params }: { params: Params }): Promise<Metadata> {
  const { country, lang } = await params;
  const dict = getDict(country, lang as Lang);
  return pageMetadata({ country, lang: lang as Lang, key: "home", meta: dict.home.meta, photo: "douala" });
}

export default async function HomePage({ params }: { params: Params }) {
  const { country, lang: l } = await params;
  const lang = l as Lang;
  const dict = getDict(country, lang);
  return (
    <Shell country={country} lang={lang} dict={dict} pageKey="home">
      <JsonLd data={[organizationLd(lang), websiteLd(country, lang)]} />
      <HomeView country={country} lang={lang} dict={dict} />
    </Shell>
  );
}
