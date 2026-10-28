"use client";

import type { ReactNode } from "react";
import { track } from "@/lib/track";

/** Lien ordinaire (fonctionne sans JavaScript) qui signale aussi le clic à la mesure d'audience, si acceptée. */
export function TrackedLink({ href, className, cta, children }: { href: string; className?: string; cta: string; children: ReactNode }) {
  return (
    <a href={href} className={className} onClick={() => track("cta_click", { cta, page: typeof location === "undefined" ? "" : location.pathname })}>
      {children}
    </a>
  );
}
