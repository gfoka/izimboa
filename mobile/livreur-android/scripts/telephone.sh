#!/usr/bin/env bash
# Installe l'application livreur sur le téléphone Android branché en USB, la lance,
# puis enregistre son journal (erreurs comprises) dans mobile/livreur-android/build/journal-telephone.txt.
#
#   bash mobile/livreur-android/scripts/telephone.sh            # installe la dernière version de test
#   bash mobile/livreur-android/scripts/telephone.sh journal    # seulement récupérer le journal
#   bash mobile/livreur-android/scripts/telephone.sh release    # installer la version Play Store (signée, optimisée)
#
# Prérequis sur le téléphone : Paramètres > À propos > appuyer 7 fois sur « Numéro de build »,
# puis Options pour les développeurs > « Débogage USB » activé, et accepter l'empreinte du Mac.
set -uo pipefail
cd "$(dirname "$0")/.."

# 1. Trouver adb (Android Studio, Homebrew, ou déjà dans le PATH).
ADB=""
for c in "$(command -v adb 2>/dev/null)" "$HOME/Library/Android/sdk/platform-tools/adb" \
         /opt/homebrew/bin/adb /usr/local/bin/adb; do
  if [ -n "${c}" ] && [ -x "${c}" ]; then ADB="${c}"; break; fi
done
if [ -z "${ADB}" ]; then
  echo "adb introuvable. Installez-le avec :  brew install --cask android-platform-tools"
  echo "(ou ouvrez Android Studio une fois : il installe adb dans ~/Library/Android/sdk/platform-tools)"
  exit 1
fi
echo "adb : ${ADB}"

# 2. Téléphone branché et autorisé ?
"${ADB}" start-server >/dev/null 2>&1
STATE=$("${ADB}" get-state 2>&1 || true)
if [ "${STATE}" != "device" ]; then
  echo "Téléphone non prêt (${STATE})."
  echo "→ Débranchez / rebranchez, déverrouillez l'écran et acceptez « Autoriser le débogage USB »."
  "${ADB}" devices -l
  exit 1
fi
echo "Téléphone : $("${ADB}" shell getprop ro.product.manufacturer | tr -d '\r') $("${ADB}" shell getprop ro.product.model | tr -d '\r'), Android $("${ADB}" shell getprop ro.build.version.release | tr -d '\r')"

mkdir -p build
PKG=com.izimboa.livreur.debug
APK=build/izimboa-livreur.apk
if [ "${1:-}" = "release" ]; then PKG=com.izimboa.livreur; APK=build/izimboa-livreur-release.apk; fi

if [ "${1:-}" != "journal" ]; then
  if [ ! -f "${APK}" ]; then echo "APK absent : ${APK} (Claude le dépose ici)"; exit 1; fi
  echo "Installation de ${APK}..."
  # Pas d'option -g : Xiaomi (HyperOS) la refuse ; les autorisations se donnent dans l'application.
  if ! "${ADB}" install -r "${APK}"; then
    echo "Échec de l'installation."
    echo "Sur Xiaomi : Options pour les développeurs > activer « Installer via USB » (compte Xiaomi requis),"
    echo "puis accepter la fenêtre qui s'affiche sur le téléphone pendant l'installation."
    echo "Si l'erreur parle de signature : désinstallez d'abord Izimboa Livreur du téléphone, puis relancez."
    exit 1
  fi
  "${ADB}" logcat -c
  echo "Lancement de l'application..."
  "${ADB}" shell monkey -p "${PKG}" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
  echo "Utilisez l'application 30 secondes (passer en ligne...), puis appuyez sur Entrée."
  read -r _
fi

# 3. Journal : plantages + messages de l'application + localisation.
OUT=build/journal-telephone.txt
{
  echo "=== Appareil ==="
  "${ADB}" shell getprop ro.product.manufacturer; "${ADB}" shell getprop ro.product.model
  "${ADB}" shell getprop ro.build.version.release; "${ADB}" shell getprop ro.build.version.sdk
  echo "=== Services Google ==="
  "${ADB}" shell pm list packages com.google.android.gms
  echo "=== Localisation ==="
  "${ADB}" shell settings get secure location_mode
  "${ADB}" shell cmd location is-location-enabled 2>/dev/null
  echo "=== Permissions de l'application ==="
  "${ADB}" shell dumpsys package "${PKG}" | grep -E "permission|granted" | grep -iE "location|notif|foreground" | head -20
  echo "=== Plantages ==="
  "${ADB}" logcat -d -b crash
  echo "=== Journal (application, localisation, erreurs) ==="
  "${ADB}" logcat -d -v time | grep -iE "izimboa|AndroidRuntime|FATAL|LocationManager|Fused|SQLCipher|sqlcipher|ForegroundService" | tail -400
} > "${OUT}" 2>&1
echo "Journal enregistré : $(pwd)/${OUT} — dites à Claude « journal prêt »."
