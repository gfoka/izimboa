# Izimboa : liste des configurations de mise en ligne

À réaliser en une session dédiée, une fois les briques de code de base prêtes. Cocher chaque ligne une fois faite et vérifiée.

## 1. Domaine et DNS (fichier de zone)

- [x] Gestion DNS d'izimboa.com transférée vers Cloudflare (registrar LWS ; NS everton/jean.ns.cloudflare.com ; DNSSEC désactivé chez LWS, à réactiver via Cloudflare)
- [x] Vitrine : izimboa.com et www.izimboa.com sur Cloudflare Pages, redirection 301 www → izimboa.com
- [x] Sous-domaines api, auth, delivery, go, fly, buy → 212.47.78.167 (DNS only)
- [x] HTTPS forcé (« Always Use HTTPS » activé), HSTS via `_headers` et Caddy
- [ ] Enregistrements mail : MX (Microsoft 365), SPF unique combinant Microsoft 365 et Brevo, DKIM (Microsoft 365 et Brevo), DMARC (commencer en p=none, puis quarantine)
- [ ] Enregistrements de vérification : Google Search Console (TXT), Microsoft 365, Brevo, Meta (WhatsApp)

## 2. Messagerie Microsoft 365

- [ ] Ajouter et vérifier le domaine izimboa.com dans le tenant
- [ ] Créer les adresses : contact@, support@, noreply@ (ou alias)
- [ ] Activer DKIM côté Microsoft 365
- [ ] Tester l'envoi et la réception, vérifier les en-têtes (SPF, DKIM, DMARC = pass)

## 3. Brevo (e-mails transactionnels, SMS, WhatsApp)

- [ ] Authentifier le domaine (DKIM Brevo, ajout de Brevo au SPF)
- [ ] Expéditeur : noreply@izimboa.com
- [ ] Clé API stockée dans le gestionnaire de secrets (jamais dans le dépôt)
- [ ] Expéditeur SMS (nom alphanumérique « Izimboa », vérifier les règles au Cameroun)
- [ ] Modèles : code de connexion, confirmation de commande, livraison effectuée, reçu de paiement (FR et EN)

## 4. WhatsApp Business

- [ ] Compte Meta Business vérifié (documents de l'entreprise)
- [ ] Numéro WhatsApp Business dédié (le numéro ne doit pas être actif sur l'application WhatsApp classique s'il passe par l'API)
- [ ] Connexion directe à l'API Cloud de Meta (décision n° 29) : numéro neutre Data Engineering Consulting pour les codes de connexion
- [ ] Modèles de messages à faire approuver par Meta : code de connexion (OTP), suivi de livraison
- [ ] Ajouter le numéro sur la vitrine (WHATSAPP\_NUMBER dans src/config/site.ts)

## 5. Google Search Console et Analytics

- [ ] Search Console : propriété de type « domaine » izimboa.com (vérification DNS)
- [ ] Soumettre https://izimboa.com/sitemap.xml
- [ ] Vérifier l'indexation des pages FR et EN et les hreflang
- [ ] Google Analytics 4 : une propriété, un flux par site (vitrine, puis chaque produit)
- [ ] Bandeau de consentement aux cookies avant d'activer Analytics
- [ ] Lier Search Console et Analytics
- [ ] Fiche **Google Business Profile** à Douala (recherches locales)

## 6. Hébergement de la vitrine

- [x] Cloudflare Pages (projet `izimboa`) connecté au dépôt GitHub, dossier web/vitrine, déploiement à chaque push sur main
- [x] Commande de build : npm run build, dossier publié : out (NODE_VERSION=22)
- [x] 404.html servi avec le statut 404 (vérifié)
- [x] Redirection des adresses sans barre finale vers la forme avec barre finale (vérifié)
- [x] En-tête X-Robots-Tag: noindex sur les fichiers .txt (sauf robots.txt) — `public/_headers`
- [x] Cache long sur /\_next/static/ — `public/_headers`

## 7. Serveur (VPS) et plateforme

- [x] VPS commandé : Contabo, 2e palier, IP 212.47.78.167 (Ubuntu 24.04 recommandé)
- [x] Préparation : `infra/vps/bootstrap.sh` (utilisateur deploy, clé SSH uniquement, pare-feu, fail2ban, Docker, swap)
- [x] Caddy (HTTPS automatique) + pages « bientôt disponible » sur les sous-domaines : `infra/vps/`
- [ ] Docker Compose : PostgreSQL, Redis, RabbitMQ, Keycloak, stockage compatible S3, API Spring Boot
- [x] Keycloak sur auth.izimboa.com : realm `izimboa`, clients web et mobile (PKCE), page de connexion FR/EN
- [ ] Keycloak : fournisseurs Google, WhatsApp, SMS ; e-mails (Brevo) ; thème aux couleurs d'Izimboa
- [x] API sur api.izimboa.com + PostgreSQL 17 (interne), déployés par GitHub Actions
- [ ] Sauvegardes automatiques hors du VPS, avec un test de restauration : bucket **Cloudflare R2** + clé d'accès (secret GitHub)
- [ ] Supervision : OpenTelemetry et Grafana, alertes

## 8. CI/CD (GitHub)

- [x] Accès GitHub pour que Claude puisse pousser, **sur le modèle du projet Onacle** : application Claude installée sur le dépôt `gfoka/izimboa` (github.com/apps/claude) ou jeton « fine-grained » limité à ce dépôt (Contents: read and write)
- [ ] Objectif : une consigne depuis le téléphone → Claude écrit le code, pousse → GitHub Actions compile, teste et déploie automatiquement
- [x] GitHub Actions : déploiement automatique du VPS (`.github/workflows/deploy-vps.yml`, clé SSH dédiée `VPS_SSH_KEY`, empreinte serveur vérifiée) — 1er déploiement réussi
- [x] Vitrine : construite et publiée par Cloudflare Pages à chaque push
- [ ] GitHub Actions : tests et build du backend à chaque commit (quand l'API existera)
- [ ] Déploiement automatique : vitrine à chaque fusion sur main, backend par images Docker
- [ ] Trois environnements : développement, préproduction, production
- [ ] Secrets dans GitHub Actions (clés Brevo, base de données, Keycloak), jamais dans le code
- [ ] Protection de la branche main

## 9. Paiements (avant le lancement de Delivery)

- [ ] Comptes marchands Orange Money et MTN Mobile Money (ou agrégateur)
- [ ] Clés API de test puis de production, URL de rappel (webhooks) sur api.izimboa.com

## 10. Informations à fournir

- [ ] Adresse de contact confirmée (contact@izimboa.com ?)
- [ ] Numéro WhatsApp Business
- [ ] Mentions légales : raison sociale, forme, capital, siège, RCCM, numéro contribuable, directeur de publication, hébergeur
- [ ] Vérifier la loi camerounaise sur les données personnelles

## 11. Google Play (application livreur)

- [ ] Créer l'application « Izimboa Livreur » dans la console (compte organisation existant)
- [ ] Compte de service Google Cloud avec accès à la console Play → clé JSON en secret GitHub (publication automatique)
- [ ] Clé de signature de l'application (upload key) en secret GitHub ; signature Play App Signing activée
- [ ] Fiche : titre, descriptions FR / EN, icône, bannière, captures (dès que l'application tourne)
- [ ] Politique de confidentialité publiée sur la vitrine (obligatoire)
- [ ] Déclaration « localisation en arrière-plan » avec une courte vidéo (avant la production)
- [ ] Piste de test interne : liste des testeurs
