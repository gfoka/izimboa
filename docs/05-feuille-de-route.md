# Feuille de route (agile)

On livre et on déploie au fur et à mesure.

1. ✅ **Fondations** : structure du monorepo et documentation des décisions.
2. 🟡 **Identité visuelle** (direction retenue, à affiner par un graphiste) : logo, couleurs, typographie Izimboa.
3. ✅ **Vitrine izimboa.com** en ligne (Cloudflare Pages) : pages et textes, développement Next.js, mise en ligne. *Première livraison.*
4. 🟡 **Infra de base** : environnement local Docker, VPS sécurisé + HTTPS, CI/CD, squelette de l'API (`backend/`, tests verts en CI) — reste : déployer API et Keycloak sur le VPS : VPS, Docker Compose, Keycloak (Google, WhatsApp, SMS), API Spring Boot vide.
5. ⬜ **Izimboa Delivery** (MVP Douala) : app client, app livreur, administration, paiement Mobile Money, paiement à la livraison.
6. ⬜ **Izimboa Go** : moteur de procédures paramétrables.
7. ⬜ **Izimboa Fly** : site de billetterie en partenariat IATA.
8. ⬜ **Izimboa Buy** : portail web d'achat à l'international.
9. ⬜ **SaaS de gestion commerciale** (nom à définir) : après Delivery, sur lequel il s'appuie.
10. ⬜ Migration progressive vers AWS.

Délai visé pour une première version visible : 2 à 3 mois.
