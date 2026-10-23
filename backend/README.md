# backend — API Izimboa (Spring Boot)

API RESTful unique, exposée sur `api.izimboa.com`, documentée en OpenAPI.

- **Monolithe modulaire** : un module par produit (`delivery`, `go`, `fly`, `buy`, `saas`) + modules communs (`payments`, `notifications`, `ledger`, `storage`…).
- **Architecture hexagonale** (ports et adaptateurs) : le métier ne dépend que d'interfaces ; les implémentations (stockage local/MinIO/S3, Mobile Money, SMS, WhatsApp, e-mail) se changent par configuration.
- **Règle d'or** : un module ne lit jamais les tables d'un autre ; il passe par son service.
- **Base** : PostgreSQL, un schéma par produit (`delivery`, `go`, `fly`, `buy`, `saas`, `common`). Aucune donnée d'authentification ici : seulement l'identifiant utilisateur issu du jeton.
- **Temps réel** : WebSocket (Spring WebSocket + STOMP) pour le suivi GPS ; positions en Redis.
- **Événements** : RabbitMQ (Kafka plus tard si besoin).

_Vide pour l'instant._
