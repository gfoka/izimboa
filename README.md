# Izimboa

Plateforme du groupe Izimboa : logistique, accompagnement d'immigration et billetterie.

| Produit | Rôle | Adresse |
|---|---|---|
| **izimboa.com** | Vitrine du groupe | izimboa.com |
| **Izimboa Delivery** | Livraison de colis | delivery.izimboa.com |
| **Izimboa Go** | Accompagnement des procédures d'immigration | go.izimboa.com |
| **Izimboa Fly** | Vente de billets d'avion (site web d'abord) | fly.izimboa.com |

## Organisation du dépôt (monorepo)

```
izimboa/
├── backend/    API Spring Boot (monolithe modulaire : delivery, go, fly + modules communs)
├── web/        Sites Next.js : vitrine, Delivery, Go, Fly, administration
├── mobile/     Applications React Native (Expo) : Izimboa Delivery, Izimboa Go
├── packages/   Code TypeScript partagé web/mobile (design system, client API généré)
├── infra/      Docker Compose, Keycloak, PostgreSQL, Redis, RabbitMQ, MinIO, déploiement
└── docs/       Vision, décisions d'architecture, feuille de route
```

Les dossiers sont créés au fur et à mesure (démarche agile) : on commence par la vitrine.

Toutes les décisions prises sont consignées dans [`docs/`](docs/README.md).
