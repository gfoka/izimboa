#!/usr/bin/env bash
# Sauvegarde quotidienne de toutes les bases (Izimboa + Keycloak), gardée 14 jours sur le serveur.
# Lancée chaque nuit par /etc/cron.d/izimboa-backup (installé par deploy.sh).
# À compléter : copie hors serveur chiffrée (Cloudflare R2), voir docs/09-configurations.md.
set -euo pipefail
cd /srv/izimboa
umask 077
mkdir -p backups
f="backups/izimboa-$(date +%F-%H%M).sql.gz"
docker compose exec -T postgres pg_dumpall -U postgres | gzip > "$f.tmp"
mv "$f.tmp" "$f"
find backups -name 'izimboa-*.sql.gz' -mtime +14 -delete
echo "Sauvegarde : $f ($(du -h "$f" | cut -f1))"
