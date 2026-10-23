# Serveur de production (VPS)

VPS Contabo (2e palier), Ubuntu 24.04, IP **212.47.78.167**. Tout tourne dans Docker, dans `/srv/izimboa`.

## 1. Préparer le serveur (une seule fois)

Depuis le Terminal du Mac :

```bash
# Si vous n'avez pas encore de clé SSH :
ssh-keygen -t ed25519 -C "votre-email"
# Installer la clé sur le serveur (mot de passe root Contabo demandé une dernière fois) :
ssh-copy-id root@212.47.78.167
# Lancer la préparation :
ssh root@212.47.78.167 'bash -s' < infra/vps/bootstrap.sh
```

Ensuite on se connecte uniquement avec `ssh deploy@212.47.78.167` (root et mots de passe désactivés).

## 2. Lancer les services

```bash
scp -r infra/vps/compose.yaml infra/vps/caddy infra/vps/site deploy@212.47.78.167:/srv/izimboa/
ssh deploy@212.47.78.167 'cd /srv/izimboa && docker compose up -d'
```

Caddy obtient tout seul les certificats HTTPS dès que les sous-domaines pointent vers le serveur.
Le déploiement est automatique : voir « Déploiement automatique » ci-dessous.

## DNS (Cloudflare)

| Nom | Type | Valeur | Proxy |
|---|---|---|---|
| api, auth, delivery, go, fly, buy | A | 212.47.78.167 | DNS uniquement (nuage gris) au départ |
| izimboa.com et www | — | Cloudflare Pages (vitrine) | activé |

Le proxy Cloudflare pourra être activé sur les sous-domaines du VPS une fois les certificats en place
(mode SSL « Full (strict) »).

## Déploiement automatique (GitHub Actions)

Workflow : `.github/workflows/deploy-vps.yml`. À chaque push sur `main` qui touche `infra/vps/`,
GitHub Actions copie `compose.yaml`, `caddy/` et `site/` dans `/srv/izimboa`, relance les services
et recharge Caddy sans coupure. Lancement manuel possible depuis l'onglet **Actions** de GitHub.

- Clé SSH dédiée : `~/.ssh/izimboa_deploy` (Mac) → secret GitHub **`VPS_SSH_KEY`** ; sa partie publique est dans
  `~deploy/.ssh/authorized_keys` sur le serveur.
- Empreinte du serveur : `infra/vps/known_hosts` (évite toute connexion à un faux serveur).

## Services en production

| Service | Adresse publique | Rôle |
|---|---|---|
| Caddy | ports 80/443 | HTTPS automatique, aiguillage vers les services |
| API (Spring Boot) | https://api.izimboa.com | `/health`, `/v1/...`, `/swagger-ui.html` |
| Keycloak | https://auth.izimboa.com | connexion centrale, realm `izimboa` (console : `/admin`) |
| PostgreSQL 17 | interne uniquement | bases `izimboa` et `keycloak` |

- **Mots de passe** : `/srv/izimboa/.env`, créé automatiquement au premier déploiement, lisible seulement par
  `deploy`. Mot de passe de la console Keycloak (utilisateur `admin`) :
  `ssh deploy@212.47.78.167 'grep KEYCLOAK_ADMIN_PASSWORD /srv/izimboa/.env'`
- **Realm de production** : `keycloak/import/izimboa-realm.json`, importé une seule fois (s'il n'existe pas).
  Pas de comptes de test ni de connexion par mot de passe en ligne de commande, contrairement au local.
- L'image de l'API est construite par GitHub Actions et envoyée directement sur le serveur (pas de registre).
- **À faire** : sauvegardes automatiques de PostgreSQL hors du serveur (voir `docs/09-configurations.md`).
