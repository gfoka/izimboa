# Projet SaaS de gestion commerciale (nom à définir)

> Statut : **en clarification**. Le nom n'est pas arrêté ; le projet sera lancé après Izimboa Delivery, sur lequel il s'appuie.

## L'idée
Une **suite complète de gestion commerciale** en SaaS, sur le modèle d'Odoo ou de Zoho, pour accompagner les entreprises dans toute leur activité commerciale : restaurants, supermarchés, commerces, prestataires de services…

Parcours d'un client :
1. L'entreprise s'inscrit et **crée son entreprise** sur la plateforme.
2. Elle **paramètre ses produits et services** (et leurs variantes, types de colis, etc.).
3. Elle dispose d'une **boutique en ligne** prête à l'emploi, accessible sur un sous-domaine ou sur **son propre nom de domaine**.
4. Elle vend via **nos moyens de paiement** ; la **livraison est assurée automatiquement par Izimboa Delivery**.

## Modules envisagés
- **Achats, ventes, stock** : le cœur du produit, qui doit être très pointu.
- **Facturation**
- **Boutique en ligne** (domaines personnalisés)
- **Messagerie WhatsApp** avec les clients
- **Centre d'appels / service client**
- **Référencement** des boutiques

La gestion et l'opérationnel (logistique) réunis, c'est justement ce qui fait la valeur de l'offre.

## Marque
- **Marque séparée d'Izimboa**, sans « Izimboa » devant : un commerçant doit voir un logiciel de gestion professionnel, pas une entreprise de livraison. Cela laisse aussi la liberté de le vendre séparément ou de le brancher sur d'autres livreurs plus tard.
- Un seul nom pour la suite, avec des noms de modules simples à l'intérieur (Stock, Ventes, Boutique…).
- Mention possible : « Livraison par Izimboa ».

### Pistes de noms étudiées (aucune retenue pour l'instant)
| Nom | .com | Remarque |
|---|---|---|
| Gesco | pris | « gestion commerciale » ; getgesco.com libre |
| Bizna | pris | « business » ; getbizna.com libre |
| Gescora | libre (à la vérification) | sonne pro, FR/EN |
| Gescoro | libre (à la vérification) | |
| Bizniko | libre (à la vérification) | plus ludique |
| Gestiko | premium (payant) | |
| Boutika, Vendo, Stocka, Marketa, Kontrol | non vérifiés | jugés trop réducteurs pour une suite complète |

Avant de choisir : vérifier qu'aucune entreprise n'utilise déjà le nom (marques déposées).

## Technique
- **Même monorepo, même backend, même serveur** : le SaaS est un **nouveau module** du monolithe modulaire, avec **son propre schéma** PostgreSQL. Il réutilise la connexion, les paiements, les notifications (Brevo) et l'infrastructure.
- Frontières strictes entre modules dès le premier jour → extraction possible plus tard en microservice, voire dans un dépôt séparé, si le produit décolle.
- **Multi-entreprises** (multi-tenant) : chaque entreprise cliente a ses données isolées.
- **Connexion** : même serveur Keycloak, mais dans un **realm séparé** pour que la marque du SaaS ait sa propre page de connexion (ex. `auth.<nomdusaas>.com`), sans afficher Izimboa. OpenID Connect fonctionne quel que soit le domaine (redirections).
- **Domaines personnalisés** des boutiques : chaque domaine est déclaré comme adresse de retour autorisée ; certificats TLS automatiques.
- **Intégration Delivery** : une commande validée crée automatiquement une livraison Izimboa Delivery (via le service Delivery, jamais ses tables).

## Ordre de lancement
Après Izimboa Delivery : c'est le plus gros des produits, et sa valeur dépend d'une livraison qui fonctionne bien.
