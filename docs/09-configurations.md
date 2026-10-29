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

- [x] Domaine izimboa.com authentifié dans Brevo (code, DKIM brevo1/brevo2, DMARC, sous-domaine de marque mail.izimboa.com) le 29/10/2026
- [x] Expéditeur « Izimboa » <noreply@izimboa.com> vérifié
- [ ] Clé API stockée dans le gestionnaire de secrets (jamais dans le dépôt)
- [ ] Clé API Brevo : secret GitHub `BREVO_API_KEY` (le déploiement la recopie dans `/srv/izimboa/.env`), puis relancer « Déploiement VPS » (active les e-mails du formulaire Entreprises du site ; d'ici là les demandes sont enregistrées en base et visibles via `GET /v1/contact-requests`)
- [ ] Vérifier que contact@izimboa.com reçoit bien les e-mails (boîte Microsoft 365)
- [ ] Expéditeur SMS (nom alphanumérique « Izimboa », vérifier les règles au Cameroun)
- [ ] Modèles : code de connexion, confirmation de commande, livraison effectuée, reçu de paiement (FR et EN)

## 4. WhatsApp Business

- [x] Portefeuille business « Data Engineering Consulting Sarl » (ID 773626450132033) : vérification de l'entreprise terminée
- [x] Application Meta « Izimboa » créée le 29/10/2026 (ID 3769090063229208), cas d'utilisation WhatsApp, rattachée au portefeuille
- [x] Numéro de test Meta (+1 555 017 9613) : ID du numéro 117113727949627, compte WhatsApp de test 116592728035046 (90 jours, 5 destinataires)
- [x] Numéro des codes Izimboa : **+237 6 72 76 99 08**, ID du numéro 112115108576086, compte WhatsApp « Data Engineering Consulting Sarl » 112090771912051 (qualité élevée)
- [ ] Nom affiché « Izimboa » : demande envoyée le 29/10/2026 (en cours d'examen ; l'ancien nom « Data Engineering Consulting Sarl » avait été refusé)
- [ ] Profil du numéro (description Izimboa, contact@izimboa.com, izimboa.com, logo) : à enregistrer après la validation du nom (Meta bloque la modification pendant l'examen)
- [x] Modèle d'authentification `izimboa_code` (« copier le code », expiration 10 min) : **actif en français et en anglais** dans le compte 112090771912051
- Autres numéros du portefeuille, à ne pas utiliser pour Izimboa : Kingston Print +237 6 93 25 48 30 (compte 1806899227122606, qui contient aussi une ancienne copie de `izimboa_code`) ; compte « production » 115373118247817 vide
- [ ] Utilisateur système + jeton permanent (droits whatsapp_business_messaging) → secret GitHub, jamais dans le dépôt
- [ ] Modèles « suivi de livraison » (utilitaires)
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

- [x] Application « Izimboa Livreur » créée (compte Play « Data Engineering Consulting », contact@dataengineeringconsulting.com)
- [ ] Compte de service Google Cloud avec accès à la console Play → clé JSON en secret GitHub (publication automatique)
- [ ] Clé de signature de l'application (upload key) en secret GitHub ; signature Play App Signing activée
- [ ] Fiche : titre, descriptions FR / EN, icône, bannière, captures (dès que l'application tourne)
- [x] Politique de confidentialité publiée sur la vitrine : https://izimboa.com/cm/fr/confidentialite/ (EN : /cm/en/privacy-policy/)
- [x] Page de suppression de compte (exigée par Play) : https://izimboa.com/cm/fr/confidentialite/#suppression-compte
- [ ] Déclaration « localisation en arrière-plan » avec une courte vidéo (avant la production)
- [x] Test interne actif : version 100 (0.1.100) publiée le 28/10/2026, liste « Testeurs Izimboa Livreur ». Lien d'inscription : https://play.google.com/apps/internaltest/4701175208671766825
- [x] Déclarations faites : règles de confidentialité, annonces (aucune), identifiant publicitaire (non)
- [ ] Déclarations restantes : informations de connexion (compte de test livreur pour Google), classification, cible et contenu, sécurité des données, applis gouvernementales, fonctionnalités financières, santé, service de premier plan (localisation, vidéo), intent plein écran
- [ ] Projet Firebase (Cloud Messaging) pour l'alerte de course qui sonne sur le téléphone du livreur

## 12. Référencement : domaines de préversion Cloudflare

- [x] Les préversions de branche (`*.izimboa.pages.dev`) reçoivent automatiquement l'en-tête `X-Robots-Tag: noindex` (comportement par défaut de Cloudflare Pages) et toutes nos pages déclarent `izimboa.com` comme adresse canonique.
- [ ] L'adresse de production `izimboa.pages.dev` n'est pas en noindex : créer une redirection 301 vers `https://izimboa.com` (Cloudflare > Règles > Redirections groupées) pour éviter tout doublon.
- Rappel : noindex n'est pas une protection. Ne jamais mettre de données privées sur une préversion.

## 13. Sécurité du serveur (contrôle du 28/10/2026, workflow « Contrôle de sécurité du serveur »)

- [x] Pare-feu : seuls 22, 80 et 443 ouverts ; base de données et API non joignables depuis Internet ; `.env` en 600.
- [x] fail2ban actif (362 adresses bannies à ce jour) ; mises à jour de sécurité automatiques ; certificats valides jusqu'en janvier 2027 (renouvellement automatique).
- [x] Corrigé : connexion SSH par mot de passe encore active (fichier de l'hébergeur prioritaire) → désactivée par `deploy.sh`.
- [x] Corrigé : aucune sauvegarde → sauvegarde quotidienne de toutes les bases, 14 jours (`backup.sh`).
- [x] Corrigé : documentation de l'API publique → masquée.
- [ ] Copie des sauvegardes **hors du serveur**, chiffrée (Cloudflare R2) : à configurer.
- [ ] Console d'administration Keycloak : remplacer l'administrateur temporaire par un compte nominatif avec double authentification.
- [ ] Utilisateur système « ubuntu » de l'hébergeur : vérifier qu'il n'a pas de mot de passe ni de clé inutile.

