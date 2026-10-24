# Izimboa Delivery : marché et stratégie

## Concurrence à Douala (benchmark rapide)
- **Yango Delivery** : le poids lourd. Moto ou voiture, jusqu'à 20 kg, souvent < 1 h, prix très bas, offre entreprises, énorme base d'utilisateurs. Faiblesses rapportées : manque de professionnalisme, chauffeurs peu fiables, plateforme générale et non partenaire des vendeurs.
- **ColiXpress** : cible les e-commerçants, suivi GPS, notifications, prix selon la distance. Douala, Yaoundé (Bafoussam, Kribi, Buea annoncés). Pas de tarifs publics ni de preuve de gestion du paiement à la livraison.
- Autres : Go (transport/courses/colis), Wakavite, Safari Quickly, EasyMove (via WhatsApp).
- À faire : tester soi-même les applications concurrentes avec de vraies commandes.

**Conclusion :** ne pas se battre sur le prix contre Yango, mais être le meilleur sur une niche.

## Positionnement
> « Vos colis livrés, votre argent reversé le jour même. »

Izimboa ne vend pas de la livraison, mais de la tranquillité aux vendeurs. Trois piliers :
1. **Fiabilité** : zéro colis perdu, preuve de livraison (code ou photo).
2. **Paiement à la livraison bien géré** : le livreur encaisse, Izimboa reverse au vendeur le jour même.
3. **Proximité** : service client local joignable sur WhatsApp.

Pistes de différenciation : adressage fiable (épingle sur la carte + repère du type « derrière la pharmacie »), création d'une livraison en un clic pour les vendeurs WhatsApp.

## Flotte de livreurs
- Pas d'achat de motos au départ : recrutement de moto-taxis indépendants équipés, formés, avec gilet Izimboa.
- Démarrer petit : 10 à 15 livreurs fiables.
- Confiance (ils encaissent de l'argent) : pièce d'identité + garant, caution ou plafond de liquide par livreur, reversement le soir même via Mobile Money.
- **Rémunération validée** : modèle mixte — petit fixe journalier (présence aux heures de pointe) + commission de 60 à 70 % du prix de la course. Passage à la commission seule quand le volume le permettra.

## Les trois interfaces
1. Application client : commande, prix immédiat, paiement, suivi en direct.
2. Application livreur : réception des courses, ramassage, remise validée par code ou photo.
3. Tableau de bord d'administration : livreurs, tarifs, litiges.

## Les acteurs
| Acteur | Rôle Keycloak | Ce qu'il fait |
|---|---|---|
| Vendeur (expéditeur) | `vendeur` | Crée les livraisons, paie, reçoit l'argent encaissé le jour même. |
| Particulier | `client` | Envoie un colis de temps en temps. |
| Destinataire | — (sans compte) | Suit le colis par un lien WhatsApp, paie si besoin, donne le code de remise. |
| Livreur | `livreur` | Accepte les courses, ramasse, livre, encaisse, prend la preuve de remise. |
| Partenaire de flotte | `partenaire` | Possède des motos et ses livreurs ; les gère, suit sa flotte, reçoit les gains de sa flotte et paie ses livreurs. |
| Opérateur | `operateur` | Suit les courses en direct, réassigne, répond au support WhatsApp. |
| Administrateur | `admin` | Tarifs, livreurs, primes, reversements, comptabilité. |

Systèmes externes : Mobile Money, WhatsApp (API Meta), cartes OpenStreetMap, Keycloak, météo.

## Tarifs (base provisoire)
- **Prix au palier de distance**, connu d'avance et stable : 1 000 FCFA jusqu'à 3 km, 1 500 FCFA jusqu'à 7 km, 2 000 FCFA jusqu'à 12 km, au-delà sur devis.
- **Express** : +500 FCFA, prise en charge prioritaire.
- **Paiement à la livraison** : commission de 1,5 % du montant encaissé (risque + reversement le jour même).
- **Pluie, bouchons** : le prix ne bouge pas. Izimboa paie une prime aux livreurs et le délai annoncé s'allonge.
- Grille stockée en base et modifiable par l'administration. Chiffres à valider avec quelques vendeurs et livreurs.

## Cartes et suivi
- OpenStreetMap hébergé chez nous : itinéraires et distances (Valhalla ou OSRM) et affichage de la carte, sans coût par requête.
- Le client suit le livreur en direct dans l'application, avec l'heure d'arrivée.
- Nos propres données (adresses épinglées, repères, traces GPS) enrichissent la carte au fil du temps.
