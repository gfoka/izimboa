import type { PhotoKey } from "@/config/photos";
import type { ProductKey } from "@/config/site";

export type Meta = { title: string; description: string };
export type Item = { title: string; text: string; id?: string };
export type Faq = { q: string; a: string };

export type ProductContent = {
  name: string;
  navLabel: string;
  cardText: string;
  meta: Meta;
  h1: string;
  intro: string;
  status: string;
  photo: PhotoKey;
  stepsTitle: string;
  steps: Item[];
  featuresTitle: string;
  features: Item[];
  faqTitle: string;
  faq: Faq[];
  notice?: string;
  cta: { title: string; text: string; button: string; subject: string };
  /** Application en ligne : bouton principal vers l'espace du produit. */
  app?: { button: string; team: string };
  /** Formulaire « Entreprises » (demande enregistrée par l'API, e-mail à l'équipe). */
  business?: BusinessForm;
  /** Le bouton d'équipe mène au test d'éligibilité de la page (#test). */
  testAnchor?: boolean;
};

export type BusinessForm = {
  title: string;
  lead: string;
  points: string[];
  company: string;
  contactName: string;
  phone: string;
  phoneHint: string;
  email: string;
  emailHint: string;
  city: string;
  cities: string[];
  activity: string;
  activityPlaceholder: string;
  volume: string;
  volumes: { value: string; label: string }[];
  message: string;
  optional: string;
  submit: string;
  sending: string;
  success: string;
  successAgain: string;
  error: string;
  consent: string;
  privacyLink: string;
};

export type Dict = {
  langName: string;
  common: {
    skipToContent: string;
    menu: string;
    services: string;
    about: string;
    contact: string;
    otherLanguage: string;
    comingSoon: string;
    discover: string;
    home: string;
    photoBy: string;
    onUnsplash: string;
    otherServices: string;
    countriesTitle: string;
    openLabel: string;
    soonLabel: string;
    footerTagline: string;
    footerCompany: string;
    legal: string;
    privacy: string;
    sellerSpace: string;
    rights: string;
  };
  home: {
    meta: Meta;
    eyebrow: string;
    h1: string;
    lead: string;
    primary: string;
    secondary: string;
    servicesTitle: string;
    servicesLead: string;
    pillarsTitle: string;
    pillarsLead: string;
    pillars: Item[];
    accountTitle: string;
    accountText: string;
    accountPoints: string[];
    regionTitle: string;
    regionText: string;
    ctaTitle: string;
    ctaText: string;
    ctaButton: string;
    ctaApp?: string;
  };
  products: Record<ProductKey, ProductContent>;
  about: {
    meta: Meta;
    h1: string;
    lead: string;
    sections: Item[];
    valuesTitle: string;
    values: Item[];
  };
  contact: {
    meta: Meta;
    h1: string;
    lead: string;
    channels: { title: string; text: string; subject: string; button: string }[];
    directTitle: string;
    emailLabel: string;
    whatsappLabel: string;
    responseNote: string;
  };
  legal: {
    meta: Meta;
    h1: string;
    sections: Item[];
  };

  privacy: {
    meta: Meta;
    h1: string;
    sections: Item[];
  };
};
