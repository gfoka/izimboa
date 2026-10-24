# web/delivery — espace vendeur Izimboa Delivery

Application web du vendeur sur **delivery.izimboa.com** : connexion Izimboa (Keycloak), simulation de prix,
création d'une livraison (épingles sur la carte + repères), suivi des livraisons et de l'argent, envoi du lien
de suivi et du code de remise au client sur WhatsApp. Page publique de suivi du destinataire : `/suivi/<jeton>/`.

| | |
|---|---|
| Technique | Next.js 16 (export statique), React 19, TypeScript |
| Connexion | `oidc-client-ts`, flux code + PKCE, client Keycloak `izimboa-web` |
| Carte | Leaflet + tuiles OpenStreetMap (nos propres tuiles au lot 5) |
| Hébergement | fichiers statiques servis par Caddy sur le VPS (`/srv/izimboa/apps/delivery`) |
| Déploiement | `.github/workflows/deploy-delivery-web.yml` à chaque push sur `web/delivery/**` |
| Référencement | aucun (`robots.txt` + `X-Robots-Tag: noindex`) : seule la vitrine est référencée |

## En local

```bash
cp .env.example .env.local     # API et Keycloak locaux (infra/local)
npm install
npm run dev                    # http://localhost:3001
```

Pages : `/` (mes livraisons), `/nouvelle/`, `/livraison/?id=…`, `/callback/` (retour de connexion), `/suivi/<jeton>/`.
