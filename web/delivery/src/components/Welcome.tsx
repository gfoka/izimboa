"use client";

import { useAuth } from "@/lib/auth";

const BUSINESS_URL = "https://izimboa.com/cm/fr/livraison-colis/#entreprises";

/**
 * Accueil d'une personne non connectée : ce qu'est Izimboa Delivery, puis deux portes d'entrée.
 * « Créer mon compte » et « Se connecter » mènent à la même page Keycloak : avec le code WhatsApp,
 * le compte est créé automatiquement à la première connexion (décision 71).
 */
export function Welcome() {
  const { login } = useAuth();

  return (
    <div className="welcome">
      <section className="welcome-hero" aria-labelledby="welcome-title">
        <div className="container welcome-hero-inner">
          <p className="welcome-eyebrow">Espace vendeur · Douala et Yaoundé</p>
          <h1 id="welcome-title">Faites livrer vos colis, on encaisse pour vous</h1>
          <p className="welcome-lead">
            Vous vendez sur WhatsApp, Facebook, Instagram ou en boutique ? Créez une livraison en une minute :
            un livreur Izimboa récupère le colis, votre client paie à la livraison et vous êtes reversé le jour même.
          </p>
          <ul className="welcome-points">
            <li>Prix affiché avant de confirmer, dès 1 000 FCFA</li>
            <li>Lien de suivi en direct pour votre client</li>
            <li>Argent encaissé reversé le jour même</li>
          </ul>
        </div>
      </section>

      <div className="container welcome-body">
        <section className="welcome-doors" aria-label="Accéder à l'espace vendeur">
          <div className="door door-new">
            <h2>Nouveau sur Izimboa ?</h2>
            <p>
              Créez votre compte vendeur en 30 secondes avec votre numéro WhatsApp : vous recevez un code,
              sans mot de passe à retenir.
            </p>
            <button type="button" className="btn btn-sun btn-block" onClick={() => login("/nouvelle/")}>
              Créer mon compte
            </button>
            <p className="door-note">Aucun abonnement : vous payez seulement les livraisons.</p>
          </div>
          <div className="door">
            <h2>Déjà inscrit ?</h2>
            <p>Retrouvez vos livraisons en cours, leur suivi et l&apos;argent qui vous est reversé.</p>
            <button type="button" className="btn btn-navy btn-block" onClick={() => login("/")}>
              Se connecter
            </button>
            <p className="door-note">Par code WhatsApp ou avec votre adresse e‑mail.</p>
          </div>
        </section>

        <section className="welcome-section" aria-labelledby="how-title">
          <h2 id="how-title">Comment ça marche</h2>
          <ol className="how">
            <li>
              <span className="how-num" aria-hidden="true">1</span>
              <div>
                <h3>Vous créez la livraison</h3>
                <p className="muted">Adresse de départ, client, montant à encaisser : le prix s&apos;affiche tout de suite.</p>
              </div>
            </li>
            <li>
              <span className="how-num" aria-hidden="true">2</span>
              <div>
                <h3>Un livreur récupère le colis</h3>
                <p className="muted">Le livreur le plus proche passe chez vous. Votre client reçoit un lien de suivi et un code de remise.</p>
              </div>
            </li>
            <li>
              <span className="how-num" aria-hidden="true">3</span>
              <div>
                <h3>Votre client paie, vous êtes reversé</h3>
                <p className="muted">Le livreur encaisse à la remise du colis. Izimboa vous reverse l&apos;argent le jour même.</p>
              </div>
            </li>
          </ol>
        </section>

        <section className="welcome-section" aria-labelledby="prices-title">
          <h2 id="prices-title">Des prix clairs</h2>
          <p className="muted">Selon la distance par la route, dans la ville.</p>
          <ul className="prices">
            <li><span className="price-amount">1 000 FCFA</span><span className="muted">jusqu&apos;à 3 km</span></li>
            <li><span className="price-amount">1 500 FCFA</span><span className="muted">jusqu&apos;à 7 km</span></li>
            <li><span className="price-amount">2 000 FCFA</span><span className="muted">jusqu&apos;à 12 km</span></li>
          </ul>
          <p className="muted small">Course express : + 500 FCFA. Au-delà de 12 km : sur devis.</p>
        </section>

        <aside className="welcome-business">
          <div>
            <h2>Vous expédiez beaucoup de colis ?</h2>
            <p>Entreprises et boutiques : parlons d&apos;un accompagnement et de tarifs adaptés à votre volume.</p>
          </div>
          <a className="btn btn-outline" href={BUSINESS_URL}>Contacter l&apos;équipe</a>
        </aside>
      </div>
    </div>
  );
}
