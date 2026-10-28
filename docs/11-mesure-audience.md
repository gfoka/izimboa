# Mesure d'audience (Google Analytics 4)

- Chargée **uniquement après accord** du visiteur (bandeau, `components/Analytics.tsx`), mode consentement Google, aucune publicité.
- Identifiant : `GA_MEASUREMENT_ID` dans `web/vitrine/src/config/site.ts` (null = rien n'est chargé ni affiché).
- Envoi des événements : `web/vitrine/src/lib/track.ts`. **Jamais de donnée personnelle** (ni nom, ni téléphone, ni e-mail, ni ville).

| Événement | Quand | Paramètres |
|---|---|---|
| `page_view` | Automatique (GA4) | – |
| `cta_click` | Clic sur un bouton principal (premier écran, « Évaluer gratuitement mon profil ») | `cta`, `page` |
| `eligibility_start` | Première réponse au test | `goal` |
| `eligibility_step` | Chaque réponse | `step`, `question` |
| `eligibility_complete` | Affichage du résultat | `goal`, `results` (nombre de pistes) |
| `eligibility_result_click` | Clic sur une piste proposée | `program` |
| `lead_submit` | Demande de rappel envoyée avec succès | `product`, `kind` |
| `lead_error` | Échec d'envoi | `product`, `status` |

Conversions à déclarer dans GA4 : `lead_submit` (principale), `eligibility_complete` (secondaire).
Entonnoir à suivre : page Go → `cta_click` → `eligibility_start` → `eligibility_complete` → `lead_submit`.
Les demandes elles-mêmes sont en base (`GET /v1/contact-requests`), ce qui permet de recouper les chiffres.
