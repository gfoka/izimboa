# Référencement (SEO) : règles du projet

> Le référencement est une priorité : un beau site qui n'est pas visité ne sert à rien.
> Ces règles s'appliquent à la vitrine (`web/vitrine`) et à tous les futurs sites produits.

## 1. Une page = une adresse = un sujet
- Chaque sujet a **sa propre page** (pas de longue page unique) : chaque page vise ses propres recherches, avec son titre, sa description et son H1.
- Format des adresses : `izimboa.com/<pays>/<langue>/<page>/` (codes pays ISO, ex. `cm` = Cameroun).
- Les adresses sont **traduites** (`/cm/fr/livraison-colis/` ↔ `/cm/en/parcel-delivery/`).
- **Une seule forme d'URL** : minuscules, tirets, barre finale. Toute autre forme redirige en 301 vers celle-ci.
- **Ne jamais changer une adresse publiée** sans redirection 301 de l'ancienne vers la nouvelle.

## 2. Pas de contenu en double
- Chaque page porte une **balise canonical** vers sa propre adresse absolue.
- Les versions FR et EN d'une même page sont reliées par **hreflang** (`fr-CM`, `en-CM`) + `x-default` vers `izimboa.com/` (page de choix du pays). Le lien est réciproque et figure aussi dans le `sitemap.xml`.
- Le texte des pages produits de la vitrine est **différent** de celui des sites produits (`delivery.izimboa.com`…).
- **Contenus par pays** : un pays n'est publié qu'avec ses **propres** textes (villes, moyens de paiement, FAQ locales). Copier un pays en changeant seulement le nom = contenu dupliqué : interdit.
- Pays non ouverts : **aucune page publiée** (ils sont seulement listés « bientôt »). Pas de pages vides.
- Les fichiers techniques `.txt` de Next.js sont exclus par `robots.txt`.

## 3. Pages lisibles par Google, sans risque d'erreur 5XX
- Pages générées **à l'avance en HTML complet** (export statique Next.js) : Google lit tout le texte sans exécuter de JavaScript. Le JavaScript ne sert qu'aux interactions.
- Le changement de langue est un **lien vers l'autre adresse**, jamais un bouton qui change le texte sur place.
- La vitrine est un ensemble de fichiers statiques servis par Cloudflare / Nginx : **elle reste en ligne même si l'API ou la base tombent**. Aucune page ne dépend d'un serveur applicatif pour s'afficher.
- Une **seule page 404** (`404.html`), servie avec le **vrai statut HTTP 404**, en `noindex`.
- Pages sans valeur de recherche (mentions légales) : `noindex, follow` et hors sitemap.

## 4. Balises et données structurées
- `<html lang="fr-CM">` / `en-CM` selon la page.
- `title` (≈ 60 caractères, mot-clé + lieu + marque) et `meta description` (≈ 155 caractères) uniques par page.
- Un seul `H1` par page, puis H2/H3 hiérarchisés.
- Open Graph + Twitter Card (partages WhatsApp, Facebook, LinkedIn) avec image 1200×630.
- Données structurées JSON-LD : `Organization`, `WebSite` (accueil), `Service` (pages produits), `BreadcrumbList` (pages internes).
- Fil d'Ariane visible sur les pages internes ; liens internes entre services.

## 5. Performance (Core Web Vitals)
- Images responsives (`srcset`/`sizes`), dimensions déclarées (aucun décalage de mise en page), chargement différé sauf l'image principale (priorité haute).
- Polices auto-hébergées (Outfit, Inter), pas d'appel à Google Fonts.
- Peu de JavaScript ; pages légères pour les connexions mobiles lentes.
- À terme : héberger nous-mêmes les photos derrière Cloudflare (aujourd'hui servies par le CDN Unsplash).

## 6. Configuration de l'hébergement (à faire à la mise en ligne)
- Redirections 301 : `http` → `https`, `www.izimboa.com` → `izimboa.com`, adresse sans barre finale → avec barre finale.
- `404.html` servi avec le statut 404 pour toute adresse inconnue.
- En-tête `X-Robots-Tag: noindex` sur les fichiers `*.txt` sauf `robots.txt`.
- Cache long sur `/_next/static/` (fichiers versionnés).
- Search Console : propriété « domaine » `izimboa.com` + soumission du `sitemap.xml`. Analytics : une propriété GA4, un flux par site.

## 7. Évolutions prévues (pas pour le démarrage)
- **Contenus par pays gérés dans un CMS** : ouvrir un pays et renseigner ses textes, sa FAQ locale, ses carrousels sans toucher au code. Les pages restent générées en HTML statique (reconstruction à chaque publication).
- **Blog** dans un dossier du domaine principal : `izimboa.com/cm/fr/blog/…` (pas de sous-domaine `blog.`), chaque article étiqueté par produit (Delivery, Go, Fly, Buy) et affiché sur les pages concernées.
- Nouveaux pays : `/ga/fr/`, `/cg/fr/`, `/td/fr/`, `/cf/fr/`, `/gq/es/`… uniquement avec des contenus propres.
