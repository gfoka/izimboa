#!/usr/bin/env bash
# Range la clé de signature de l'application livreur dans les secrets GitHub (à lancer UNE fois, sur le Mac).
#   bash mobile/livreur-android/scripts/secrets-play.sh
# Fichiers attendus dans .secrets/ (déposés par Claude, exclus de Git) : upload.jks, password.txt.
set -euo pipefail
cd "$(dirname "$0")/../../.."
S=.secrets
[ -f "$S/upload.jks" ] && [ -f "$S/password.txt" ] || { echo "Fichiers absents dans $S/"; exit 1; }
if ! command -v gh >/dev/null 2>&1; then
  echo "L'outil GitHub (gh) n'est pas installé : brew install gh   puis   gh auth login"
  exit 1
fi
gh auth status >/dev/null 2>&1 || { echo "Connectez-vous d'abord : gh auth login"; exit 1; }
REPO=gfoka/izimboa
base64 < "$S/upload.jks" | tr -d '\n' | gh secret set ANDROID_KEYSTORE_BASE64 -R "$REPO"
gh secret set ANDROID_KEYSTORE_PASSWORD -R "$REPO" < "$S/password.txt"
gh secret set ANDROID_KEY_PASSWORD -R "$REPO" < "$S/password.txt"
printf 'izimboa-upload' | gh secret set ANDROID_KEY_ALIAS -R "$REPO"
echo "Secrets enregistrés :"; gh secret list -R "$REPO"
echo
echo "IMPORTANT : sauvegardez aussi $S/upload.jks et $S/password.txt dans un endroit sûr"
echo "(gestionnaire de mots de passe, clé USB rangée). Ils servent à publier les mises à jour."
