# backend — API Izimboa (Spring Boot)

API RESTful unique (`api.izimboa.com`), **monolithe modulaire** : un module par produit, des frontières vérifiées automatiquement.

| | |
|---|---|
| Langage | Java 21 |
| Framework | Spring Boot 4.1, Spring Security 7, Spring Modulith 2.1 |
| Base | PostgreSQL 17, un schéma par module, migrations Flyway |
| Connexion | Jetons Keycloak (OAuth 2 / OpenID Connect), audience `izimboa-api` |
| Documentation | OpenAPI : `/v3/api-docs`, Swagger UI : `/swagger-ui.html` |
| Build | Gradle (wrapper inclus) |

## Modules

```
com.izimboa
├── common     sécurité, utilisateur connecté, profil (schéma « common »)
└── delivery   Izimboa Delivery (schéma « delivery »)
```

Règle : un module n'utilise que les classes **publiques du package racine** d'un autre module, jamais ses
sous-packages ni ses tables. `ModularityTests` fait échouer le build en cas d'écart.

## Lancer en local

```bash
# 1. Services (PostgreSQL, Keycloak…) — une seule fois par session
cd infra/local && docker compose up -d
# 2. L'API
cd backend && ./gradlew bootRun
```

Tester :

```bash
curl http://localhost:8080/actuator/health          # {"status":"UP"}
curl http://localhost:8080/v1/delivery/zones        # public

TOKEN=$(curl -s -X POST http://localhost:8081/realms/izimboa/protocol/openid-connect/token \
  -d grant_type=password -d client_id=izimboa-dev \
  -d username=client@izimboa.local -d password=Izimboa-dev-2026 | sed -E 's/.*"access_token":"([^"]+)".*/\1/')
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/v1/me
```

## Routes

| Méthode | Route | Accès |
|---|---|---|
| GET | `/actuator/health` | public |
| GET | `/v1/delivery/zones` | public |
| GET | `/v1/me` | connecté (tout rôle) |
| GET | `/v1/delivery/tariffs` | public : grille de prix |
| PUT | `/v1/delivery/tariffs` | `admin` : remplacer la grille |
| POST | `/v1/delivery/quotes` | public : simuler un prix (vitrine) |
| POST | `/v1/delivery/deliveries` | `vendeur`, `client` (en-tête `Idempotency-Key` conseillé) |
| GET | `/v1/delivery/deliveries?scope=&status=` | connecté : les siennes, celles attribuées (livreur), toutes (opérateur) |
| GET | `/v1/delivery/deliveries/{id}` | expéditeur, livreur attribué, opérateur |
| POST | `.../{id}/assign` | `operateur`, `admin` |
| POST | `.../{id}/pickup`, `/depart`, `/deliver`, `/fail`, `/retry`, `/return` | livreur attribué (ou opérateur) |
| POST | `.../{id}/cancel` | expéditeur ou opérateur, avant le ramassage |
| POST | `.../{id}/unlock-handover` | `operateur`, `admin` |
| GET | `/v1/delivery/track/{jeton}` | public : suivi du destinataire, sans données personnelles |
| POST | `/v1/delivery/conditions` | `livreur` : « il pleut ici », « gros bouchon » |
| GET | `/v3/api-docs`, `/swagger-ui.html` | public |

Erreurs au format Problem Details (RFC 9457), avec un champ `code` stable
(`outside-service-area`, `quote-required`, `invalid-transition`, `wrong-handover-code`, `handover-locked`…).

## Tests

`./gradlew test` — unitaires, sécurité, frontières de modules, et parcours complets de livraison sur un vrai
PostgreSQL : via Docker (Testcontainers) quand il est disponible, et toujours dans GitHub Actions
(`.github/workflows/backend-ci.yml`). Sans Docker, pointer une base vide :
`TEST_DB_URL=jdbc:postgresql://localhost:5432/izimboa_test ./gradlew test`.

## Variables d'environnement

| Variable | Défaut (local) |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/izimboa` |
| `DB_USER` / `DB_PASSWORD` | `izimboa` / `izimboa` |
| `KEYCLOAK_ISSUER` | `http://localhost:8081/realms/izimboa` |
| `PORT` | `8080` |
