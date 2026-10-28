import type { Lang } from "@/config/site";

/**
 * Guides thématiques d'Izimboa Go (Cameroun) : un public, une question, des liens vers les fiches programmes.
 * Uniquement des affirmations générales et vérifiables ; les chiffres restent dans les fiches (sources et dates).
 */
export type GuideText = {
  metaTitle: string;
  metaDescription: string;
  h1: string;
  intro: string;
  callbackTitle: string;
  sections: { title: string; text: string; programs?: string[] }[];
  faq: { q: string; a: string }[];
};

export type Guide = { key: string; slug: Record<Lang, string>; fr: GuideText; en: GuideText };

export const GUIDES: Guide[] = [
  {
    key: "workers",
    slug: { fr: "travailler-a-l-etranger", en: "work-abroad" },
    fr: {
      metaTitle: "Travailler à l'étranger depuis le Cameroun : les voies",
      metaDescription: "Diplôme, métier ou expérience : les voies pour travailler au Canada, en Allemagne ou en France depuis le Cameroun, avec ou sans offre d'emploi.",
      h1: "Travailler à l'étranger depuis le Cameroun",
      intro: "Vous avez un diplôme, un métier ou plusieurs années d'expérience ? Plusieurs pays recherchent des profils qualifiés, et certaines voies mènent à une installation durable. Voici comment elles se distinguent, pour choisir celle qui correspond à votre parcours.",
      callbackTitle: "Parlons de votre projet professionnel",
      sections: [
        { title: "Avec une offre d'emploi", text: "Si un employeur étranger vous recrute, plusieurs permis deviennent accessibles : la Mobilité francophone pour un emploi au Canada hors Québec, le titre « Talent » ou le visa salarié en France, la Carte bleue européenne en Allemagne.", programs: ["ca-francophone-mobility", "fr-talent-work", "de-skilled-work"] },
        { title: "Sans offre d'emploi au départ", text: "Certaines voies ne demandent pas d'offre d'emploi au moment de la candidature : l'Entrée express au Canada (sélection par score), certains programmes des provinces, ou la carte opportunité en Allemagne, qui permet de chercher un emploi sur place.", programs: ["ca-express-entry", "ca-pnp", "de-skilled-work"] },
        { title: "Se reconvertir avec une formation payée", text: "L'Ausbildung allemande permet d'apprendre un métier en étant rémunéré par une entreprise. Elle demande un bon niveau d'allemand et un contrat de formation.", programs: ["de-ausbildung"] },
        { title: "Faire reconnaître vos diplômes et votre expérience", text: "La plupart des voies demandent de faire évaluer ou reconnaître vos diplômes, de prouver votre expérience (attestations, contrats, bulletins de paie) et votre niveau de langue. Bien préparés, ces documents font souvent la différence." },
      ],
      faq: [
        { q: "Faut-il forcément une offre d'emploi ?", a: "Non. Certaines voies en demandent une (Mobilité francophone, visa salarié), d'autres non (Entrée express, carte opportunité en Allemagne). Le test d'éligibilité vous indique les pistes adaptées à votre profil." },
        { q: "Mon expérience au Cameroun compte-t-elle ?", a: "Dans de nombreuses voies, oui, si elle est qualifiée et prouvée par des documents. Les règles exactes dépendent de chaque programme : elles sont détaillées dans les fiches." },
        { q: "Combien de temps faut-il compter ?", a: "Selon le pays et la voie, de quelques mois à plus d'un an. Chaque fiche indique les délais publiés par les sources officielles." },
      ],
    },
    en: {
      metaTitle: "Work abroad from Cameroon: the routes",
      metaDescription: "Degree, trade or experience: the routes to work in Canada, Germany or France from Cameroon, with or without a job offer.",
      h1: "Working abroad from Cameroon",
      intro: "Do you have a degree, a trade or several years of experience? Several countries are looking for skilled profiles, and some routes lead to settling long-term. Here is how they differ, so you can choose the one that fits your background.",
      callbackTitle: "Let's talk about your career plan",
      sections: [
        { title: "With a job offer", text: "If a foreign employer hires you, several permits open up: Francophone Mobility for a job in Canada outside Québec, the \"Talent\" permit or salaried worker visa in France, the EU Blue Card in Germany.", programs: ["ca-francophone-mobility", "fr-talent-work", "de-skilled-work"] },
        { title: "Without a job offer at first", text: "Some routes do not require a job offer when you apply: Express Entry in Canada (selection by score), some provincial programs, or Germany's Opportunity Card, which lets you look for a job on site.", programs: ["ca-express-entry", "ca-pnp", "de-skilled-work"] },
        { title: "Retrain with paid training", text: "German Ausbildung lets you learn a trade while being paid by a company. It requires good German and a training contract.", programs: ["de-ausbildung"] },
        { title: "Get your qualifications and experience recognised", text: "Most routes require your qualifications to be assessed or recognised, and proof of your experience (certificates, contracts, payslips) and language level. Well prepared, these documents often make the difference." },
      ],
      faq: [
        { q: "Do I need a job offer?", a: "Not always. Some routes require one (Francophone Mobility, salaried worker visa), others do not (Express Entry, Germany's Opportunity Card). The eligibility test shows the routes that fit your profile." },
        { q: "Does my experience in Cameroon count?", a: "In many routes, yes, if it is skilled and documented. The exact rules depend on each program and are detailed in the guides." },
        { q: "How long does it take?", a: "Depending on the country and route, from a few months to over a year. Each program guide shows the timelines published by official sources." },
      ],
    },
  },
  {
    key: "family",
    slug: { fr: "partir-en-famille", en: "move-with-your-family" },
    fr: {
      metaTitle: "Partir en famille ou faire venir sa famille",
      metaDescription: "Conjoint, enfants : les voies pour partir en famille ou faire venir sa famille au Canada, en Allemagne ou en France depuis le Cameroun, et le budget à prévoir.",
      h1: "Partir en famille ou faire venir votre famille",
      intro: "Votre projet concerne aussi votre conjoint et vos enfants ? Certaines voies permettent de partir ensemble, d'autres de faire venir votre famille une fois installé. Le choix de la voie change tout : conditions, délais et budget.",
      callbackTitle: "Parlons de votre projet familial",
      sections: [
        { title: "Partir ensemble dès la demande", text: "Pour la résidence permanente au Canada (Entrée express, programmes des provinces, Québec), le conjoint et les enfants à charge peuvent être inclus dans la même demande. Des frais s'appliquent pour chaque membre de la famille : ils sont indiqués dans les fiches.", programs: ["ca-express-entry", "ca-pnp", "ca-quebec"] },
        { title: "Partir d'abord, faire venir sa famille ensuite", text: "Une fois installé avec un titre de séjour, la plupart des pays prévoient le regroupement familial, sous conditions : titre de séjour, ressources, logement, parfois langue. Ces conditions varient selon le pays et votre statut.", programs: ["de-skilled-work", "fr-talent-work"] },
        { title: "Avec un permis de travail ou d'études", text: "Selon le pays et votre situation, votre conjoint peut parfois obtenir un titre de séjour ou un permis de travail. Ces règles ont récemment changé au Canada : elles doivent être vérifiées pour votre cas précis.", programs: ["ca-francophone-mobility", "ca-study-permit"] },
        { title: "Prévoir le budget familial", text: "Chaque membre de la famille augmente les frais officiels et, souvent, le montant des fonds à prouver. Mieux vaut le calculer dès le départ, en FCFA, pour choisir une voie réaliste." },
      ],
      faq: [
        { q: "Puis-je partir avec mes enfants ?", a: "Oui, dans plusieurs voies : par exemple, les demandes de résidence permanente au Canada peuvent inclure les enfants à charge. D'autres voies passent par un regroupement familial après votre installation." },
        { q: "Combien coûte une demande en famille ?", a: "Cela dépend de la voie. Pour l'Entrée express, par exemple, des frais s'ajoutent pour le conjoint et pour chaque enfant : les montants à jour sont dans la fiche du programme." },
        { q: "Mon conjoint pourra-t-il travailler ?", a: "Parfois, selon le pays, votre statut et votre métier. Ces règles changent souvent : un conseiller les vérifie avec vous." },
      ],
    },
    en: {
      metaTitle: "Move with your family or bring your family abroad",
      metaDescription: "Spouse, children: the routes to move with your family or bring them to Canada, Germany or France from Cameroon, and the budget to plan.",
      h1: "Moving with your family, or bringing them later",
      intro: "Does your plan include your spouse and children? Some routes let you move together, others let you bring your family once you are settled. The route you choose changes everything: requirements, timelines and budget.",
      callbackTitle: "Let's talk about your family plan",
      sections: [
        { title: "Moving together from the start", text: "For permanent residence in Canada (Express Entry, provincial programs, Québec), your spouse and dependent children can be included in the same application. Fees apply for each family member: they are shown in the program guides.", programs: ["ca-express-entry", "ca-pnp", "ca-quebec"] },
        { title: "Going first, bringing your family later", text: "Once you are settled with a residence permit, most countries allow family reunification, under conditions: residence permit, income, housing, sometimes language. These vary by country and status.", programs: ["de-skilled-work", "fr-talent-work"] },
        { title: "With a work or study permit", text: "Depending on the country and your situation, your spouse may sometimes get a residence or work permit. These rules recently changed in Canada and must be checked for your exact case.", programs: ["ca-francophone-mobility", "ca-study-permit"] },
        { title: "Plan the family budget", text: "Each family member adds official fees and often increases the funds you must prove. It is best to work it out from the start, in FCFA, to choose a realistic route." },
      ],
      faq: [
        { q: "Can I move with my children?", a: "Yes, in several routes: for example, Canadian permanent residence applications can include dependent children. Other routes go through family reunification after you settle." },
        { q: "How much does a family application cost?", a: "It depends on the route. For Express Entry, for example, fees are added for the spouse and each child: up-to-date amounts are in the program guide." },
        { q: "Will my spouse be able to work?", a: "Sometimes, depending on the country, your status and occupation. These rules change often: an adviser checks them with you." },
      ],
    },
  },
];
