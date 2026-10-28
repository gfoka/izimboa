import type { Lang } from "@/config/site";
import type { CostKind, Destination, Goal } from "@/content/go-types";

/** Textes de l'interface d'Izimboa Go (Cameroun). Les contenus des programmes et des villes sont dans data/. */
export type GoUi = {
  destinations: Record<Destination, string>;
  goals: Record<Goal, string>;
  hub: {
    title: string;
    lead: string;
    all: string;
    filterLabel: string;
    compareTitle: string;
    compareText: string;
    compareLink: string;
    citiesTitle: string;
    citiesLead: string;
    readMore: string;
  };
  program: {
    forWho: string;
    conditions: string;
    steps: string;
    costs: string;
    costLabel: string;
    costAmount: string;
    costFcfa: string;
    costsNote: string;
    costNature: string;
    costKinds: Record<CostKind, string>;
    uncertain: string;
    delays: string;
    biometrics: string;
    faq: string;
    sources: string;
    updated: string;
    disclaimer: string;
    related: string;
    paused: string;
    limited: string;
    hubLink: string;
  };
  trust: {
    title: string;
    lead: string;
    items: { title: string; text: string }[];
    notAffiliated: string;
    legalLink: string;
  };
  city: {
    travel: string;
    toYaounde: string;
    toDouala: string;
    local: string;
    universities: string;
    programs: string;
    others: string;
    faq: string;
  };
  test: {
    title: string;
    lead: string;
    cta: string;
    q: {
      goal: string;
      education: string;
      age: string;
      language: string;
      budget: string;
      experience: string;
      destination: string;
    };
    goalOptions: { value: Goal; label: string }[];
    educationOptions: { value: string; label: string }[];
    ageOptions: { value: string; label: string }[];
    languageOptions: { value: string; label: string }[];
    budgetOptions: { value: string; label: string }[];
    experienceOptions: { value: string; label: string }[];
    destinationOptions: { value: string; label: string }[];
    profileTitle: string;
    keyConditions: string;
    officialSource: string;
    seeProgram: string;
    restricted: string;
    warningsTitle: string;
    warnings: Record<string, string>;
    phoneError: string;
    next: string;
    back: string;
    step: string;
    resultTitle: string;
    resultLead: string;
    noResult: string;
    contactTitle: string;
    contactLead: string;
    name: string;
    phone: string;
    phoneHint: string;
    city: string;
    email: string;
    optional: string;
    submit: string;
    sending: string;
    success: string;
    error: string;
    consent: string;
    privacyLink: string;
    restart: string;
    disclaimer: string;
  };
  compare: {
    slug: string;
    meta: { title: string; description: string };
    h1: string;
    intro: string;
    tableCaption: string;
    headers: { destination: string; language: string; study: string; work: string; permanent: string; budget: string };
    rows: Record<Destination, { language: string; study: string; work: string; permanent: string; budget: string }>;
    adviceTitle: string;
    advice: { title: string; text: string }[];
    faq: { q: string; a: string }[];
  };
  /** Villes proposées dans le formulaire (toutes les villes, pas seulement celles qui ont une page). */
  cityOptions: string[];
};

const cityOptions = [
  "Douala", "Yaoundé", "Bafoussam", "Bamenda", "Garoua", "Maroua", "Ngaoundéré", "Bertoua", "Kribi", "Buea",
  "Limbé", "Ebolowa", "Dschang", "Kumba", "Nkongsamba", "Edéa", "Foumban", "Mbouda", "Sangmélima", "Kousseri",
];

export const GO_UI: Record<Lang, GoUi> = {
  fr: {
    destinations: { ca: "Canada", de: "Allemagne", fr: "France", be: "Belgique" },
    goals: { study: "Études", work: "Travail", permanent: "Résidence permanente", training: "Formation en alternance" },
    hub: {
      title: "Trouvez la bonne voie, pas seulement une destination",
      lead: "Canada, Allemagne, France, Belgique : chaque programme a ses conditions, ses délais et son budget. Nous les avons vérifiés sur les sites officiels et expliqués depuis le Cameroun, en FCFA.",
      all: "Tous",
      filterLabel: "Filtrer par objectif",
      compareTitle: "Vous hésitez entre plusieurs pays ?",
      compareText: "Langue, coût des études, possibilités de travail, résidence permanente : comparez les destinations côte à côte.",
      compareLink: "Comparer les pays",
      citiesTitle: "Partout au Cameroun",
      citiesLead: "Une grande partie de la préparation se fait à distance. Où que vous soyez, nous vous aidons à préparer votre dossier et à organiser les déplacements nécessaires (rendez-vous au centre de visa ou à l'ambassade, biométrie, parfois un examen de langue).",
      readMore: "Voir le programme",
    },
    program: {
      forWho: "Pour qui ?",
      conditions: "Conditions principales",
      steps: "Les étapes",
      costs: "Frais officiels",
      costLabel: "Frais",
      costAmount: "Montant",
      costFcfa: "En FCFA",
      costsNote: "Montants publiés à la date de vérification ; la conversion en FCFA est indicative. Ce tableau ne représente pas le coût total du projet (billet d'avion, logement, assurance, traductions, examens de langue…). Les frais officiels sont payés directement aux autorités ou à leurs prestataires ; les éventuels frais d'accompagnement d'Izimboa sont distincts et annoncés avant tout engagement.",
      costNature: "Nature",
      costKinds: { official: "Frais officiel", provider: "Frais de prestataire", employer: "Payé par l'employeur", requirement: "Condition de ressources (pas un frais)" },
      uncertain: "à confirmer",
      delays: "Délais",
      biometrics: "Rendez-vous et empreintes au Cameroun",
      faq: "Questions fréquentes",
      sources: "Sources officielles",
      updated: "Informations vérifiées le",
      disclaimer: "Izimboa n'est pas une autorité de délivrance de visas et ne garantit aucun résultat. Seules les autorités du pays de destination décident. Les règles changent souvent : vérifiez toujours sur les sources officielles ci-dessous.",
      related: "Autres voies pour le même objectif",
      paused: "Programme suspendu ou fermé actuellement",
      limited: "Accès restreint ou règles récemment modifiées : lisez les conditions et la date de vérification",
      hubLink: "Tous les programmes",
    },
    trust: {
      title: "Ce que fait Izimboa Go, et ce qu'il ne fait pas",
      lead: "Pour choisir en confiance, voici exactement ce que nous proposons, et ses limites.",
      items: [
        { title: "Information générale", text: "Des fiches sur les programmes, rédigées à partir des sites officiels, avec leurs sources et leur date de vérification. Gratuit." },
        { title: "Orientation", text: "Le test d'éligibilité et l'échange avec un conseiller vous indiquent des pistes à étudier. C'est une orientation indicative, pas une décision d'éligibilité : seules les autorités et les établissements décident." },
        { title: "Assistance administrative", text: "Liste des pièces, organisation du dossier, calendrier, préparation des rendez-vous. Vous déposez votre demande et payez les frais officiels directement aux autorités ou à leurs prestataires." },
        { title: "Conseil réglementé : le cas du Canada", text: "Pour une demande au Canada (y compris au Québec), seuls des représentants autorisés (avocats, notaires du Québec, consultants réglementés par le CICC) peuvent être rémunérés pour conseiller ou représenter un candidat. Izimboa Go ne représente pas les candidats auprès des autorités ; pour un conseil personnalisé rémunéré sur une demande canadienne, nous vous orientons vers un représentant autorisé." },
      ],
      notAffiliated: "Izimboa Go n'est ni un organisme gouvernemental, ni une ambassade, ni un centre de visa (VFS Global, TLScontact), ni Campus France, et n'a aucun lien officiel avec eux.",
      legalLink: "Mentions légales",
    },
    city: {
      travel: "Se rendre au rendez-vous de visa",
      toYaounde: "Vers Yaoundé",
      toDouala: "Vers Douala",
      local: "Bon à savoir sur place",
      universities: "Établissements d'enseignement supérieur",
      programs: "Les programmes les plus demandés",
      others: "Nous accompagnons aussi",
      faq: "Questions fréquentes",
    },
    test: {
      title: "Évaluez gratuitement votre profil",
      cta: "Évaluer gratuitement mon profil",
      lead: "Six questions environ, deux minutes. Vous obtenez des pistes à étudier, leurs principales conditions et les points d'attention, puis vous pouvez demander à être rappelé.",
      q: {
        goal: "Quel est votre projet ?",
        education: "Votre plus haut diplôme ?",
        age: "Votre âge ?",
        language: "Quelles langues parlez-vous ?",
        budget: "Budget disponible pour le projet (hors billet d'avion) ?",
        experience: "Votre expérience professionnelle ?",
        destination: "Une destination en tête ?",
      },
      goalOptions: [
        { value: "study", label: "Faire mes études" },
        { value: "work", label: "Travailler" },
        { value: "permanent", label: "M'installer durablement" },
        { value: "training", label: "Une formation payée (alternance)" },
      ],
      educationOptions: [
        { value: "secondary", label: "Probatoire, BEPC ou CAP" },
        { value: "bac", label: "Baccalauréat ou GCE A Level" },
        { value: "bachelor", label: "Licence, BTS ou HND" },
        { value: "master", label: "Master, ingénieur ou plus" },
      ],
      ageOptions: [
        { value: "18-24", label: "18 à 24 ans" },
        { value: "25-29", label: "25 à 29 ans" },
        { value: "30-35", label: "30 à 35 ans" },
        { value: "36+", label: "36 ans et plus" },
      ],
      languageOptions: [
        { value: "fr", label: "Français" },
        { value: "fr-en", label: "Français et anglais" },
        { value: "en", label: "Anglais surtout" },
        { value: "de", label: "J'apprends l'allemand" },
      ],
      budgetOptions: [
        { value: "low", label: "Moins de 3 millions FCFA" },
        { value: "mid", label: "3 à 8 millions FCFA" },
        { value: "high", label: "Plus de 8 millions FCFA" },
      ],
      experienceOptions: [
        { value: "none", label: "Pas encore d'expérience" },
        { value: "1-2", label: "1 à 2 ans" },
        { value: "3+", label: "3 ans ou plus" },
      ],
      destinationOptions: [
        { value: "any", label: "Peu importe, je veux la meilleure voie" },
        { value: "ca", label: "Canada" },
        { value: "de", label: "Allemagne" },
        { value: "fr", label: "France" },
        { value: "be", label: "Belgique" },
      ],
      profileTitle: "Votre profil",
      keyConditions: "Principales conditions à vérifier",
      officialSource: "Source officielle",
      seeProgram: "Voir la fiche complète",
      restricted: "Accès restreint ou récemment modifié",
      warningsTitle: "Points d'attention pour votre profil",
      warnings: {
        budgetStudy: "Les études à l'étranger demandent souvent de prouver des ressources importantes (compte bloqué en Allemagne, preuve de fonds au Canada, ressources mensuelles en France et en Belgique) : vérifiez ce montant en priorité.",
        german: "Les voies allemandes demandent en général un certificat d'allemand reconnu (souvent B1 pour l'Ausbildung).",
        french: "Les voies proposées supposent une bonne maîtrise du français, souvent attestée par un test reconnu (TCF, TEF).",
        experience: "La plupart des voies de travail qualifié demandent un diplôme reconnu ou une expérience professionnelle dans le métier.",
        age: "Pour l'Entrée express (Canada), l'âge fait baisser le score à partir de 30 ans environ : d'autres voies peuvent être plus adaptées.",
        secondary: "Une admission à l'université demande en général le baccalauréat ou le GCE A Level.",
        destination: "Votre destination préférée ne ressort pas en premier pour votre profil : comparez avec les autres pistes avant de décider.",
      },
      phoneError: "Numéro de téléphone invalide : saisissez un numéro camerounais (ex. 6 99 12 34 56) ou international avec l'indicatif.",
      next: "Suivant",
      back: "Retour",
      step: "Question",
      resultTitle: "Des pistes à étudier pour votre profil",
      resultLead: "Orientation indicative calculée à partir de vos réponses, pas une décision d'éligibilité : les conditions exactes dépendent de votre dossier et des règles en vigueur.",
      noResult: "Vos réponses ne permettent pas d'indiquer une piste fiable. Cela ne veut pas dire qu'aucune voie n'existe : un conseiller peut étudier votre situation.",
      contactTitle: "Être rappelé gratuitement",
      contactLead: "Un conseiller Izimboa vous appelle pour faire le point sur votre profil, où que vous soyez au Cameroun. Sans engagement ; vos coordonnées ne servent qu'à vous recontacter.",
      name: "Votre nom",
      phone: "Téléphone",
      phoneHint: "De préférence votre numéro WhatsApp.",
      city: "Votre ville",
      email: "E-mail",
      optional: "(facultatif)",
      submit: "Être rappelé",
      sending: "Envoi en cours…",
      success: "Merci ! Un conseiller Izimboa vous rappelle sous un jour ouvré.",
      error: "L'envoi n'a pas abouti. Vérifiez votre connexion et réessayez, ou écrivez-nous à {email}.",
      consent: "Nous utilisons ces informations uniquement pour vous recontacter.",
      privacyLink: "Politique de confidentialité",
      restart: "Recommencer le test",
      disclaimer: "Orientation indicative. Izimboa ne délivre pas de visa et ne garantit aucun résultat.",
    },
    compare: {
      slug: "quel-pays-choisir",
      meta: {
        title: "Canada, Allemagne, France ou Belgique : quel pays choisir ?",
        description: "Étudier ou travailler à l'étranger depuis le Cameroun : comparez langue, coût, travail et installation au Canada, en Allemagne, en France et en Belgique.",
      },
      h1: "Canada, Allemagne, France ou Belgique : quel pays choisir depuis le Cameroun ?",
      intro: "Il n'existe pas de meilleure destination pour tout le monde, mais une meilleure voie pour votre profil. Voici l'essentiel pour comparer, avant d'entrer dans le détail de chaque programme.",
      tableCaption: "Comparaison des quatre destinations (informations générales, vérifiées sur les sources officielles des programmes)",
      headers: { destination: "Destination", language: "Langue", study: "Études", work: "Travail", permanent: "Installation durable", budget: "Budget de départ" },
      rows: {
        ca: { language: "Français et anglais (Québec : français)", study: "Permis d'études, nombre de places limité chaque année", work: "Mobilité francophone hors Québec, offres d'emploi", permanent: "Possible directement (Entrée express, provinces, Québec)", budget: "Élevé pour les études (preuve de fonds importante)" },
        de: { language: "Allemand (anglais pour certains masters)", study: "Universités publiques presque gratuites, compte bloqué obligatoire", work: "Carte opportunité, Carte bleue européenne, Ausbildung payée", permanent: "Après quelques années de travail", budget: "Moyen pour les études, faible pour l'Ausbildung" },
        fr: { language: "Français", study: "Procédure Études en France (Campus France)", work: "Titre « Talent » ou contrat de travail avec autorisation", permanent: "Après plusieurs années de séjour", budget: "Moyen" },
        be: { language: "Français (Wallonie-Bruxelles), néerlandais (Flandre)", study: "Admission puis visa étudiant, preuve de solvabilité", work: "Surtout après les études", permanent: "Après plusieurs années de séjour", budget: "Moyen" },
      },
      adviceTitle: "Comment choisir",
      advice: [
        { title: "Partez de votre objectif", text: "Études, travail, formation payée ou installation : chaque objectif ouvre des voies différentes, parfois plus rapides dans un autre pays que celui auquel vous pensiez." },
        { title: "Regardez la langue", text: "Le français ouvre la France, la Belgique, le Québec et des programmes réservés aux francophones au Canada. L'allemand ouvre l'Ausbildung et les études presque gratuites." },
        { title: "Calculez le vrai budget en FCFA", text: "Frais officiels, preuve de fonds ou compte bloqué, billet, logement : le budget réel décide souvent du pays." },
        { title: "Méfiez-vous des promesses", text: "Personne ne peut garantir un visa. Une bonne orientation, un dossier complet et des délais respectés sont vos meilleurs atouts." },
      ],
      faq: [
        { q: "Quel est le pays le plus accessible depuis le Cameroun ?", a: "Cela dépend de votre profil : avec peu de budget et l'envie d'apprendre l'allemand, l'Ausbildung est souvent la plus accessible ; avec un diplôme et une bonne maîtrise du français, les programmes francophones du Canada sont intéressants. Le test d'éligibilité vous oriente en une minute." },
        { q: "Peut-on préparer sa procédure sans habiter Douala ou Yaoundé ?", a: "Oui. Une grande partie de la préparation se fait en ligne. Certaines étapes demandent de se déplacer, surtout le rendez-vous au centre de visa ou à l'ambassade (à Yaoundé, parfois Douala ou Garoua selon le pays), et parfois un examen de langue : nous vous aidons à les planifier pour limiter les allers-retours." },
        { q: "Izimboa travaille-t-il avec une seule destination ?", a: "Non. Notre rôle est de trouver la voie la plus adaptée à votre profil parmi plusieurs pays, puis de vous accompagner étape par étape." },
      ],
    },
    cityOptions,
  },
  en: {
    destinations: { ca: "Canada", de: "Germany", fr: "France", be: "Belgium" },
    goals: { study: "Study", work: "Work", permanent: "Permanent residence", training: "Paid vocational training" },
    hub: {
      title: "Find the right route, not just a destination",
      lead: "Canada, Germany, France, Belgium: every program has its own requirements, timelines and budget. We checked them on official sources and explain them from Cameroon, in FCFA.",
      all: "All",
      filterLabel: "Filter by goal",
      compareTitle: "Torn between several countries?",
      compareText: "Language, study costs, work options, permanent residence: compare the destinations side by side.",
      compareLink: "Compare countries",
      citiesTitle: "Everywhere in Cameroon",
      citiesLead: "Much of the preparation can be done remotely. Wherever you are, we help you prepare your file and plan the trips you need (visa centre or embassy appointment, biometrics, sometimes a language test).",
      readMore: "See the program",
    },
    program: {
      forWho: "Who is it for?",
      conditions: "Main requirements",
      steps: "Steps",
      costs: "Official fees",
      costLabel: "Fee",
      costAmount: "Amount",
      costFcfa: "In FCFA",
      costsNote: "Amounts published at the date of verification; the FCFA conversion is indicative. This table is not the total cost of the project (flight, housing, insurance, translations, language tests…). Official fees are paid directly to the authorities or their providers; any Izimboa support fees are separate and quoted before any commitment.",
      costNature: "Type",
      costKinds: { official: "Official fee", provider: "Service provider fee", employer: "Paid by the employer", requirement: "Funds requirement (not a fee)" },
      uncertain: "to be confirmed",
      delays: "Timelines",
      biometrics: "Appointment and biometrics in Cameroon",
      faq: "FAQ",
      sources: "Official sources",
      updated: "Information checked on",
      disclaimer: "Izimboa is not a visa-issuing authority and does not guarantee any outcome. Only the destination country's authorities decide. Rules change often: always check the official sources below.",
      related: "Other routes for the same goal",
      paused: "Program currently paused or closed",
      limited: "Restricted access or recently changed rules: read the requirements and verification date",
      hubLink: "All programs",
    },
    trust: {
      title: "What Izimboa Go does, and what it does not",
      lead: "To choose with confidence, here is exactly what we offer, and its limits.",
      items: [
        { title: "General information", text: "Program guides written from official websites, with their sources and verification date. Free." },
        { title: "Guidance", text: "The eligibility test and a call with an adviser point you to routes worth exploring. This is indicative guidance, not an eligibility decision: only the authorities and institutions decide." },
        { title: "Administrative assistance", text: "Document checklist, file organisation, calendar, appointment preparation. You submit your own application and pay official fees directly to the authorities or their providers." },
        { title: "Regulated advice: the case of Canada", text: "For a Canadian application (including Québec), only authorised representatives (lawyers, Québec notaries, consultants regulated by the CICC) may be paid to advise or represent an applicant. Izimboa Go does not represent applicants before the authorities; for paid personalised advice on a Canadian application, we refer you to an authorised representative." },
      ],
      notAffiliated: "Izimboa Go is not a government body, an embassy, a visa centre (VFS Global, TLScontact) or Campus France, and has no official link with them.",
      legalLink: "Legal notice",
    },
    city: {
      travel: "Getting to your visa appointment",
      toYaounde: "To Yaoundé",
      toDouala: "To Douala",
      local: "Good to know locally",
      universities: "Higher education institutions",
      programs: "Most requested programs",
      others: "We also support",
      faq: "FAQ",
    },
    test: {
      title: "Assess your profile for free",
      cta: "Assess my profile for free",
      lead: "About six questions, two minutes. You get routes worth exploring, their main requirements and points to watch, then you can ask for a call back.",
      q: {
        goal: "What is your plan?",
        education: "Your highest qualification?",
        age: "Your age?",
        language: "Which languages do you speak?",
        budget: "Budget available for the project (excluding the flight)?",
        experience: "Your work experience?",
        destination: "A destination in mind?",
      },
      goalOptions: [
        { value: "study", label: "Study" },
        { value: "work", label: "Work" },
        { value: "permanent", label: "Settle long-term" },
        { value: "training", label: "Paid vocational training" },
      ],
      educationOptions: [
        { value: "secondary", label: "GCE O Level, Probatoire or CAP" },
        { value: "bac", label: "GCE A Level or Baccalauréat" },
        { value: "bachelor", label: "Bachelor's, BTS or HND" },
        { value: "master", label: "Master's, engineering degree or higher" },
      ],
      ageOptions: [
        { value: "18-24", label: "18 to 24" },
        { value: "25-29", label: "25 to 29" },
        { value: "30-35", label: "30 to 35" },
        { value: "36+", label: "36 or older" },
      ],
      languageOptions: [
        { value: "fr", label: "French" },
        { value: "fr-en", label: "French and English" },
        { value: "en", label: "Mostly English" },
        { value: "de", label: "I am learning German" },
      ],
      budgetOptions: [
        { value: "low", label: "Under 3 million FCFA" },
        { value: "mid", label: "3 to 8 million FCFA" },
        { value: "high", label: "Over 8 million FCFA" },
      ],
      experienceOptions: [
        { value: "none", label: "No experience yet" },
        { value: "1-2", label: "1 to 2 years" },
        { value: "3+", label: "3 years or more" },
      ],
      destinationOptions: [
        { value: "any", label: "No preference, I want the best route" },
        { value: "ca", label: "Canada" },
        { value: "de", label: "Germany" },
        { value: "fr", label: "France" },
        { value: "be", label: "Belgium" },
      ],
      profileTitle: "Your profile",
      keyConditions: "Main requirements to check",
      officialSource: "Official source",
      seeProgram: "See the full guide",
      restricted: "Restricted or recently changed",
      warningsTitle: "Points to watch for your profile",
      warnings: {
        budgetStudy: "Studying abroad often requires proof of substantial funds (blocked account in Germany, proof of funds in Canada, monthly resources in France and Belgium): check this amount first.",
        german: "German routes usually require a recognised German certificate (often B1 for Ausbildung).",
        french: "The suggested routes assume good French, often proven by a recognised test (TCF, TEF).",
        experience: "Most skilled work routes require a recognised qualification or work experience in the occupation.",
        age: "For Express Entry (Canada), age lowers the score from about 30: other routes may fit better.",
        secondary: "University admission usually requires the GCE A Level or Baccalauréat.",
        destination: "Your preferred destination does not come first for your profile: compare it with the other routes before deciding.",
      },
      phoneError: "Invalid phone number: enter a Cameroonian number (e.g. 6 99 12 34 56) or an international one with the country code.",
      next: "Next",
      back: "Back",
      step: "Question",
      resultTitle: "Routes worth exploring for your profile",
      resultLead: "Indicative guidance based on your answers, not an eligibility decision: exact requirements depend on your file and the rules in force.",
      noResult: "Your answers do not allow us to point to a reliable route. This does not mean none exists: an adviser can review your situation.",
      contactTitle: "Get a free call back",
      contactLead: "An Izimboa adviser will call you to review your profile, wherever you are in Cameroon. No commitment; your details are only used to get back to you.",
      name: "Your name",
      phone: "Phone",
      phoneHint: "Your WhatsApp number if possible.",
      city: "Your city",
      email: "E-mail",
      optional: "(optional)",
      submit: "Call me back",
      sending: "Sending…",
      success: "Thank you! An Izimboa adviser will call you back within one business day.",
      error: "Your request could not be sent. Check your connection and try again, or write to us at {email}.",
      consent: "We only use this information to get back to you.",
      privacyLink: "Privacy policy",
      restart: "Restart the test",
      disclaimer: "Indicative guidance. Izimboa does not issue visas and does not guarantee any outcome.",
    },
    compare: {
      slug: "which-country",
      meta: {
        title: "Canada, Germany, France or Belgium: which country?",
        description: "Study or work abroad from Cameroon: compare language, costs, work options and permanent residence in Canada, Germany, France and Belgium.",
      },
      h1: "Canada, Germany, France or Belgium: which country to choose from Cameroon?",
      intro: "There is no best destination for everyone, but there is a best route for your profile. Here are the essentials to compare before going into each program in detail.",
      tableCaption: "Comparison of the four destinations (general information, checked against the programs' official sources)",
      headers: { destination: "Destination", language: "Language", study: "Study", work: "Work", permanent: "Settling long-term", budget: "Starting budget" },
      rows: {
        ca: { language: "French and English (Québec: French)", study: "Study permit, limited number of places each year", work: "Francophone Mobility outside Québec, job offers", permanent: "Possible directly (Express Entry, provinces, Québec)", budget: "High for studies (large proof of funds)" },
        de: { language: "German (English for some master's)", study: "Public universities almost free, blocked account required", work: "Opportunity Card, EU Blue Card, paid Ausbildung", permanent: "After a few years of work", budget: "Medium for studies, low for Ausbildung" },
        fr: { language: "French", study: "Études en France procedure (Campus France)", work: "\"Talent\" permit or job contract with work authorisation", permanent: "After several years of residence", budget: "Medium" },
        be: { language: "French (Wallonia-Brussels), Dutch (Flanders)", study: "Admission then student visa, proof of means", work: "Mostly after studies", permanent: "After several years of residence", budget: "Medium" },
      },
      adviceTitle: "How to choose",
      advice: [
        { title: "Start from your goal", text: "Study, work, paid training or settling: each goal opens different routes, sometimes faster in another country than the one you had in mind." },
        { title: "Look at the language", text: "French opens France, Belgium, Québec and French-speaker programs in Canada. German opens Ausbildung and almost free studies." },
        { title: "Work out the real budget in FCFA", text: "Official fees, proof of funds or blocked account, flight, housing: the real budget often decides the country." },
        { title: "Beware of promises", text: "Nobody can guarantee a visa. Good guidance, a complete file and respected deadlines are your best assets." },
      ],
      faq: [
        { q: "Which country is the most accessible from Cameroon?", a: "It depends on your profile: with a small budget and willingness to learn German, Ausbildung is often the most accessible; with a degree and strong French, Canada's francophone programs are attractive. The eligibility test guides you in one minute." },
        { q: "Can I prepare my application without living in Douala or Yaoundé?", a: "Yes. Much of the preparation is done online. Some steps require travel, mainly the visa centre or embassy appointment (in Yaoundé, sometimes Douala or Garoua depending on the country), and sometimes a language test: we help you plan them to limit return trips." },
        { q: "Does Izimboa work with a single destination?", a: "No. Our role is to find the route that best fits your profile across several countries, then support you step by step." },
      ],
    },
    cityOptions: cityOptions.map((c) => (c === "Limbé" ? "Limbe" : c)),
  },
};
