"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { UserManager, WebStorageStateStore, type User } from "oidc-client-ts";
import { AUTH_URL, CLIENT_ID } from "./config";

/**
 * Connexion centrale Keycloak (auth.izimboa.com), flux « code + PKCE ».
 * Les jetons restent dans la session de l'onglet (sessionStorage) ; la session Keycloak (cookie)
 * reconnecte sans mot de passe au prochain passage.
 */
let manager: UserManager | null = null;
function userManager(): UserManager {
  if (!manager) {
    const origin = window.location.origin;
    manager = new UserManager({
      authority: AUTH_URL,
      client_id: CLIENT_ID,
      redirect_uri: `${origin}/callback/`,
      post_logout_redirect_uri: `${origin}/`,
      response_type: "code",
      scope: "openid profile email",
      automaticSilentRenew: true,
      userStore: new WebStorageStateStore({ store: window.sessionStorage }),
    });
  }
  return manager;
}

type AuthState = {
  user: User | null;
  ready: boolean;
  login: (returnTo?: string) => Promise<void>;
  logout: () => Promise<void>;
  token: () => Promise<string | null>;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const um = userManager();
    um.getUser().then((u) => {
      setUser(u && !u.expired ? u : null);
      setReady(true);
    });
    const onLoaded = (u: User) => setUser(u);
    const onUnloaded = () => setUser(null);
    um.events.addUserLoaded(onLoaded);
    um.events.addUserUnloaded(onUnloaded);
    um.events.addSilentRenewError(onUnloaded);
    return () => {
      um.events.removeUserLoaded(onLoaded);
      um.events.removeUserUnloaded(onUnloaded);
      um.events.removeSilentRenewError(onUnloaded);
    };
  }, []);

  const login = useCallback(async (returnTo?: string) => {
    const target = returnTo ?? window.location.pathname + window.location.search;
    await userManager().signinRedirect({ state: { returnTo: target }, ui_locales: "fr" } as never);
  }, []);

  const logout = useCallback(async () => {
    await userManager().signoutRedirect();
  }, []);

  const token = useCallback(async () => {
    const u = await userManager().getUser();
    if (!u) return null;
    if (u.expired) {
      try {
        const renewed = await userManager().signinSilent();
        return renewed?.access_token ?? null;
      } catch {
        return null;
      }
    }
    return u.access_token;
  }, []);

  const value = useMemo(() => ({ user, ready, login, logout, token }), [user, ready, login, logout, token]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth hors de AuthProvider");
  return ctx;
}

/** Termine la connexion au retour de Keycloak et renvoie l'adresse où retourner. */
export async function completeLogin(): Promise<string> {
  const u = await userManager().signinRedirectCallback();
  const state = u.state as { returnTo?: string } | undefined;
  const returnTo = state?.returnTo;
  return returnTo && returnTo.startsWith("/") && !returnTo.startsWith("/callback") ? returnTo : "/";
}
