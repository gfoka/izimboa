import type { MetadataRoute } from "next";
import { SITE_URL } from "@/config/site";

export const dynamic = "force-static";

export default function robots(): MetadataRoute.Robots {
  return {
    // Les fichiers .txt générés par Next.js (données de navigation) ne sont pas des pages.
    rules: [{ userAgent: "*", allow: "/", disallow: ["/*.txt$"] }],
    sitemap: `${SITE_URL}/sitemap.xml`,
  };
}
