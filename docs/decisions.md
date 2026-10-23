# Journal des décisions

| # | Date | Décision |
|---|---|---|
| 1 | 2026-10 | Izimboa est une marque parapluie ; trois produits sous le même nom. |
| 2 | 2026-10 | Noms : Izimboa **Delivery** (colis), Izimboa **Go** (immigration), Izimboa **Fly** (billets). |
| 3 | 2026-10 | Domaines : vitrine sur izimboa.com, applications sur sous-domaines (delivery., go., fly., admin., api., auth.). |
| 4 | 2026-10 | Le SaaS de gestion commerciale est une marque séparée, connectée à Izimboa Delivery. |
| 5 | 2026-10 | Delivery cible d'abord les vendeurs en ligne de Douala ; promesse : « Vos colis livrés, votre argent reversé le jour même ». |
| 6 | 2026-10 | Livreurs : indépendants équipés, fixe journalier + 60-70 % de commission. |
| 7 | 2026-10 | Monorepo unique : backend, web, mobile, packages, infra, docs. |
| 8 | 2026-10 | Backend **Spring Boot**, API RESTful + OpenAPI ; web Next.js ; mobile React Native + Expo. |
| 9 | 2026-10 | Monolithe modulaire, un schéma PostgreSQL par produit ; microservices plus tard si nécessaire. |
| 10 | 2026-10 | Architecture hexagonale (ports et adaptateurs) pour tout ce qui est remplaçable. |
| 11 | 2026-10 | Authentification centrale Keycloak (SSO) : Google, WhatsApp, SMS. |
| 12 | 2026-10 | Temps réel GPS : WebSocket STOMP + Redis. |
| 13 | 2026-10 | Grand livre en double écriture, idempotence, architecture par événements (RabbitMQ). |
| 14 | 2026-10 | Hébergement : VPS 8 Go + Docker Compose + MinIO au départ, puis AWS Paris. |
| 15 | 2026-10 | Vitrine statique sans base de données. |
| 16 | 2026-10 | Fly démarre par un site web en partenariat avec une agence IATA. |
