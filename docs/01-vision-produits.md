# Vision et produits

Izimboa est une marque parapluie autour du **mouvement** : des colis comme des personnes.
Une seule marque porte tous les produits, pour que la confiance gagnée sur l'un profite aux autres.
Un compte Izimboa unique donne accès à tous les produits.

Force de la plateforme : chaque nouveau produit réutilise le même backend, la connexion centrale, les paiements et les notifications. Lancer un produit devient rapide, et on le teste sur le marché avant d'investir davantage.

## Les produits

### Izimboa Delivery — livraison de colis
- Le client indique un point de départ et une destination, reçoit un prix immédiat, paie (Mobile Money : Orange Money, MTN) et suit son colis en direct.
- Livreurs à moto sous contrat avec Izimboa.
- Cible prioritaire : les vendeurs en ligne de Douala (WhatsApp, Facebook).
- Application mobile + site `delivery.izimboa.com`.

### Izimboa Go — accompagnement d'immigration
- Exemple : une personne au Cameroun qui veut partir au Canada.
- Chaque procédure est découpée en **étapes**. À chaque étape : paiement en ligne, dépôt des pièces, notifications, suivi.
- **Entièrement paramétrable** : on peut créer n'importe quelle procédure pour n'importe quel pays sans recoder. Modèle duplicable.
- Objectif : rassurer le client grâce à un très bon suivi de tout le parcours.
- Nom retenu « Go » plutôt que « Travel » (trop touristique) ou « Visa » (laisse croire qu'on délivre les visas). Le mot « visa » est utilisé pour le référencement (sous-titre des stores, pages « visa Canada depuis le Cameroun »).
- Application mobile + site `go.izimboa.com`.

### Izimboa Fly — billets d'avion (plus tard)
- Débouché naturel de Go : les clients en immigration ont besoin d'un billet à la fin de leur procédure.
- Démarrage en partenariat avec une agence agréée IATA (commission par billet), sans agrément propre au départ.
- Un site web suffit au départ : `fly.izimboa.com`. Application seulement si la demande le justifie.

### Izimboa Buy — achat à l'international pour le client
- Besoin : beaucoup de personnes au Cameroun veulent des produits disponibles en France ou ailleurs (livres, produits divers, etc.).
- Le client trouve son produit (ex. en collant un lien), reçoit un **devis tout compris** (prix, transport, douane), paie, et **Izimboa achète pour lui** puis le fait livrer jusqu'à sa porte.
- Nom « Buy » : l'action d'acheter pour la personne. « Shop » écarté (évoque une boutique qui possède déjà les produits), « Import » écarté (évoque l'import-export).
- L'envoi d'un produit **déjà acheté** par le client relève plutôt du transfert de colis : future option internationale d'Izimboa Delivery, pas de Buy.
- **Portail web seulement** au départ, très bien référencé (ex. « acheter en France livré au Cameroun ») ; éventuellement installable (PWA), sans application mobile.
- Point d'attention : les **médicaments** sont très réglementés à l'import ; vérifier ce qui est autorisé.
- Adresse : `buy.izimboa.com`.

## Projet séparé : le SaaS de gestion commerciale
- Suite complète de gestion commerciale (achats, ventes, stock, facturation, boutique en ligne, WhatsApp, centre d'appels) pour restaurants, supermarchés, commerces…
- Détails : [06-saas-gestion-commerciale.md](06-saas-gestion-commerciale.md). Nom à définir.
- **Marque séparée**, distincte d'Izimboa (métier différent), mais **connectée** : la livraison des commandes part automatiquement chez Izimboa Delivery.
- **Même code et même plateforme** : nouveau module du backend dans ce monorepo.
- Condition : Izimboa Delivery doit être solide en premier.

## Domaines
- `izimboa.com` : vitrine du groupe (pages `/livraison`, `/go`, `/fly` pour le référencement)
- Sous-domaines pour les applications : `delivery.`, `go.`, `fly.`, `buy.`, `admin.`, `api.`, `auth.`
- Pas de nom de domaine différent par produit (dispersion de la marque).
