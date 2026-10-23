# web/vitrine — site izimboa.com

Site vitrine statique (Next.js, export HTML), bilingue FR/EN, organisé par pays.

```bash
npm install
npm run dev      # http://localhost:3000/cm/fr/
npm run build    # génère le site dans out/
npm start        # sert out/ en local
```

## Adresses publiées

| Page | Français | Anglais |
|---|---|---|
| Choix du pays (x-default) | `/` | `/` |
| Accueil | `/cm/fr/` | `/cm/en/` |
| Delivery | `/cm/fr/livraison-colis/` | `/cm/en/parcel-delivery/` |
| Go | `/cm/fr/accompagnement-immigration/` | `/cm/en/immigration-support/` |
| Fly | `/cm/fr/billets-avion/` | `/cm/en/flight-tickets/` |
| Buy | `/cm/fr/achat-international/` | `/cm/en/international-shopping/` |
| À propos | `/cm/fr/a-propos/` | `/cm/en/about/` |
| Contact | `/cm/fr/contact/` | `/cm/en/contact/` |
| Mentions légales (noindex) | `/cm/fr/mentions-legales/` | `/cm/en/legal-notice/` |

## Où modifier quoi

- `src/config/site.ts` : pays (ouvrir un pays), e-mail, WhatsApp, couleurs, liens vers les applications produits.
- `src/config/routes.ts` : pages et adresses traduites. **Changer une adresse publiée impose une redirection 301.**
- `src/content/<pays>/<langue>.ts` : tous les textes. Chaque pays a ses propres textes.
- `src/config/photos.ts` : photos (Unsplash) et leurs crédits.
- `src/styles/globals.css` : design (couleurs, typographies Outfit et Inter).

## Référencement intégré

Pages en HTML complet, canonical, hreflang (fr-CM, en-CM, x-default), sitemap.xml avec alternates,
robots.txt, données structurées (Organization, WebSite, Service, BreadcrumbList), Open Graph,
404 unique en noindex. Règles détaillées : `docs/08-referencement.md`.

## Mise en ligne

Le dossier `out/` se dépose sur n'importe quel hébergement statique (Nginx sur le VPS, Cloudflare Pages…).
L'hébergement doit : servir `404.html` avec le statut 404, rediriger en 301 `http→https` et `www→sans www`,
rediriger en 301 les adresses sans barre finale vers la forme avec barre finale.
