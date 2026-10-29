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

# 2. Services (apps/ : applications web statiques, déposées par leurs propres workflows)
mkdir -p apps/delivery
docker compose pull --quiet caddy postgres keycloak
docker compose up -d --remove-orphans
docker compose exec -T caddy caddy reload --config /etc/caddy/Caddyfile

# 3. Thème de connexion et extensions : Keycloak les garde en cache, on le redémarre seulement s'ils ont changé
#    (ou si le jeton WhatsApp a changé : il est lu au démarrage).
mkdir -p keycloak/providers
theme_hash=$( { find keycloak-theme keycloak/providers -type f -print0 | sort -z | xargs -0 sha256sum; grep '^WHATSAPP_' .env || true; } | sha256sum | cut -d' ' -f1)
if [ "$(cat .theme-hash 2>/dev/null || true)" != "$theme_hash" ]; then
  echo "Thème, extension ou jeton WhatsApp modifié : redémarrage de Keycloak."
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
# Adresses de retour des sites web (Keycloak n'accepte pas de joker dans le nom de domaine).
web_id=$(kc get clients -r izimboa -q clientId=izimboa-web --fields id --format csv --noquotes | tail -1 || true)
kc update "clients/$web_id" -r izimboa \
  -s 'redirectUris=["https://izimboa.com/*","https://delivery.izimboa.com/*","https://go.izimboa.com/*","https://fly.izimboa.com/*","https://buy.izimboa.com/*"]' \
  -s 'attributes."post.logout.redirect.uris"="https://izimboa.com/*##https://delivery.izimboa.com/*##https://go.izimboa.com/*##https://fly.izimboa.com/*##https://buy.izimboa.com/*"' \
  && echo "Client izimboa-web : adresses de retour à jour." \
  || echo "ATTENTION : adresses de retour de izimboa-web non mises à jour (à vérifier dans la console)."

# Rôles ajoutés après la création du realm (idempotent).
for role in operateur:"Opérateur : suit les courses en direct, réassigne, support" \
            partenaire:"Partenaire de flotte : gère ses motos et ses livreurs"; do
  name=${role%%:*}; desc=${role#*:}
  if ! kc get "roles/$name" -r izimboa > /dev/null 2>&1; then
    kc create roles -r izimboa -s "name=$name" -s "description=$desc"
    echo "Rôle « $name » créé."
  fi
done

# Connexion par code WhatsApp (décision 40) : flux « izimboa-navigateur » = session existante, sinon
# formulaire « code WhatsApp » proposé d'abord, e-mail + mot de passe en autre méthode. Idempotent.
if kc get authentication/authenticator-providers -r izimboa 2>/dev/null | grep -q '"izimboa-whatsapp-otp"'; then
  if ! kc get authentication/flows -r izimboa --fields alias --format csv --noquotes | grep -qx izimboa-navigateur; then
    kc create authentication/flows -r izimboa -s alias=izimboa-navigateur -s providerId=basic-flow \
      -s topLevel=true -s builtIn=false -s 'description=Connexion Izimboa : code WhatsApp, e-mail en option'
    kc create authentication/flows/izimboa-navigateur/executions/execution -r izimboa -s provider=auth-cookie
    kc create authentication/flows/izimboa-navigateur/executions/execution -r izimboa -s provider=identity-provider-redirector
    kc create authentication/flows/izimboa-navigateur/executions/flow -r izimboa \
      -s alias=izimboa-formulaires -s type=basic-flow -s provider=registration-page-form -s description=Formulaires
    kc create authentication/flows/izimboa-formulaires/executions/execution -r izimboa -s provider=izimboa-whatsapp-otp
    kc create authentication/flows/izimboa-formulaires/executions/execution -r izimboa -s provider=auth-username-password-form
    for id in $(kc get authentication/flows/izimboa-navigateur/executions -r izimboa --fields id --format csv --noquotes); do
      kc update authentication/flows/izimboa-navigateur/executions -r izimboa -b "{\"id\":\"$id\",\"requirement\":\"ALTERNATIVE\"}"
    done
    echo "Flux de connexion « izimboa-navigateur » créé."
  fi
  kc update realms/izimboa -s browserFlow=izimboa-navigateur
  echo "Connexion par code WhatsApp active."

  # Profil utilisateur : numéro de téléphone (lu par l'API dans le jeton) ; e-mail et nom deviennent facultatifs,
  # sinon Keycloak exigerait un e-mail des comptes créés par WhatsApp.
  if command -v python3 > /dev/null; then
    kc get users/profile -r izimboa > /tmp/izimboa-profile.json
    if python3 - /tmp/izimboa-profile.json <<'PY'
import json, sys
path = sys.argv[1]
p = json.load(open(path))
before = json.dumps(p, sort_keys=True)
for a in p["attributes"]:
    if a["name"] in ("email", "firstName", "lastName"):
        a.pop("required", None)
names = {a["name"] for a in p["attributes"]}
if "phoneNumber" not in names:
    p["attributes"].append({"name": "phoneNumber", "displayName": "${phoneNumber}", "multivalued": False,
        "validations": {"length": {"max": 20}}, "permissions": {"view": ["admin", "user"], "edit": ["admin"]}})
if "phoneNumberVerified" not in names:
    p["attributes"].append({"name": "phoneNumberVerified", "displayName": "${phoneNumberVerified}", "multivalued": False,
        "permissions": {"view": ["admin"], "edit": ["admin"]}})
json.dump(p, open(path, "w"))
sys.exit(0 if json.dumps(p, sort_keys=True) != before else 1)
PY
    then
      kc update users/profile -r izimboa -f - < /tmp/izimboa-profile.json && echo "Profil utilisateur : numéro de téléphone ajouté."
    fi
    rm -f /tmp/izimboa-profile.json
  else
    echo "ATTENTION : python3 absent, profil utilisateur non mis à jour."
  fi

  # Le numéro de téléphone dans les jetons des applications (portée standard « phone »).
  phone_scope=$(kc get client-scopes -r izimboa --fields id,name --format csv --noquotes | awk -F, '$2=="phone"{print $1}')
  for client in izimboa-web izimboa-mobile; do
    cid=$(kc get clients -r izimboa -q clientId=$client --fields id --format csv --noquotes | tail -1)
    if [ -n "$phone_scope" ] && [ -n "$cid" ] && \
       ! kc get "clients/$cid/default-client-scopes" -r izimboa --fields name --format csv --noquotes | grep -qx phone; then
      # Une portée ne peut pas être à la fois facultative et par défaut.
      kc delete "clients/$cid/optional-client-scopes/$phone_scope" -r izimboa > /dev/null 2>&1 || true
      kc update "clients/$cid/default-client-scopes/$phone_scope" -r izimboa && echo "Client $client : numéro de téléphone dans les jetons."
    fi
  done
else
  echo "ATTENTION : extension izimboa-whatsapp-otp absente de Keycloak, connexion WhatsApp non activée."
fi

# 5. Durcissement SSH : connexion par clé uniquement. Le fichier « 00- » passe avant celui de l'hébergeur
#    (50-cloud-init.conf réactivait les mots de passe). Vérifié par « sshd -t » avant rechargement.
want=$'PasswordAuthentication no\nKbdInteractiveAuthentication no\nPermitRootLogin no'
if [ "$(sudo cat /etc/ssh/sshd_config.d/00-izimboa.conf 2>/dev/null || true)" != "$want" ]; then
  printf '%s\n' "$want" | sudo tee /etc/ssh/sshd_config.d/00-izimboa.conf > /dev/null
  if sudo sshd -t; then
    sudo systemctl reload ssh 2>/dev/null || sudo systemctl reload sshd
    echo "SSH : connexion par mot de passe désactivée."
  else
    sudo rm -f /etc/ssh/sshd_config.d/00-izimboa.conf
    echo "ATTENTION : configuration SSH invalide, non appliquée."
  fi
fi

# 6. Sauvegardes quotidiennes des bases (02:30, gardées 14 jours) + une première tout de suite.
chmod 700 backup.sh
cron='30 2 * * * deploy /srv/izimboa/backup.sh >> /srv/izimboa/backups/backup.log 2>&1'
if [ "$(sudo cat /etc/cron.d/izimboa-backup 2>/dev/null || true)" != "$cron" ]; then
  echo "$cron" | sudo tee /etc/cron.d/izimboa-backup > /dev/null
  sudo chmod 644 /etc/cron.d/izimboa-backup
  echo "Sauvegarde quotidienne programmée."
fi
ls backups/izimboa-"$(date +%F)"-*.sql.gz > /dev/null 2>&1 || ./backup.sh

docker image prune -f > /dev/null
docker compose ps
