"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import { AppShell, RequireLogin } from "@/components/AppShell";
import type { CourierFileStatus } from "@/lib/api";
import { TEAM_ROLES, useAuth } from "@/lib/auth";

/**
 * Administration d'Izimboa (décision 74) : validateurs pour les livreurs, administrateurs pour le modèle
 * de documents et l'équipe. L'API vérifie chaque droit ; ici on n'affiche que ce que la personne peut faire.
 */
export function AdminShell({ children, adminOnly = false }: { children: ReactNode; adminOnly?: boolean }) {
  return (
    <AppShell>
      <RequireLogin>
        <AdminGate adminOnly={adminOnly}>{children}</AdminGate>
      </RequireLogin>
    </AppShell>
  );
}

function AdminGate({ children, adminOnly }: { children: ReactNode; adminOnly: boolean }) {
  const { roles } = useAuth();
  const path = usePathname();
  const isAdmin = roles.includes("admin");
  const allowed = adminOnly ? isAdmin : roles.some((r) => TEAM_ROLES.includes(r));
  if (!allowed) {
    return (
      <div className="container page narrow center">
        <h1>Accès réservé</h1>
        <p className="muted">Cette page est réservée à l&apos;équipe Izimboa.</p>
        <Link href="/" className="btn btn-navy">Retour à mes livraisons</Link>
      </div>
    );
  }
  const tabs = [
    { href: "/admin/livreurs/", label: "Livreurs", show: true },
    { href: "/admin/documents/", label: "Documents demandés", show: isAdmin },
    { href: "/admin/equipe/", label: "Équipe", show: isAdmin },
  ];
  return (
    <div className="container page">
      <nav className="tabs" aria-label="Administration">
        {tabs.filter((t) => t.show).map((t) => (
          <Link key={t.href} href={t.href}
            className={path.startsWith(t.href) || (t.href === "/admin/livreurs/" && path.startsWith("/admin/livreur/")) ? "tab active" : "tab"}>
            {t.label}
          </Link>
        ))}
      </nav>
      {children}
    </div>
  );
}

export const FILE_STATUS: Record<CourierFileStatus, { label: string; badge: string }> = {
  DRAFT: { label: "En cours de saisie", badge: "badge-off" },
  PENDING: { label: "À vérifier", badge: "badge-wait" },
  ACTIVE: { label: "Actif", badge: "badge-ok" },
  REJECTED: { label: "Refusé", badge: "badge-bad" },
  SUSPENDED: { label: "Suspendu", badge: "badge-bad" },
};

export function FileBadge({ status }: { status: CourierFileStatus }) {
  const s = FILE_STATUS[status];
  return <span className={`badge ${s.badge}`}>{s.label}</span>;
}
