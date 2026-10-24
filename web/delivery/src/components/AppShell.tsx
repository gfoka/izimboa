"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState, type ReactNode } from "react";
import { useAuth } from "@/lib/auth";
import { Logo } from "./Logo";

/** Cadre de l'espace vendeur : en-tête, navigation, contenu. Demande la connexion si besoin. */
export function AppShell({ children }: { children: ReactNode }) {
  const { user, ready, login, logout } = useAuth();
  const path = usePathname();
  const [menu, setMenu] = useState(false);
  useEffect(() => setMenu(false), [path]);

  const name = user?.profile.given_name ?? user?.profile.name ?? user?.profile.email ?? "";

  return (
    <>
      <a className="skip-link" href="#contenu">Aller au contenu</a>
      <header className="app-header">
        <div className="container header-inner">
          <Link href="/" className="brand" aria-label="Izimboa Delivery, accueil"><Logo /></Link>
          {user && (
            <nav className="main-nav" aria-label="Navigation principale">
              <Link href="/" className={path === "/" ? "active" : undefined}>Mes livraisons</Link>
              <Link href="/nouvelle/" className={path.startsWith("/nouvelle") ? "active" : undefined}>Nouvelle livraison</Link>
            </nav>
          )}
          <div className="header-end">
            {user ? (
              <div className="user-menu">
                <button type="button" className="avatar" aria-expanded={menu} aria-haspopup="menu" onClick={() => setMenu(!menu)}>
                  <span aria-hidden="true">{(name || "?").charAt(0).toUpperCase()}</span>
                  <span className="sr-only">Mon compte</span>
                </button>
                {menu && (
                  <div className="menu" role="menu">
                    <p className="menu-name">{user.profile.name ?? name}</p>
                    <p className="menu-email">{user.profile.email}</p>
                    <button type="button" role="menuitem" className="menu-item" onClick={() => logout()}>Se déconnecter</button>
                  </div>
                )}
              </div>
            ) : ready ? (
              <button type="button" className="btn btn-navy btn-sm" onClick={() => login("/")}>Se connecter</button>
            ) : null}
          </div>
        </div>
      </header>
      <main id="contenu" className="app-main">{children}</main>
      {user && path !== "/nouvelle/" && (
        <Link href="/nouvelle/" className="fab" aria-label="Nouvelle livraison">
          <svg viewBox="0 0 24 24" width="26" height="26" aria-hidden="true"><path d="M12 5v14M5 12h14" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" /></svg>
        </Link>
      )}
    </>
  );
}

/** Contenu réservé aux personnes connectées. */
export function RequireLogin({ children }: { children: ReactNode }) {
  const { user, ready, login } = useAuth();
  if (!ready) return <div className="container page"><div className="skeleton tall" /></div>;
  if (!user) {
    return (
      <div className="container page narrow center">
        <h1>Connectez-vous pour continuer</h1>
        <p className="muted">Votre compte Izimboa vous donne accès à vos livraisons, sur tous vos appareils.</p>
        <button type="button" className="btn btn-sun" onClick={() => login()}>Se connecter</button>
      </div>
    );
  }
  return <>{children}</>;
}
