# Izimboa Livreur — application Android (Kotlin natif)

L'application des livreurs d'Izimboa Delivery : passer en ligne, recevoir une course qui sonne comme un appel,
l'accepter en 30 secondes, ramasser, livrer avec le code du client. Pensée pour la moto : gros boutons
(72 dp), contrastes forts pour le plein soleil, une seule action à la fois, français simple.

| | |
|---|---|
| Langage | Kotlin 2.4, Jetpack Compose (Material 3), coroutines / Flow |
| Android | minimum 8.0 (API 26), cible API 36, compilé avec l'API 37 |
| Connexion | Keycloak (auth.izimboa.com), client `izimboa-mobile`, AppAuth (code + PKCE), session longue `offline_access`, chiffrée par le Keystore Android |
| Position | service de premier plan « En ligne » (Fused Location, ~5 s), points envoyés par paquets, gardés en mémoire si le réseau coupe |
| Courses | interrogation toutes les 4 s pendant qu'il est en ligne ; notification plein écran + sonnerie ; Firebase Cloud Messaging plus tard |
| API | `api.izimboa.com/v1/delivery/courier/...` et étapes de livraison (voir `backend/README.md`) |

## Écrans
1. Connexion (compte Izimboa) → si le compte n'a pas le rôle `livreur` : « compte pas encore activé ».
2. Accueil : un énorme bouton rond **Passer en ligne / En ligne**.
3. Course proposée (plein écran, sonne) : gain, distance, ramassage, livraison, montant à encaisser, compte à rebours, **Accepter / Refuser**.
4. Course en cours : adresse et repère en gros, **Appeler / WhatsApp / Itinéraire** (Google Maps en mode deux-roues), une seule grande action : *J'ai ramassé* → *Je pars livrer* → *Saisir le code*.
5. Code du client : 4 grandes cases et pavé numérique géant. Problème : motifs en un appui. Fin : écran « Livraison réussie ».

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
