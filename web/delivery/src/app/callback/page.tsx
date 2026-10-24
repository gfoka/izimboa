"use client";

import { useEffect, useRef, useState } from "react";
import { completeLogin } from "@/lib/auth";

/** Retour de Keycloak après la connexion. */
export default function Callback() {
  const done = useRef(false);
  const [error, setError] = useState(false);
  useEffect(() => {
    if (done.current) return;
    done.current = true;
    completeLogin()
      .then((to) => window.location.replace(to))
      .catch(() => setError(true));
  }, []);
  return (
    <main className="container page narrow center">
      {error ? (
        <>
          <h1>La connexion n'a pas abouti</h1>
          <p className="muted">Le lien a peut-être expiré. Recommencez depuis l'accueil.</p>
          <a className="btn btn-navy" href="/">Retour à l'accueil</a>
        </>
      ) : (
        <p className="muted" role="status">Connexion en cours…</p>
      )}
    </main>
  );
}
