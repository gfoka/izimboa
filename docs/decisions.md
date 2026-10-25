# Journal des décisions

| # | Date | Décision |
|---|---|---|
| 1 | 2026-10 | Izimboa est une marque parapluie ; trois produits sous le même nom. |
| 2 | 2026-10 | Noms : Izimboa **Delivery** (colis), Izimboa **Go** (immigration), Izimboa **Fly** (billets). |
| 3 | 2026-10 | Domaines : vitrine sur izimboa.com, applications sur sous-domaines (delivery., go., fly., admin., api., auth.). |
| 4 | 2026-10 | Le SaaS de gestion commerciale est une marque séparée, connectée à Izimboa Delivery. |
| 5 | 2026-10 | Delivery cible d'abord les vendeurs en ligne de Douala ; promesse : « Vos colis livrés, votre argent reversé le jour même ». |
| 6 | 2026-10 | Livreurs : indépendants équipés, fixe journalier + 60-70 % de commission. |
| 7 | 2026-10 | Monorepo unique : backend, web, mobile, packages, infra, docs. |
| 8 | 2026-10 | Backend **Spring Boot**, API RESTful + OpenAPI ; web Next.js ; mobile React Native + Expo. |
| 9 | 2026-10 | Monolithe modulaire, un schéma PostgreSQL par produit ; microservices plus tard si nécessaire. |
| 10 | 2026-10 | Architecture hexagonale (ports et adaptateurs) pour tout ce qui est remplaçable. |
| 11 | 2026-10 | Authentification centrale Keycloak (SSO) : Google, WhatsApp, SMS. |
| 12 | 2026-10 | Temps réel GPS : WebSocket STOMP + Redis. |
| 13 | 2026-10 | Grand livre en double écriture, idempotence, architecture par événements (RabbitMQ). |
| 14 | 2026-10 | Hébergement : VPS 8 Go + Docker Compose + MinIO au départ, puis AWS Paris. |
| 15 | 2026-10 | Vitrine statique sans base de données. |
| 16 | 2026-10 | Fly démarre par un site web en partenariat avec une agence IATA. |
| 17 | 2026-10 | Nouveau produit **Izimboa Buy** (achat à l'étranger pour le client), portail web seulement, sur buy.izimboa.com. |
| 18 | 2026-10 | Le SaaS de gestion commerciale est une **suite complète** (achats, ventes, stock, facturation, boutique, WhatsApp, centre d'appels). Nom à définir. |
| 19 | 2026-10 | Le SaaS vit dans le **même monorepo et le même backend** (module `saas`, schéma dédié), sous une marque séparée ; extraction possible plus tard. |
| 20 | 2026-10 | Keycloak : realm séparé pour le SaaS (connexion à sa marque, domaines personnalisés). |
| 21 | 2026-10 | Brevo comme fournisseur unique de départ pour e-mail, SMS et WhatsApp (derrière une interface remplaçable). |
| 22 | 2026-10 | Logo : le « i » avec une flèche à la place du point ; une variante de point par produit. Piste « aigle » écartée. Couleurs bleu nuit / orange soleil, polices Outfit et Inter. |
| 23 | 2026-10 | Projet régional : adresses `/<pays>/<langue>/<page>/`, Cameroun ouvert en premier, autres pays CEMAC « bientôt » sans pages publiées. |
| 24 | 2026-10 | Vitrine : une page par sujet, adresses traduites, HTML statique (export Next.js), canonical + hreflang + sitemap ; règles dans `08-referencement.md`. |
| 25 | 2026-10 | Blog plus tard dans un dossier du domaine (`/cm/fr/blog/`), articles étiquetés par produit ; contenus par pays via un CMS plus tard. |
| 26 | 2026-10 | Vitrine hébergée sur Cloudflare Pages (déploiement à chaque push) ; services du VPS déployés par GitHub Actions (SSH, clé dédiée). |
| 27 | 2026-10 | API : Spring Boot 4.1 / Java 21 / Gradle 9, Spring Modulith pour vérifier les frontières entre modules, routes versionnées `/v1/...`. |
| 28 | 2026-10 | Keycloak : thème de connexion « izimboa » (parent keycloak.v2), polices auto-hébergées, appliqué par `deploy.sh`. |
| 29 | 2026-10 | WhatsApp : API Meta en direct (remplace Brevo pour WhatsApp, cf. n° 21). Codes de connexion envoyés depuis un numéro neutre au nom de Data Engineering Consulting ; numéros des marques pour les messages métier. |
| 30 | 2026-10 | Tarif Delivery (base provisoire, à valider avec vendeurs et livreurs) : paliers de distance — 1 000 FCFA ≤ 3 km, 1 500 ≤ 7 km, 2 000 ≤ 12 km, au-delà sur devis ; option express +500 FCFA ; commission de 1,5 % sur l'argent encaissé à la livraison. Grille modifiable par l'administration, sans redéploiement. |
| 31 | 2026-10 | Pas de prix dynamique pour le vendeur : pluie et bouchons ne changent pas le prix. Izimboa verse une prime aux livreurs et allonge le délai annoncé. Argument : « chez nous, le prix ne bouge pas ». |
| 32 | 2026-10 | Données pluie et trafic collectées dès le premier jour (bouton livreur « il pleut ici », vitesses GPS, météo) pour entraîner plus tard un modèle qui affine délais et primes, jamais le prix vendeur. |
| 33 | 2026-10 | Cartes : OpenStreetMap hébergé chez nous (moteur d'itinéraires Valhalla ou OSRM + tuiles vectorielles servies par Caddy), mise à jour hebdomadaire ; le Cameroun tient sur le VPS actuel, serveur dédié aux cartes pour l'Afrique entière. Google Maps écarté (stockage interdit, calculs payants). Suivi du livreur en direct dans l'application mobile. |
| 34 | 2026-10 | Acteurs de Delivery : vendeur, particulier, destinataire (sans compte, suivi par lien WhatsApp + code de remise), livreur, opérateur, **partenaire de flotte**, administrateur. Nouveaux rôles Keycloak `operateur` et `partenaire`. |
| 35 | 2026-10 | **Partenaire de flotte** : propriétaire de quelques motos qui les met à disposition avec ses livreurs ; gère ses livreurs, ses motos (assurance, entretien, affectation), suit sa flotte en direct ; Izimboa lui verse les gains de sa flotte et il paie ses livreurs. |
| 36 | 2026-10 | Parcours d'une livraison : créée → attribuée → ramassée → en route → livrée (ou échouée → nouvelle tentative / retour, ou annulée). Frais retenus sur l'argent encaissé quand il suffit, sinon payés à la création ; retour d'un colis refusé facturé 50 % de la course ; annulation gratuite avant ramassage ; 2 passages maximum ; remise validée par un code à 4 chiffres (5 essais, puis déblocage par un opérateur). |
| 37 | 2026-10 | Distance provisoire = vol d'oiseau × 1,35, derrière une interface remplacée plus tard par notre moteur d'itinéraires OpenStreetMap. Zone desservie : rectangle autour de Douala (Bonabéri compris). |
| 38 | 2026-10 | **Application livreur en Kotlin natif** (Android Studio, Jetpack Compose, coroutines / Flow) : GPS en arrière-plan fiable sur Tecno / Infinix / Itel, alerte de course qui sonne comme un appel, légère sur téléphones 2 Go, hors ligne robuste. **Application client en Expo** (Android + iPhone). |
| 39 | 2026-10 | CI/CD Google Play : GitHub Actions construit, signe et publie sur la piste de **test interne** ; passage en production d'un clic. Compte Google Play de type organisation (déjà validé). |
| 40 | 2026-10 | Connexion par **code WhatsApp** prioritaire (Google et e-mail en options) ; sessions longues (≈ 90 jours sur le même téléphone) pour limiter le coût des codes. Tarif Meta Cameroun à vérifier. |
| 41 | 2026-10 | Bouton « il pleut ici » : **pas dans l'application livreur** au départ (la route API reste). Pluie et bouchons déduits du GPS et de la météo. |
| 42 | 2026-10 | Données GPS : position en direct dans **Redis** ; tracés dans PostgreSQL + **PostGIS** + **TimescaleDB** (un point toutes les 5 s en course : position, vitesse, direction, précision, heure). Enregistrement seulement quand le livreur est en ligne, et il en est informé. |
| 43 | 2026-10 | Moteur d'itinéraires **Valhalla** sur OpenStreetMap : trajets et recalage des tracés GPS sur les routes ; remplaçable (interface `RouteDistance`) ; ne stocke aucune de nos données. |
| 44 | 2026-10 | **Nos routes à nous** : rues découvertes par les tracés des livreurs, gardées dans une couche PostGIS séparée et réappliquées à chaque mise à jour de la carte (Geofabrik). Partage avec OpenStreetMap public : à décider plus tard. |
| 45 | 2026-10 | IA alimentée par PostGIS (tracés recalés, durées réelles par rue, heures, météo, adresses et repères) → durées rue par rue, primes, délais. **Jamais le prix vendeur.** |
| 46 | 2026-10 | Sauvegarde : la base PostgreSQL / PostGIS est le trésor ; la carte OpenStreetMap se retélécharge. |
| 47 | 2026-10 | **Flotte en sous-traitance** : pas d'achat de motos ; moto-taxis avec smartphone Android ; vérification (pièce d'identité, garant, formation, gilet). Recrutement au rythme des commandes. |
| 48 | 2026-10 | Argent encaissé : paiement Mobile Money direct du destinataire depuis le lien WhatsApp ; **plafond de liquide par livreur** (ex. 50 000 FCFA) au-delà duquel plus de courses avec encaissement ; **retenue automatique** sur les gains du livreur ; caution + garant. |
| 49 | 2026-10 | Vol de colis : **niveaux de confiance** (nouveau livreur = colis de faible valeur) ; photo au ramassage, code du destinataire, sachet scellé numéroté. |
| 50 | 2026-10 | **Stock confié au livreur** : version simple dans Delivery (quantités confiées, livrées, restantes, contrôle chaque soir). Gestion de stock complète dans le SaaS. |
| 51 | 2026-10 | **API publique pour les vendeurs** : clés d'accès par entreprise (Keycloak), webhooks à chaque étape, environnement de test, documentation sur developers.izimboa.com. |
| 52 | 2026-10 | Modules e-commerce : **WooCommerce** (WordPress), puis **PrestaShop**, puis **Shopify**. Magento écarté. |
| 53 | 2026-10 | SaaS développé en interne (personnalisé) ; Delivery est la première brique d'un vrai e-commerce. |
| 54 | 2026-10 | Référencement : seule la vitrine izimboa.com est référencée ; les applications métier (delivery., go., fly., buy.) sont bloquées pour les moteurs de recherche. |
| 55 | 2026-10 | Méthode : fonctionnalité par fonctionnalité, qualité professionnelle, produit testable au plus tôt. **À distance on code ; les configurations se font quand Gilles est devant sa machine.** |
| 56 | 2026-10 | Commercial : vendre **la livraison seule, tout de suite** (stratégie dans `10-strategie-commerciale.md`). |
| 57 | 2026-10 | Attribution automatique : livreur libre le plus proche du ramassage (en ligne, position < 2 min, sans colis en charge, rayon 8 km), express en priorité, 30 s pour accepter, sinon le suivant ; le livreur voit ce qu'il gagnera (65 % de la course) avant d'accepter. L'opérateur garde l'attribution manuelle. |
| 58 | 2026-10 | Suivi en direct : le vendeur voit le livreur dès l'attribution, le destinataire seulement quand le colis est en route vers lui. Au démarrage, positions dans PostgreSQL (dernière position + tracé) ; Redis, PostGIS et TimescaleDB arrivent au lot 5 derrière les mêmes routes d'API. L'application livreur interroge les propositions toutes les quelques secondes ; notification « qui sonne » (Firebase Cloud Messaging) avec l'application Kotlin. |
| 59 | 2026-10 | Espace vendeur web sur delivery.izimboa.com : fichiers statiques servis par Caddy sur le VPS, déployés par GitHub Actions ; envoi du lien de suivi et du code au client par un bouton WhatsApp (« cliquer pour discuter ») en attendant l'envoi automatique par l'API Meta. Français d'abord, anglais ensuite. |
| 60 | 2026-10 | Application livreur Android : Kotlin 2.4 + Jetpack Compose, minimum Android 8 ; connexion Keycloak (AppAuth, PKCE, session longue chiffrée par le Keystore) ; service de premier plan « En ligne » démarré depuis l'écran (pas de permission « localisation en arrière-plan ») ; course proposée en plein écran qui sonne ; une seule grande action par étape ; APK de test produit à chaque modification par GitHub Actions, publication Play automatique dès que les secrets existent. |
| 61 | 2026-10 | Courses proposées par **attente longue** (`offers/current?wait=`, 25 s max) : la radio du téléphone se réveille 2 à 3 fois par minute au lieu de 15. Firebase Cloud Messaging viendra en complément. |
| 62 | 2026-10 | Application livreur **hors connexion d'abord** : positions et actions (ramassé, parti, échec, nouvelle tentative, retour, hors ligne) écrites d'abord dans une base **SQLite chiffrée (SQLCipher, AES-256)**, clé aléatoire propre au téléphone rangée par le Keystore Android ; envoi dans l'ordre (positions, puis actions) dès le retour du réseau, même application fermée (WorkManager) ; chaque action garde son heure réelle (`occurredAt`, bornée côté serveur). Exceptions qui exigent le réseau : passer en ligne, accepter une course, **valider le code du client** (vérifié par le serveur contre la fraude). Déconnexion = base effacée ; sauvegarde et transfert Android désactivés. |
| 63 | 2026-10 | **GPS économe, proportionnel à la vitesse** : en attente de course, précision « quartier », un point par 50 m (au plus toutes les 30 s), position redonnée chaque minute sans rallumer le GPS si le livreur est immobile ; en course, GPS précis, un point par 15 m parcourus (beaucoup quand la moto roule vite, aucun à l'arrêt), points livrés par paquets ; envois regroupés toutes les 20 s en course, 60 s en attente. |
| 64 | 2026-10 | **Chemin parcouru** montré au destinataire (pendant que le colis est en route) et au vendeur, **depuis le ramassage seulement**. Pas de tracé d'itinéraire dans l'application livreur pour l'instant (Google Maps suffit) ; itinéraire calculé par notre moteur OpenStreetMap au lot 5. |
