# Architecture technique

Objectif : une architecture **robuste, évolutive, de niveau bancaire**, puisque la plateforme manipule de l'argent et des documents sensibles.

## Vue d'ensemble
- **Monorepo** : tout Izimboa dans un seul dépôt ; chaque application se déploie séparément.
- **Backend : Spring Boot (Java)** — choisi pour sa maturité, Spring Security et l'écosystème Spring Cloud.
- **Web : Next.js (TypeScript)** — référencement, rapidité même en connexion lente.
- **Mobile : React Native + Expo (TypeScript)** — un code Android/iOS, mises à jour instantanées.
- **Base : PostgreSQL**. **Cache / temps réel : Redis**. **Messagerie : RabbitMQ**.

## API
- API **RESTful** indépendante sur `api.izimboa.com`, documentée en **OpenAPI** ; clients web/mobile générés depuis cette spécification.
- Les frontends ne touchent **jamais** la base de données.
- **API Gateway** (Spring Cloud Gateway) : point d'entrée unique, vérification des jetons, limitation des abus.

## Monolithe modulaire → microservices
- Démarrage en monolithe modulaire : modules `delivery`, `go`, `fly` + modules communs.
- Une seule base PostgreSQL, **un schéma par produit** (`delivery`, `go`, `fly`, `common`).
- Règle d'or : un module ne lit jamais les tables d'un autre ; il passe par son service.
- Découpage en microservices uniquement quand un produit doit grandir seul (on déplace alors son schéma dans sa propre base).

## Architecture hexagonale (ports et adaptateurs)
Le métier ne dépend que d'interfaces ; les implémentations se changent par configuration Spring, sans toucher au code métier :
- Stockage : local / MinIO / S3
- Paiement : Orange Money, MTN Mobile Money…
- Notifications : SMS, WhatsApp, e-mail, push
- Messagerie : RabbitMQ / Kafka

## Authentification (SSO)
- Service central **Keycloak** sur `auth.izimboa.com` — OAuth 2 / OpenID Connect, jetons JWT vérifiés par l'API.
- Connexion unique pour tous les produits (comme Google : Gmail, YouTube, Drive).
- Méthodes : **Google** (natif), **WhatsApp** (code via WhatsApp Business API, authentificateur Keycloak personnalisé — déjà testé sur un autre projet), **SMS** en secours. Un même compte peut lier plusieurs méthodes.
- Rôles : client, livreur, conseiller immigration, administrateur.
- Les comptes, mots de passe, rôles et sessions vivent **uniquement** dans Keycloak. Chaque produit ne stocke que son métier + l'identifiant utilisateur (et éventuellement un profil métier, ex. moto et zone du livreur).

## Suivi GPS en temps réel (Delivery)
1. Le téléphone du livreur envoie sa position (~toutes les 5 s, y compris en arrière-plan).
2. Le backend stocke la dernière position dans **Redis** ; l'historique du trajet est enregistré périodiquement en base (utile en cas de litige).
3. Le client est abonné au canal de sa livraison via **WebSocket** (Spring WebSocket + STOMP) et voit la moto avancer.
4. Plusieurs serveurs : Redis ou RabbitMQ sert de relais.
5. Application fermée : notifications push.
- Cartes : Google Maps ou Mapbox. Adressage : épingle + repère texte.

## Argent et fiabilité
- **Grand livre comptable en double écriture** : paiements, encaissements livreurs, commissions, reversements. Écritures jamais modifiées ni supprimées. Rapprochement automatique quotidien avec Orange Money et MTN.
- **Idempotence** des paiements : aucun double débit en cas de coupure réseau.
- **Architecture par événements** : ex. « livraison terminée » → facturation, notifications, comptabilité réagissent séparément ; aucun événement perdu.

## Sécurité et exploitation
- Documents d'identité (Go) **chiffrés** dans un stockage dédié, chaque consultation tracée (journal d'audit).
- **Observabilité** : logs, métriques, alertes (OpenTelemetry + Grafana).
- **Sauvegardes** automatiques testées, plan de reprise.
- **CI/CD** : tests et déploiements automatiques, Docker puis Kubernetes.
- **Trois environnements** : développement, préproduction, production.
- **Cloudflare** devant tout (accélération, protection).

## Mobile
- Mode **hors connexion** avec synchronisation au retour du réseau (essentiel pour les livreurs).

## Transverse
- **Bilingue FR/EN** dès le départ.
- **Vitrine** : site statique Next.js sans base ; formulaires via l'API. CMS possible plus tard.
- **Search Console** : propriété « domaine » izimboa.com (DNS) + une propriété par sous-domaine.
- **Analytics** : une propriété GA4 pour toute la marque, un flux par site et par application.
