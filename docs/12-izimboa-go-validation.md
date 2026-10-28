# Izimboa Go : liste de validation humaine

Contenus publiés : `web/vitrine/src/content/cm/go/data/*.json` (recherche du 28/10/2026 sur les sources officielles).
Chaque point ci-dessous doit être confirmé par une personne avant ou peu après la mise en ligne, puis à chaque changement de règles.
Règle : une information non vérifiable est retirée ou signalée « à confirmer », jamais inventée.

## 1. Juridique et entreprise (bloquant pour la confiance)

- [ ] **Mentions légales** (prochaine mise à jour) : raison sociale, forme, capital, siège, RCCM, NIU, directeur de la publication. Les mentions « à compléter » ont été retirées le 28/10/2026 ; l'hébergeur (Cloudflare) est indiqué.
- [ ] **Conseil réglementé au Canada** (décision du 28/10/2026 : pas de juriste pour la phase de test ; orientation gratuite uniquement pour le Canada, à revoir dès le premier client canadien payant) : au Canada, seuls les représentants autorisés (avocats, notaires du Québec, consultants du CICC) peuvent être rémunérés pour conseiller ou représenter sur une demande auprès d'IRCC (Loi sur l'immigration, art. 91 ; page IRCC « Représentants rémunérés »). Faire valider par un juriste l'offre payante d'Izimboa Go pour le Canada (partenariat avec un consultant réglementé ?). Le Québec a aussi ses règles pour les consultants. Le site dit déjà qu'Izimboa ne représente pas les candidats et oriente vers un représentant autorisé.
- [ ] **Conditions générales de vente** de l'accompagnement (aucune n'existe encore) et **grille de prix** : le site dit seulement « prix annoncé avant tout engagement ».
- [ ] **Engagements de service** affichés : rappel « sous un jour ouvré », premier échange gratuit, « sans engagement ». À confirmer ou modifier.
- [ ] **WhatsApp Business** : aucun numéro configuré (`WHATSAPP_NUMBER = null`), donc aucun bouton WhatsApp. À ajouter quand le numéro existe.
- [ ] **Témoignages** : aucun publié. N'en publier qu'avec l'accord écrit des clients et sans exagérer les résultats.

- [ ] **Consultant réglementé (CICC) pour le Canada** : recherche à partir du 29/10/2026 ; argument de négociation : la base de prospects qualifiés (`GET /v1/contact-requests`).

## 2. Réserves des programmes (notes des recherches, non publiées)

### Entrée express (Canada) (`ca-express-entry`, vérifié le 2026-10-28, statut : open)

Taux utilisé : 1 CAD ≈ 420,1 FCFA (XAF), cours du 28/10/2026 (fxfeed.io). Statut : OUVERT. Tirages « compétence linguistique en français » 2026 : 11 rondes, 55 500 invitations au total ; seuils CRS entre 382 (19 août) et 435 (16 octobre, dernier tirage francophone). Les chiffres des tirages viennent de sites spécialisés (immigration.ca, CIC News) car la page canada.ca des rondes n'a pas pu être chargée : à revérifier sur canada.ca avant publication. Frais RP au 30/04/2026 : traitement 990 CAD + droit de résidence permanente 600 CAD (CIC News + Canadim, concordants) – à confirmer sur la grille officielle IRCC. Fonds d'établissement : 15 263 CAD pour une personne seule = dernier barème confirmé (juillet 2025) ; IRCC met à jour chaque année, mise à jour 2026 non confirmée, donc chiffre volontairement non publié. VFS : seul centre au Cameroun = Yaoundé, nouvelle adresse après déménagement (sept. 2026) ; nouveaux frais de service VFS au 1er novembre 2026 (montants non vérifiés, non publiés).

### Permis d'études (Canada) (`ca-study-permit`, vérifié le 2026-10-28, statut : limited)

Taux : 1 CAD ≈ 420,1 FCFA (28/10/2026). Statut : OUVERT mais PLAFONNÉ (2026 : 408 000 permis au total, dont 180 000 soumis à la PAL/LAT ; 309 670 places de demandes PAL réparties entre provinces). Changement 2026 : les étudiants de maîtrise et doctorat dans un établissement public sont exemptés de PAL depuis le 1er janvier 2026. Preuve de fonds hors frais de scolarité : 23 448 CAD (1 personne) + 6 318 CAD par membre de famille, pour les demandes reçues à partir du 1er septembre 2026 (CIC News, SFU, PIE News concordants ; page canada.ca non chargée directement). Québec : 24 617 CAD (1 personne) selon barème du 1er janvier 2026 (via CIC News). Frais CAQ : tableau officiel 2026 « Étudiant étranger » 135 CAD (catégorie sélection temporaire). Depuis juillet 2026, les agents doivent vérifier l'origine des fonds dans tous les dossiers (CIC News). Délais de traitement depuis le Cameroun non vérifiés (outil dynamique IRCC).

### Mobilité francophone (Canada) (`ca-francophone-mobility`, vérifié le 2026-10-28, statut : open)

Taux : 1 CAD ≈ 420,1 FCFA (28/10/2026). Statut : OUVERT, aucun changement ni pause annoncé (page d'admissibilité IRCC modifiée le 20 avril 2026 : NCLC 5 à l'oral, tous les niveaux FEER sauf agriculture primaire FEER 4-5, hors Québec). Frais permis de travail 155 CAD (Canadim, grille au 30/04/2026) ; frais de conformité employeur 230 CAD (IRCC). Délais depuis le Cameroun non vérifiés. Je n'ai pas vérifié si un permis ouvert pour le conjoint est automatiquement accessible : non publié.

### Programmes des candidats des provinces (Canada) (`ca-pnp`, vérifié le 2026-10-28, statut : limited)

Taux : 1 CAD ≈ 420,1 FCFA (28/10/2026). Statut : OUVERT mais CHANGÉ. Ontario : le volet « Travailleurs qualifiés francophones » (Entrée express) est FERMÉ ; l'Ontario a fusionné ses 8 volets en un seul « Ontario Workforce Priority » (effectif 26 juin 2026) exigeant une offre d'emploi (sources : immigration.ca, moving2canada ; page ontario.ca non consultée directement – à confirmer). Nouveau-Brunswick : Initiative stratégique (Travailleurs francophones / Priorités francophones) active – dernier tirage 11 sept. 2026, 251 invitations ; NCLC 5 dans les 4 compétences, 65/100 points, frais de désignation 250 CAD (gnb.ca). La voie « Priorités francophones » pour les candidats à l'étranger passe par une lettre d'intérêt d'Immigration NB. Je n'ai pas vérifié d'autres provinces (Manitoba, Nouvelle-Écosse, Î.-P.-É.) pour 2026. Allocations PNP 2026 au niveau fédéral non vérifiées.

### Immigrer au Québec (PSTQ) (`ca-quebec`, vérifié le 2026-10-28, statut : limited)

Taux : 1 CAD ≈ 420,1 FCFA (28/10/2026). Statut : OUVERT mais TRÈS RESTREINT pour les candidats à l'étranger. Plan 2026 : ~45 000 admissions/an, dont 28 275 travailleurs qualifiés ; priorité aux personnes déjà au Québec ; PEQ fermé le 19 nov. 2025 ; PSTQ = seule voie de sélection des travailleurs qualifiés. Rondes 2026 : volets 1 et 2 (et la plupart du volet 3) réservés à des personnes « séjournant au Québec » ; quelques invitations au volet 3 (professions réglementées) et au volet 4 (talents d'exception, 21 en août, 7 en septembre) – des candidats hors Québec ont été invités selon pvtistes.net, chiffres non officiels. Dernière ronde : 24 sept. 2026. Frais Québec 2026 : demande de sélection permanente 940 CAD (requérant principal) + 201 CAD par membre de la famille. Montants du contrat d'autonomie financière non vérifiés (PDF séparé). Gratuité de la déclaration Arrima non vérifiée : non publiée.

### Études en Allemagne (visa étudiant) (`de-study`, vérifié le 2026-10-28, statut : open)

Compte bloqué : 11 904 EUR/an (992 EUR/mois) confirmé sur Make it in Germany (mention « 2025 ») et repris comme montant 2026 inchangé par les prestataires (Expatrio, page du 07/10/2026). La réforme du BAföG prévue pour le semestre d'hiver 2026/27 (forfait logement 380 -> 440 EUR) pourrait relever ce montant : à revérifier avant publication et à chaque rentrée. APS : exigé seulement pour Chine, Vietnam, Inde (et non pour le Cameroun) d'après les sources consultées. Frais de visa : l'ambassade de Yaoundé affiche 50 000 XAF (adulte) / 25 000 XAF (mineur), gratuit pour boursiers de fonds publics (équivalent des 75 EUR). Délai de traitement après dépôt non publié pour Yaoundé. Règles d'accès Bac/GCE (Studienkolleg vs accès direct) à vérifier au cas par cas dans la base d'admission DAAD/anabin ; formulation volontairement prudente. Le recours gracieux (remonstration) contre un refus a été supprimé le 01/07/2025 (FAQ ambassade).

### Ausbildung (formation professionnelle en Allemagne) (`de-ausbildung`, vérifié le 2026-10-28, statut : open)

Langue : Make it in Germany (2026) indique B1 en règle générale, sauf si l'entreprise a déjà évalué vos compétences ; la page de l'ambassade de Yaoundé indique au minimum A2 (B1 pour le secteur de la santé), uniquement certificats ALTE (Goethe, ÖSD, telc, TestDaF). Ressources : Make it in Germany 2026 = rémunération d'au moins 822 EUR net (1 048 EUR brut) pour une formation en entreprise, ou 959 EUR net/mois pour une formation scolaire ; la page de l'ambassade de Yaoundé affiche encore 853 EUR/mois (page probablement plus ancienne) : à faire confirmer. Rémunération minimale légale 2026 (contrats débutant en 2026) : 724 EUR la 1re année (BIBB/presse spécialisée) ; 2027 : 764 / 902 / 1 031 / 1 070 EUR. Frais de visa : 50 000 XAF affichés par l'ambassade pour le visa national (page étudiant), équivalent 75 EUR. Reconnaissance des certificats scolaires : non exigée formellement selon les sources consultées, mais l'entreprise/l'école peut l'exiger ; formulation prudente.

### Travailler en Allemagne (Chancenkarte et Carte bleue européenne) (`de-skilled-work`, vérifié le 2026-10-28, statut : open)

Seuils Carte bleue 2026 : 50 700 EUR brut/an (4 225 EUR/mois) et 45 934,20 EUR (3 827,85 EUR/mois) pour métiers en pénurie, jeunes diplômés (diplôme obtenu il y a moins de 3 ans) et informaticiens sans diplôme ; vérifiés via une notice de mission allemande (janvier 2026) et la presse citant digital.diplo.de. Je n'ai pas pu charger directement make-it-in-germany.com ni digital.diplo.de (non accessibles depuis l'outil). Chancenkarte : 1 091 EUR/mois (13 092 EUR pour 12 mois) en 2026, repris de sources secondaires qui citent Make it in Germany (ancien montant 1 027 EUR) : à revérifier sur make-it-in-germany.com avant publication. Grille de points tirée de sources secondaires concordantes (fondée sur § 20a AufenthG). L'ambassade de Yaoundé affiche encore une page « visa de recherche d'emploi » avec 853 EUR/mois (ancien dispositif). Frais de visa : 50 000 XAF (barème national affiché sur la page étudiant de l'ambassade, équivalent 75 EUR). Délais d'attente Yaoundé : Chancenkarte > 1 an ; emploi qualifié jusqu'à 1 mois ; procédure § 81a jusqu'à 4 semaines.

### Études en France (procédure Campus France) (`fr-study`, vérifié le 2026-10-28, statut : open)

IMPORTANT : le montant de ressources n'est plus 615 €/mois. Service-Public (vérifié le 01/08/2026) indique 877,5 €/mois et la liste visa Campus France Cameroun (sept. 2026) indique 877 €/mois ; on affiche 877,5 €. Taxe de validation VLS-TS étudiant : 100 € selon Service-Public F2231 (un article de presse évoquait 150 € ; non retenu car non confirmé par une source officielle). Frais de service TLScontact pour le dépôt visa : 41 € selon la page France-Visas Cameroun (texte daté de 2023, à revérifier sur le site TLScontact). Frais Campus France : 85 000 FCFA + 16 000 FCFA de frais TLS, payés chez TLScontact après autorisation de paiement (72 h). La page Campus France 'Demander son visa' décrit encore l'ancienne validation OFII par courrier ; aujourd'hui la validation se fait en ligne (ANEF) selon Service-Public. Dates du calendrier préconsulaire Parcoursup/CampusArt sur la page Campus France contiennent des coquilles d'année (2026 au lieu de 2027) : non reprises.

### Travailler en France (carte talent et visa salarié) (`fr-talent-work`, vérifié le 2026-10-28, statut : open)

Nom officiel : depuis la loi du 26 janvier 2024, le « passeport talent » est devenu la « carte de séjour pluriannuelle talent » (Service-Public parle de « carte talent »). Seuils 2026 vérifiés sur Service-Public : talent-salarié qualifié 39 582 € brut/an ; talent-professions médicales 41 386,48 € ; talent-carte bleue européenne 59 373 €. Coût de la carte talent : 350 € (50 € timbre + 300 € taxe). Le montant de la taxe de validation du VLS-TS « salarié » / « travailleur temporaire » n'a pas été confirmé sur une source officielle : non chiffré. Délai d'instruction de l'autorisation de travail non publié officiellement : non chiffré. Frais de service TLScontact 41 € selon page France-Visas Cameroun (texte de 2023), à revérifier.

### Études en Belgique (visa D étudiant) (`be-study`, vérifié le 2026-10-28, statut : open)

Pas de « Campus Belgique » officiel pour le Cameroun en 2026. L'ancienne étape Viabel (entretien à l'Institut français, documents 2025) n'apparaît plus sur la page de l'Ambassade mise à jour le 19/06/2026 : procédure actuelle = Visa On Web + TLScontact Yaoundé + questionnaire/entretien éventuel à l'ambassade + authentification des diplômes par GCE Board (anglophones, avant dépôt) ou OBC (francophones, après dépôt). Seul le centre TLScontact de Yaoundé accepte les visas étudiants (TLScontact Belgique a aussi un centre à Douala pour d'autres services, ex. légalisation). Frais de service TLScontact Belgique non vérifiés : non chiffrés. Délai de décision du visa étudiant non chiffré (pas de source officielle consultée). Montant du garant : 3 235,88 €/mois, puis 3 279,47 €/mois à partir du 01/09/2026 (on affiche ce dernier). Le site FWB précise que les titulaires du Baccalauréat camerounais sont soumis à un processus de sélection pour l'équivalence. Date limite d'admission pour étudiants hors UE : fixée par chaque établissement, non chiffrée.

## 3. Pages villes (Bafoussam, Bamenda, Garoua, Maroua, Ngaoundéré, Bertoua, Kribi, Buea, Limbé, Ebolowa)

- [ ] Durées de trajet : estimations (distances Rome2rio) ; Ebolowa–Douala sans source (« environ une demi-journée »).
- [ ] Vols Camair-Co (Garoua, Maroua, Ngaoundéré) : fréquences issues de sources 2023-2024, signalées comme variables.
- [ ] Point de dépôt TLScontact (France) à Garoua : annoncé en 2023, non confirmé aujourd'hui ; les pages renvoient vers france-visas.gouv.fr.
- [ ] Lieux de légalisation et de traduction par ville : non trouvés, donc non publiés.

## 4. Révision régulière

- Revoir chaque programme au moins tous les 3 mois, et immédiatement en cas d'annonce officielle (frais, preuve de fonds, quotas).
- Mettre à jour le champ `updated` du programme à chaque vérification : il s'affiche sur la page.
