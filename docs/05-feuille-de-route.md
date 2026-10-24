# Feuille de route (agile)

On livre et on déploie au fur et à mesure.

1. ✅ **Fondations** : structure du monorepo et documentation des décisions.
2. 🟡 **Identité visuelle** (direction retenue, à affiner par un graphiste) : logo, couleurs, typographie Izimboa.
3. ✅ **Vitrine izimboa.com** en ligne (Cloudflare Pages) : pages et textes, développement Next.js, mise en ligne. *Première livraison.*
4. ✅ **Infra de base** : environnement local Docker, VPS sécurisé + HTTPS, CI/CD, API et Keycloak en production (thème Izimboa). Reste à part : sauvegardes de la base, connexions Google / WhatsApp.
5. 🟡 **Izimboa Delivery** (MVP Douala) :
   - ✅ Lot 1 — cœur de l'API : grille de prix modifiable, simulation de prix, création de livraison, étapes, code de remise, suivi public, relevés pluie / trafic.
   - ⬜ Lot 2 — WhatsApp (lien de suivi + code au destinataire), attribution automatique au livreur le plus proche, position GPS en direct.
   - ⬜ Lot 3 — applications : vendeur (web), livreur (mobile), opérateur ; partenaires de flotte.
   - ⬜ Lot 4 — argent : Mobile Money, reversements du soir, grand livre, primes pluie.
   - ⬜ Lot 5 — cartes OpenStreetMap hébergées (itinéraires et affichage).
6. ⬜ **Izimboa Go** : moteur de procédures paramétrables.
7. ⬜ **Izimboa Fly** : site de billetterie en partenariat IATA.
8. ⬜ **Izimboa Buy** : portail web d'achat à l'international.
9. ⬜ **SaaS de gestion commerciale** (nom à définir) : après Delivery, sur lequel il s'appuie.
10. ⬜ Migration progressive vers AWS.

Délai visé pour une première version visible : 2 à 3 mois.
