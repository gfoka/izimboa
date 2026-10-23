# Environnement de développement local

Tous les services dont l'API a besoin, lancés sur votre Mac avec Docker Desktop.

## Démarrer

```bash
cd infra/local
cp .env.example .env          # une seule fois
docker compose up -d          # lance tout en arrière-plan
docker compose ps             # tous les services doivent être « healthy »
```

Le premier démarrage télécharge les images (quelques minutes). Keycloak met ensuite environ 30 secondes à être prêt.

## Services

| Service | Adresse | Identifiants (dev) |
|---|---|---|
| PostgreSQL | `localhost:5432` | base `izimboa` : `izimboa` / `izimboa` · base `keycloak` : `keycloak` / `keycloak` |
| Redis | `localhost:6379` | — |
| RabbitMQ | `localhost:5672` · console http://localhost:15672 | `izimboa` / `izimboa` |
| Keycloak | http://localhost:8081 · console http://localhost:8081/admin | `admin` / `admin` |
| Mailpit (boîte mail de test) | http://localhost:8025 · SMTP `localhost:1025` | — |

Le port **8080** est réservé à l'API Spring Boot.

## Keycloak : realm « izimboa »

Importé automatiquement au premier démarrage (`keycloak/import/izimboa-realm.json`) :

- **Rôles** : `client` (attribué à tout nouveau compte), `vendeur`, `livreur`, `conseiller`, `admin`.
- **Clients** : `izimboa-api` (l'API, qui vérifie les jetons), `izimboa-web` et `izimboa-mobile` (connexion avec PKCE), `izimboa-dev` (obtenir un jeton en ligne de commande, **dev uniquement**).
- **Comptes de test** (mot de passe `Izimboa-dev-2026`) : `client@izimboa.local`, `vendeur@izimboa.local`, `livreur@izimboa.local`, `admin@izimboa.local`.
- Pages de connexion en français et en anglais ; e-mails envoyés vers Mailpit.

Obtenir un jeton pour tester l'API :

```bash
curl -s -X POST http://localhost:8081/realms/izimboa/protocol/openid-connect/token \
  -d grant_type=password -d client_id=izimboa-dev \
  -d username=client@izimboa.local -d password=Izimboa-dev-2026
```

Les connexions Google, WhatsApp et SMS seront ajoutées avec les configurations de mise en ligne (`docs/09-configurations.md`).

## Commandes utiles

```bash
docker compose logs -f keycloak     # suivre les journaux d'un service
docker compose stop                 # arrêter (les données sont gardées)
docker compose down -v              # TOUT effacer (bases comprises) et repartir de zéro
```

Le realm et les bases ne sont créés qu'au **premier** démarrage. Après une modification de
`izimboa-realm.json` ou de `postgres/init/`, faire `docker compose down -v` puis `up -d`.

## Notes

- Mots de passe de **développement** uniquement : la production utilisera des secrets gérés à part.
- Stockage de fichiers (S3) : pas encore ici. MinIO ne publie plus d'images Docker libres depuis fin 2025 ;
  l'API commencera avec un stockage sur disque derrière une interface (architecture hexagonale),
  puis un service compatible S3 sera choisi au moment du déploiement.
