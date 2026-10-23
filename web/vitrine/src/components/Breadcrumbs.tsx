import { JsonLd } from "./JsonLd";
import { breadcrumbLd } from "@/lib/seo";
import { SITE_URL } from "@/config/site";

/** Fil d'Ariane visible + données structurées BreadcrumbList. */
export function Breadcrumbs({ items, label }: { items: { name: string; href: string }[]; label: string }) {
  return (
    <>
      <nav aria-label={label} className="breadcrumbs container">
        <ol>
          {items.map((it, i) => (
            <li key={it.href}>
              {i < items.length - 1 ? <a href={it.href}>{it.name}</a> : <span aria-current="page">{it.name}</span>}
            </li>
          ))}
        </ol>
      </nav>
      <JsonLd data={breadcrumbLd(items.map((it) => ({ name: it.name, url: `${SITE_URL}${it.href}` })))} />
    </>
  );
}
