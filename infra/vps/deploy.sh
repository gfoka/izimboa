#!/usr/bin/env bash
# Exécuté sur le serveur par GitHub Actions après la copie des fichiers (dans /srv/izimboa).
set -euo pipefail
cd "$(dirname "$0")"
rm -f Caddyfile   # ancien emplacement (avant le dossier caddy/)

# 1. Secrets : générés une seule fois, sur le serveur uniquement (jamais dans Git ni dans GitHub).
if [ ! -f .env ]; then
  umask 077
  {
    echo "POSTGRES_SUPERUSER_PASSWORD=$(openssl rand -hex 24)"
    echo "IZIMBOA_DB_PASSWORD=$(openssl rand -hex 24)"
    echo "KEYCLOAK_DB_PASSWORD=$(openssl rand -hex 24)"
    echo "KEYCLOAK_ADMIN_PASSWORD=$(openssl rand -base64 18 | tr -d '/+=')"
  } > .env
  echo "Fichier .env créé avec de nouveaux mots de passe."
fi

# 2. Services
docker compose pull --quiet caddy postgres keycloak
docker compose up -d --remove-orphans
docker compose exec -T caddy caddy reload --config /etc/caddy/Caddyfile

# 3. Thème de connexion : Keycloak le garde en cache, on le redémarre seulement si le thème a changé.
theme_hash=$(find keycloak-theme -type f -print0 | sort -z | xargs -0 sha256sum | sha256sum | cut -d' ' -f1)
if [ "$(cat .theme-hash 2>/dev/null || true)" != "$theme_hash" ]; then
  echo "Thème modifié : redémarrage de Keycloak."
  docker compose restart keycloak
  echo "$theme_hash" > .theme-hash
fi

# 4. Réglages du realm existant (l'import initial ne s'applique qu'à la création).
echo "Attente de Keycloak..."
for _ in $(seq 1 60); do
  if curl -fsS -o /dev/null https://auth.izimboa.com/realms/izimboa/.well-known/openid-configuration; then break; fi
  sleep 5
done
set -a; . ./.env; set +a
kc() { docker compose exec -T keycloak /opt/keycloak/bin/kcadm.sh "$@" --config /tmp/kcadm.config; }
kc config credentials --server http://localhost:8080 --realm master --user admin --password "$KEYCLOAK_ADMIN_PASSWORD" > /dev/null
kc update realms/izimboa -s loginTheme=izimboa -s 'displayNameHtml=<strong>izimboa</strong>'
echo "Realm izimboa : thème de connexion « izimboa » appliqué."

docker image prune -f > /dev/null
docker compose ps
