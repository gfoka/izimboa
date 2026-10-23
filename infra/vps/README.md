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
scp -r infra/vps/compose.yaml infra/vps/Caddyfile infra/vps/site deploy@212.47.78.167:/srv/izimboa/
ssh deploy@212.47.78.167 'cd /srv/izimboa && docker compose up -d'
```

Caddy obtient tout seul les certificats HTTPS dès que les sous-domaines pointent vers le serveur.
(Plus tard, le déploiement se fera automatiquement par GitHub Actions à chaque push.)

## DNS (Cloudflare)

| Nom | Type | Valeur | Proxy |
|---|---|---|---|
| api, auth, delivery, go, fly, buy | A | 212.47.78.167 | DNS uniquement (nuage gris) au départ |
| izimboa.com et www | — | Cloudflare Pages (vitrine) | activé |

Le proxy Cloudflare pourra être activé sur les sous-domaines du VPS une fois les certificats en place
(mode SSL « Full (strict) »).
