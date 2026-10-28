import type { Lang } from "@/config/site";
import type { Destination, Goal } from "@/content/go-types";

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
    q: {
      goal: string;
      education: string;
      age: string;
      language: string;
      budget: string;
    };
    goalOptions: { value: Goal; label: string }[];
    educationOptions: { value: string; label: string }[];
    ageOptions: { value: string; label: string }[];
    languageOptions: { value: string; label: string }[];
    budgetOptions: { value: string; label: string }[];
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
      citiesLead: "La procédure se fait presque entièrement à distance. Où que vous soyez, nous vous accompagnons et vous aidons à organiser le seul déplacement nécessaire : le rendez-vous au centre de visa.",
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
      costsNote: "Montants officiels à la date de vérification ; la conversion en FCFA est indicative. Les frais d'accompagnement d'Izimboa sont annoncés séparément, avant tout engagement.",
      delays: "Délais",
      biometrics: "Rendez-vous et empreintes au Cameroun",
      faq: "Questions fréquentes",
      sources: "Sources officielles",
      updated: "Informations vérifiées le",
      disclaimer: "Izimboa n'est pas une autorité de délivrance de visas et ne garantit aucun résultat. Seules les autorités du pays de destination décident. Les règles changent souvent : vérifiez toujours sur les sources officielles ci-dessous.",
      related: "Autres voies pour le même objectif",
      paused: "Programme suspendu ou fermé actuellement",
      limited: "Places limitées ou règles en cours de changement",
      hubLink: "Tous les programmes",
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
      title: "Quel programme vous correspond ?",
      lead: "Cinq questions, une minute. Nous vous indiquons les voies les plus adaptées à votre profil, puis un conseiller peut vous rappeler gratuitement.",
      q: {
        goal: "Quel est votre projet ?",
        education: "Votre plus haut diplôme ?",
        age: "Votre âge ?",
        language: "Quelles langues parlez-vous ?",
        budget: "Budget disponible pour le projet (hors billet d'avion) ?",
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
      next: "Suivant",
      back: "Retour",
      step: "Question",
      resultTitle: "Les voies les plus adaptées à votre profil",
      resultLead: "Une première orientation, pas une décision : un conseiller vérifiera votre dossier en détail.",
      noResult: "Votre profil demande une étude personnalisée : laissez vos coordonnées, un conseiller vous rappelle.",
      contactTitle: "Être rappelé gratuitement",
      contactLead: "Un conseiller Izimboa vous appelle pour faire le point sur votre profil, où que vous soyez au Cameroun.",
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
        description: "Étudier ou travailler à l'étranger depuis le Cameroun : comparez langue, coût, travail et résidence permanente au Canada, en Allemagne, en France et en Belgique.",
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
        { q: "Peut-on préparer sa procédure sans habiter Douala ou Yaoundé ?", a: "Oui. Presque tout se fait en ligne. Le seul déplacement est en général le rendez-vous au centre de visa ou à l'ambassade, à Yaoundé ou Douala selon le pays : nous vous aidons à l'organiser en un seul voyage." },
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
      citiesLead: "Almost the whole process is done remotely. Wherever you are, we support you and help you plan the only trip you need: the visa centre appointment.",
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
      costsNote: "Official amounts at the date of verification; the FCFA conversion is indicative. Izimboa's support fees are quoted separately, before any commitment.",
      delays: "Timelines",
      biometrics: "Appointment and biometrics in Cameroon",
      faq: "FAQ",
      sources: "Official sources",
      updated: "Information checked on",
      disclaimer: "Izimboa is not a visa-issuing authority and does not guarantee any outcome. Only the destination country's authorities decide. Rules change often: always check the official sources below.",
      related: "Other routes for the same goal",
      paused: "Program currently paused or closed",
      limited: "Limited places or rules being changed",
      hubLink: "All programs",
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
      title: "Which program fits you?",
      lead: "Five questions, one minute. We show you the routes that best fit your profile, then an adviser can call you back for free.",
      q: {
        goal: "What is your plan?",
        education: "Your highest qualification?",
        age: "Your age?",
        language: "Which languages do you speak?",
        budget: "Budget available for the project (excluding the flight)?",
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
      next: "Next",
      back: "Back",
      step: "Question",
      resultTitle: "The routes that best fit your profile",
      resultLead: "A first orientation, not a decision: an adviser will check your file in detail.",
      noResult: "Your profile needs a personalised review: leave your details and an adviser will call you back.",
      contactTitle: "Get a free call back",
      contactLead: "An Izimboa adviser will call you to review your profile, wherever you are in Cameroon.",
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
        { q: "Can I prepare my application without living in Douala or Yaoundé?", a: "Yes. Almost everything is online. The only trip is usually the visa centre or embassy appointment, in Yaoundé or Douala depending on the country: we help you plan it as a single journey." },
        { q: "Does Izimboa work with a single destination?", a: "No. Our role is to find the route that best fits your profile across several countries, then support you step by step." },
      ],
    },
    cityOptions: cityOptions.map((c) => (c === "Limbé" ? "Limbe" : c)),
  },
};
