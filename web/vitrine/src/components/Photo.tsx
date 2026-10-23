import { PHOTOS, photoSrc, photoSrcSet, type PhotoKey } from "@/config/photos";
import type { Lang } from "@/config/site";

/**
 * Image responsive : srcset + sizes (le navigateur choisit la bonne taille),
 * width/height réels (aucun décalage de mise en page), alt traduit.
 * `priority` pour l'image principale de la page (LCP) : chargée tout de suite.
 */
export function Photo({
  name,
  lang,
  sizes,
  priority = false,
  className,
}: {
  name: PhotoKey;
  lang: Lang;
  sizes: string;
  priority?: boolean;
  className?: string;
}) {
  const p = PHOTOS[name];
  return (
    <img
      className={className}
      src={photoSrc(p, 1440)}
      srcSet={photoSrcSet(p)}
      sizes={sizes}
      width={p.width}
      height={p.height}
      alt={p.alt[lang]}
      loading={priority ? "eager" : "lazy"}
      fetchPriority={priority ? "high" : "auto"}
      decoding={priority ? "sync" : "async"}
      style={{ objectPosition: p.focus }}
    />
  );
}

export function PhotoCredit({ name, label, platform }: { name: PhotoKey; label: string; platform: string }) {
  const p = PHOTOS[name];
  return (
    <p className="credit">
      {label}{" "}
      <a href={p.credit.page} rel="nofollow noopener" target="_blank">
        {p.credit.name}
      </a>{" "}
      {platform}
    </p>
  );
}
