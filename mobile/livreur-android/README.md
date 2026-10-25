# Izimboa Livreur — application Android (Kotlin natif)

L'application des livreurs d'Izimboa Delivery : passer en ligne, recevoir une course qui sonne comme un appel,
l'accepter en 30 secondes, ramasser, livrer avec le code du client. Pensée pour la moto : gros boutons
(72 dp), contrastes forts pour le plein soleil, une seule action à la fois, français simple.

| | |
|---|---|
| Langage | Kotlin 2.4, Jetpack Compose (Material 3), coroutines / Flow |
| Android | minimum 8.0 (API 26), cible API 36, compilé avec l'API 37 |
| Connexion | Keycloak (auth.izimboa.com), client `izimboa-mobile`, AppAuth (code + PKCE), session longue `offline_access`, chiffrée par le Keystore Android |
| Hors connexion | base **SQLite chiffrée (SQLCipher)**, clé dans le Keystore : positions et actions gardées sans réseau, renvoyées dans l'ordre au retour du réseau (WorkManager), avec leur heure réelle |
| Position | GPS proportionnel à la vitesse : en attente, 1 point / 50 m (précision quartier) ; en course, 1 point / 15 m (GPS précis) ; rien à l'arrêt ; envois groupés (20 s en course, 60 s en attente) |
| Courses | attente longue (une requête de 20 s) pendant qu'il est en ligne ; notification plein écran + sonnerie ; Firebase Cloud Messaging plus tard |
| API | `api.izimboa.com/v1/delivery/courier/...` et étapes de livraison (voir `backend/README.md`) |

## Écrans
1. Connexion (compte Izimboa) → si le compte n'a pas le rôle `livreur` : « compte pas encore activé ».
2. Accueil : un énorme bouton rond **Passer en ligne / En ligne**.
3. Course proposée (plein écran, sonne) : gain, distance, ramassage, livraison, montant à encaisser, compte à rebours, **Accepter / Refuser**.
4. Course en cours : adresse et repère en gros, **Appeler / WhatsApp / Itinéraire** (Google Maps en mode deux-roues), une seule grande action : *J'ai ramassé* → *Je pars livrer* → *Saisir le code*.
5. Code du client : 4 grandes cases et pavé numérique géant. Problème : motifs en un appui. Fin : écran « Livraison réussie ».

## Sans réseau
Le livreur peut couper ses données mobiles pour économiser sa batterie : « J'ai ramassé », « Je pars », un échec,
une nouvelle tentative ou un retour s'enregistrent sur le téléphone et l'écran avance tout de suite ; tout part
au serveur dès que le réseau revient. Seule la **validation du code du client** demande quelques secondes de réseau
(le code est vérifié par le serveur, jamais stocké sur le téléphone du livreur).

## Construire

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug     # APK de test : app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest -Droborazzi.test.record=true   # captures des écrans dans app/build/screens/
```

Dans GitHub : chaque modification produit un APK de test (Actions → « Application livreur (Android) » → Artifacts).
Pour l'installer sur un téléphone : autoriser « installer des applications inconnues ».
Activer un livreur : dans Keycloak, donner le rôle `livreur` au compte.

## Google Play
La version signée part automatiquement sur la piste de **test interne** dès que ces secrets GitHub existent :
`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`, `PLAY_SERVICE_ACCOUNT_JSON`
(voir `docs/09-configurations.md`, section 11). La toute première version doit être envoyée à la main dans la console Play.
