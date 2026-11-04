import type { Lang } from "@/config/site";
import type { CourierPage } from "../types";

/**
 * Page « Devenir livreur » au Cameroun (sous-page de la livraison de colis).
 * Recherches visées : « devenir livreur Douala », « recrutement livreur moto Yaoundé », « job livreur Cameroun ».
 * Chiffres repris des règles réelles : 65 % de la course au livreur (décision 57), tarifs de 1 000 à 2 000 FCFA (+ 500 express),
 * dossier CNI + selfie vérifié par l'équipe (décision 74), alerte qui sonne même téléphone verrouillé (décision 76).
 */
export const COURIER_PAGE: Record<Lang, CourierPage> = {
  fr: {
    slug: "devenir-livreur",
    navLabel: "Devenir livreur",
    meta: {
      title: "Devenir livreur moto à Douala et Yaoundé | Izimboa Delivery",
      description:
        "Devenez livreur Izimboa à Douala ou Yaoundé : inscription gratuite dans l'application Android, vous choisissez vos heures, votre gain s'affiche avant d'accepter et vous gardez 65 % de chaque course.",
    },
    h1: "Devenez livreur Izimboa à Douala et Yaoundé",
    lead: "Vous avez une moto et un smartphone Android ? Inscrivez-vous gratuitement dans l'application Izimboa Livreur, faites vérifier votre identité, puis recevez des courses près de vous quand vous le voulez.",
    heroPoints: ["Inscription gratuite", "65 % de chaque course pour vous", "Vous choisissez vos heures"],
    playButton: "Télécharger sur Google Play",
    playNote: "Application Android gratuite « Izimboa Livreur ». Version iPhone en préparation.",
    whyTitle: "Pourquoi livrer avec Izimboa",
    why: [
      { title: "Vous gardez 65 % de chaque course", text: "Le montant que vous gagnerez s'affiche avant que vous acceptiez la course. Pas de surprise, pas de négociation." },
      { title: "Vous travaillez quand vous voulez", text: "Passez « En ligne » quand vous êtes disponible, « Hors ligne » quand vous arrêtez. Aucun horaire imposé." },
      { title: "Des courses près de vous", text: "Chaque course est proposée au livreur libre le plus proche du point de ramassage : moins de route à vide, plus de courses dans la journée." },
      { title: "Une remise sans discussion", text: "Le destinataire donne un code au moment de la remise : la livraison est prouvée, pour vous comme pour le vendeur." },
      { title: "Des adresses faciles à trouver", text: "Le lieu de livraison est une épingle sur la carte, avec un repère du quartier. Un bouton ouvre l'itinéraire dans Google Maps." },
      { title: "Vous ne ratez aucune course", text: "Quand vous êtes en ligne, le téléphone sonne pour chaque nouvelle course, même verrouillé, comme un appel." },
    ],
    needsTitle: "Ce qu'il vous faut",
    needs: [
      "Une carte nationale d'identité (CNI) valide",
      "Un smartphone Android avec Internet et la localisation activée",
      "Une moto (ou un autre moyen de vous déplacer en ville)",
      "Travailler à Douala ou à Yaoundé",
    ],
    stepsTitle: "Inscription en 4 étapes",
    steps: [
      { title: "Installez l'application", text: "Téléchargez « Izimboa Livreur » sur Google Play. C'est gratuit." },
      { title: "Connectez-vous avec WhatsApp", text: "Entrez votre numéro, recevez un code sur WhatsApp. Pas de mot de passe à retenir." },
      { title: "Complétez votre dossier", text: "Votre nom, puis les photos de votre CNI (recto et verso) et un selfie, pris directement avec l'appareil photo." },
      { title: "Validation, puis vos premières courses", text: "Notre équipe vérifie votre dossier. Dès qu'il est validé, passez « En ligne » et recevez vos premières courses." },
    ],
    faqTitle: "Questions fréquentes des livreurs",
    faq: [
      {
        q: "Combien gagne un livreur Izimboa ?",
        a: "Vous gardez 65 % du prix de chaque course. Une course coûte de 1 000 à 2 000 FCFA selon la distance, plus 500 FCFA en express : vous gagnez donc de 650 à 1 625 FCFA par course. Le montant s'affiche avant que vous acceptiez.",
      },
      { q: "L'inscription est-elle payante ?", a: "Non. L'application et l'inscription sont gratuites." },
      {
        q: "Pourquoi faut-il ma CNI et un selfie ?",
        a: "Les vendeurs nous confient leurs colis et l'argent de leurs clients. Vérifier l'identité de chaque livreur protège tout le monde, vous compris. Vos photos sont visibles seulement par l'équipe de vérification d'Izimboa.",
      },
      {
        q: "Que se passe-t-il si mon dossier est refusé ?",
        a: "Le motif s'affiche dans l'application (par exemple une photo illisible). Vous corrigez et renvoyez votre dossier.",
      },
      { q: "Dois-je avoir ma propre moto ?", a: "Pour l'instant, oui : vous livrez avec votre propre moyen de transport, le plus souvent une moto." },
      {
        q: "L'application vide-t-elle la batterie ?",
        a: "Non. Hors course, la localisation est économe ; elle devient précise seulement pendant une livraison. Hors ligne, l'application ne suit plus votre position.",
      },
      { q: "Comment joindre Izimboa ?", a: "Appelez le +237 6 99 78 65 14 ou écrivez à contact@izimboa.com." },
    ],
    ctaTitle: "Prêt à rouler avec Izimboa ?",
    ctaText: "Installez l'application, complétez votre dossier en quelques minutes, et recevez vos premières courses dès sa validation.",
    sellerTitle: "Vous avez plutôt des colis à envoyer ?",
    sellerText: "Créez votre compte vendeur sur delivery.izimboa.com et commandez votre première livraison.",
    sellerButton: "Créer mon compte vendeur",
  },
  en: {
    slug: "become-a-rider",
    navLabel: "Become a rider",
    meta: {
      title: "Become a delivery rider in Douala and Yaoundé | Izimboa Delivery",
      description:
        "Become an Izimboa rider in Douala or Yaoundé: free sign-up in the Android app, choose your own hours, see your earnings before you accept and keep 65% of every job.",
    },
    h1: "Become an Izimboa rider in Douala and Yaoundé",
    lead: "Got a motorbike and an Android smartphone? Sign up for free in the Izimboa Livreur app, get your ID checked, then receive delivery jobs near you whenever you want.",
    heroPoints: ["Free sign-up", "65% of every job is yours", "You choose your hours"],
    playButton: "Get it on Google Play",
    playNote: "Free Android app “Izimboa Livreur”. iPhone version on the way.",
    whyTitle: "Why ride with Izimboa",
    why: [
      { title: "Keep 65% of every job", text: "Your earnings for the job are shown before you accept it. No surprises, no haggling." },
      { title: "Work when you want", text: "Go “Online” when you are available and “Offline” when you stop. No fixed hours." },
      { title: "Jobs near you", text: "Each job goes to the free rider closest to the pickup point: less riding empty, more jobs in a day." },
      { title: "Handover without arguments", text: "The recipient gives a code at handover: the delivery is proven, for you and for the seller." },
      { title: "Addresses that are easy to find", text: "The drop-off is a pin on the map with a local landmark. One button opens the route in Google Maps." },
      { title: "Never miss a job", text: "While you are online, your phone rings for every new job, even when locked, like a call." },
    ],
    needsTitle: "What you need",
    needs: [
      "A valid national ID card (CNI)",
      "An Android smartphone with internet and location turned on",
      "A motorbike (or another way to get around the city)",
      "To work in Douala or Yaoundé",
    ],
    stepsTitle: "Sign up in 4 steps",
    steps: [
      { title: "Install the app", text: "Download “Izimboa Livreur” from Google Play. It's free." },
      { title: "Sign in with WhatsApp", text: "Enter your number and receive a code on WhatsApp. No password to remember." },
      { title: "Complete your file", text: "Your name, then photos of your ID card (front and back) and a selfie, taken straight from the camera." },
      { title: "Approval, then your first jobs", text: "Our team checks your file. As soon as it is approved, go “Online” and receive your first jobs." },
    ],
    faqTitle: "Rider questions",
    faq: [
      {
        q: "How much does an Izimboa rider earn?",
        a: "You keep 65% of the price of every job. A delivery costs 1,000 to 2,000 FCFA depending on distance, plus 500 FCFA for express, so you earn 650 to 1,625 FCFA per job. The amount is shown before you accept.",
      },
      { q: "Does it cost anything to sign up?", a: "No. The app and the sign-up are free." },
      {
        q: "Why do you need my ID card and a selfie?",
        a: "Sellers trust us with their parcels and their customers' money. Checking every rider's identity protects everyone, you included. Your photos are only visible to Izimboa's verification team.",
      },
      { q: "What if my file is rejected?", a: "The reason shows in the app (for example an unreadable photo). You fix it and send your file again." },
      { q: "Do I need my own motorbike?", a: "For now, yes: you deliver with your own transport, usually a motorbike." },
      {
        q: "Does the app drain the battery?",
        a: "No. Between jobs, location tracking is light; it only becomes precise during a delivery. When you are offline, the app stops tracking your position.",
      },
      { q: "How do I contact Izimboa?", a: "Call +237 6 99 78 65 14 or write to contact@izimboa.com." },
    ],
    ctaTitle: "Ready to ride with Izimboa?",
    ctaText: "Install the app, complete your file in a few minutes, and get your first jobs as soon as it is approved.",
    sellerTitle: "Got parcels to send instead?",
    sellerText: "Create your seller account on delivery.izimboa.com and order your first delivery.",
    sellerButton: "Create my seller account",
  },
};

export function findCourierPage(lang: Lang, slug: string): CourierPage | undefined {
  return COURIER_PAGE[lang].slug === slug ? COURIER_PAGE[lang] : undefined;
}
