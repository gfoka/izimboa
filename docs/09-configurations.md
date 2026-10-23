# Izimboa : liste des configurations de mise en ligne

À réaliser en une session dédiée, une fois les briques de code de base prêtes. Cocher chaque ligne une fois faite et vérifiée.

## 1. Domaine et DNS (fichier de zone)

- [x] Gestion DNS d'izimboa.com transférée vers Cloudflare (registrar LWS ; NS everton/jean.ns.cloudflare.com ; DNSSEC désactivé chez LWS, à réactiver via Cloudflare)
- [ ] Enregistrements de la vitrine : izimboa.com et www (redirection 301 www vers sans www)
- [x] Sous-domaines api, auth, delivery, go, fly, buy → 212.47.78.167 (DNS only)
- [ ] HTTPS forcé (redirection 301 http vers https), HSTS
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
- [ ] Connexion via Brevo (ou API Cloud de Meta)
- [ ] Modèles de messages à faire approuver par Meta : code de connexion (OTP), suivi de livraison
- [ ] Ajouter le numéro sur la vitrine (WHATSAPP\_NUMBER dans src/config/site.ts)

## 5. Google Search Console et Analytics

- [ ] Search Console : propriété de type « domaine » izimboa.com (vérification DNS)
- [ ] Soumettre https://izimboa.com/sitemap.xml
- [ ] Vérifier l'indexation des pages FR et EN et les hreflang
- [ ] Google Analytics 4 : une propriété, un flux par site (vitrine, puis chaque produit)
- [ ] Bandeau de consentement aux cookies avant d'activer Analytics
- [ ] Lier Search Console et Analytics

## 6. Hébergement de la vitrine

- [ ] Cloudflare Pages (ou Nginx sur le VPS) connecté au dépôt GitHub, dossier web/vitrine
- [ ] Commande de build : npm run build, dossier publié : out
- [ ] 404.html servi avec le statut 404
- [ ] Redirection des adresses sans barre finale vers la forme avec barre finale
- [ ] En-tête X-Robots-Tag: noindex sur les fichiers .txt (sauf robots.txt)
- [ ] Cache long sur /\_next/static/

## 7. Serveur (VPS) et plateforme

- [x] VPS commandé : Contabo, 2e palier, IP 212.47.78.167 (Ubuntu 24.04 recommandé)
- [x] Préparation : `infra/vps/bootstrap.sh` (utilisateur deploy, clé SSH uniquement, pare-feu, fail2ban, Docker, swap)
- [x] Caddy (HTTPS automatique) + pages « bientôt disponible » sur les sous-domaines : `infra/vps/`
- [ ] Docker Compose : PostgreSQL, Redis, RabbitMQ, Keycloak, stockage compatible S3, API Spring Boot
- [ ] Keycloak sur auth.izimboa.com : realm, clients (web, mobile), fournisseurs Google, WhatsApp, SMS
- [ ] Sauvegardes automatiques hors du VPS, avec un test de restauration
- [ ] Supervision : OpenTelemetry et Grafana, alertes

## 8. CI/CD (GitHub)

- [x] Accès GitHub pour que Claude puisse pousser, **sur le modèle du projet Onacle** : application Claude installée sur le dépôt `gfoka/izimboa` (github.com/apps/claude) ou jeton « fine-grained » limité à ce dépôt (Contents: read and write)
- [ ] Objectif : une consigne depuis le téléphone → Claude écrit le code, pousse → GitHub Actions compile, teste et déploie automatiquement
- [ ] GitHub Actions : vérification et build à chaque commit (vitrine, backend)
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
