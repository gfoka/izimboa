import type { Dict } from "../types";

/** Cameroon content — English. */
const en: Dict = {
  langName: "English",
  common: {
    skipToContent: "Skip to content",
    menu: "Menu",
    services: "Services",
    about: "About",
    contact: "Contact",
    otherLanguage: "Français",
    comingSoon: "Coming soon",
    discover: "Discover",
    home: "Home",
    photoBy: "Photo:",
    onUnsplash: "on Unsplash",
    otherServices: "More Izimboa services",
    countriesTitle: "Our countries",
    openLabel: "Open",
    soonLabel: "Soon",
    footerTagline: "Delivery, immigration, travel and international shopping. Born in Cameroon, built for Central Africa.",
    footerCompany: "Company",
    legal: "Legal notice",
    privacy: "Privacy policy",
    rights: "All rights reserved.",
  },
  home: {
    meta: {
      title: "Izimboa: parcel delivery, immigration support, flights and shopping in Cameroon",
      description:
        "Izimboa brings together parcel delivery in Douala, step-by-step immigration support, flight tickets and international shopping delivered to Cameroon. One account, clear tracking.",
    },
    eyebrow: "Cameroon · Central Africa",
    h1: "Movement you can trust.",
    lead:
      "Your parcels, your plans to move abroad, your trips and your shopping from overseas: Izimboa takes care of the journey and keeps you informed at every step. Born in Cameroon, built for all of Central Africa.",
    primary: "Explore our services",
    secondary: "Contact us",
    servicesTitle: "Four services, one standard",
    servicesLead: "Each Izimboa service meets a real need to move, whether people or goods.",
    pillarsTitle: "Why Izimboa",
    pillarsLead: "We don't just sell a journey: we sell peace of mind.",
    pillars: [
      {
        title: "Tracking at every step",
        text: "Live parcel tracking, procedures broken into clear steps, a notification each time things move forward: you always know where you stand.",
      },
      {
        title: "Simple, traceable payments",
        text: "Orange Money, MTN Mobile Money or cash on delivery. Every franc is recorded and you get a receipt.",
      },
      {
        title: "A team close to you",
        text: "Local customer support on WhatsApp, a team that knows your neighbourhood and speaks your language, in English and French.",
      },
    ],
    accountTitle: "One account for every service",
    accountText:
      "Create your Izimboa account once and use it everywhere: to send a parcel, follow your immigration file or book a flight.",
    accountPoints: [
      "Sign in with WhatsApp, SMS or Google",
      "All your payments and receipts in one place",
      "Your documents protected and encrypted",
    ],
    regionTitle: "Born in Cameroon, open to Central Africa",
    regionText:
      "We are starting in Cameroon, in Douala, and will open the neighbouring CEMAC countries one by one, with services adapted to each.",
    ctaTitle: "Be among the first",
    ctaText: "Izimboa is launching its services in Cameroon. Write to us to hear about the launch or to become a partner.",
    ctaButton: "Write to us",
  },
  products: {
    delivery: {
      name: "Izimboa Delivery",
      navLabel: "Parcel delivery",
      cardText: "Parcel delivery in Douala with live tracking, and cash on delivery paid out the same day to sellers.",
      meta: {
        title: "Fast, tracked parcel delivery in Douala | Izimboa Delivery",
        description:
          "Send a parcel in Douala: see the price before you order, pay with Orange Money or MTN MoMo, track your rider live and get proof of delivery. Cash on delivery paid out to sellers the same day.",
      },
      h1: "Fast, tracked parcel delivery in Douala",
      intro:
        "Enter pickup and drop-off, see the price straight away, then follow your rider on the map until the parcel is handed over. For online sellers, we collect cash on delivery and pay you the same day.",
      status: "Launching soon in Douala",
      photo: "moto",
      stepsTitle: "How it works",
      steps: [
        { title: "Enter the route", text: "Pickup and drop-off, with a pin on the map and a local landmark: \"behind the pharmacy\", \"blue gate\"." },
        { title: "See the price, pay", text: "The price is shown before you order. Pay with Orange Money, MTN Mobile Money, or let your customer pay on delivery." },
        { title: "An Izimboa rider picks it up", text: "A trained, identified rider in Izimboa gear collects the parcel and confirms the pickup." },
        { title: "Track it, get proof", text: "Follow the motorbike live. Handover is confirmed with a code or a photo, so you have proof of delivery." },
      ],
      featuresTitle: "Built for online sellers",
      features: [
        { title: "Cash on delivery, paid out the same day", text: "Selling on WhatsApp, Facebook or Instagram? The rider collects payment and Izimboa sends your money the same evening via Mobile Money." },
        { title: "Riders you can trust", text: "Every rider is identified, trained and monitored. Every trip is tracked from pickup to handover." },
        { title: "Addresses that work", text: "Even without a street name, a map pin and a landmark let the rider find the place without ten phone calls." },
        { title: "Customer support on WhatsApp", text: "A question or a problem? A local team answers you directly on WhatsApp." },
      ],
      faqTitle: "Delivery FAQ",
      faq: [
        { q: "Which cities do you deliver to?", a: "We are starting with Douala. Other cities in Cameroon, then neighbouring countries, will open step by step." },
        { q: "How is the price calculated?", a: "The price depends on the distance. It is shown before you confirm your order, so there are no surprises." },
        { q: "How does cash on delivery work?", a: "The rider collects the item's price from your customer at handover. Izimboa pays that money out to you the same day via Mobile Money." },
        { q: "My address has no street name. Is that a problem?", a: "No. You drop a pin on the map and add a simple landmark. That is exactly what it is designed for." },
        { q: "What happens if something goes wrong with a parcel?", a: "Every step is recorded, from pickup to handover with proof. Our support team reviews every claim using that history." },
      ],
      cta: {
        title: "Selling online in Douala?",
        text: "Write to us to be among the first sellers delivered by Izimboa.",
        button: "Become a partner seller",
        subject: "Izimboa Delivery - partner seller",
      },
    },
    go: {
      name: "Izimboa Go",
      navLabel: "Immigration support",
      cardText: "Your visa and immigration process broken into clear steps, with online payment and tracking.",
      meta: {
        title: "Visa and immigration process from Cameroon, step by step | Izimboa Go",
        description:
          "Prepare your move from Cameroon: a visa process broken into steps, a clear document checklist, online payment and a notification at every milestone. Clear, reassuring support.",
      },
      h1: "Your visa and immigration process, step by step",
      intro:
        "Studying, working or settling abroad, for example in Canada, involves many formalities. Izimboa Go breaks them into clear steps, so you know what to provide, what to pay and where your file stands.",
      status: "Launching soon in Cameroon",
      photo: "passport",
      stepsTitle: "How it works",
      steps: [
        { title: "Choose your process", text: "Destination and type of project: study, work, family visit or settling abroad." },
        { title: "Follow the list of steps", text: "Each step shows the documents to provide, the expected timeframe and any fees." },
        { title: "Upload and pay online", text: "Send your documents from your phone and pay for each step with Mobile Money, without travelling." },
        { title: "Get notified as you progress", text: "A message at every milestone, and an advisor to answer your questions." },
      ],
      featuresTitle: "Transparent support",
      features: [
        { title: "Fees known in advance", text: "The cost of each step is shown before you commit. No hidden fees." },
        { title: "Your documents protected", text: "Passport, diplomas, statements: your documents are encrypted and every access is logged." },
        { title: "Progress you can see", text: "Your file moves forward in front of you: no more chasing to find out where things stand." },
        { title: "Processes for several countries", text: "Each destination has its own process, kept up to date with current rules." },
      ],
      faqTitle: "Immigration support FAQ",
      faq: [
        { q: "Does Izimboa issue visas?", a: "No. Visas and permits are issued only by the authorities of the destination country. Izimboa helps you prepare and track your file; the final decision is theirs." },
        { q: "Which destinations?", a: "We are starting with the most requested destinations from Cameroon, such as Canada, and will add more over time." },
        { q: "How do payments work?", a: "You pay step by step with Orange Money or MTN Mobile Money. The amount for each step is shown in advance and you get a receipt." },
        { q: "Are my documents safe?", a: "Yes. They are stored encrypted, accessible only to the people handling your file, and every access is recorded." },
      ],
      notice: "Izimboa is not a visa-issuing authority and does not guarantee the outcome of any application. Be wary of anyone who promises you a visa in exchange for payment.",
      cta: {
        title: "Planning a move?",
        text: "Write to us to hear when Izimboa Go opens.",
        button: "Get launch updates",
        subject: "Izimboa Go - launch updates",
      },
    },
    fly: {
      name: "Izimboa Fly",
      navLabel: "Flight tickets",
      cardText: "Book flights from Cameroon and pay with Mobile Money.",
      meta: {
        title: "Flight tickets from Douala and Yaoundé | Izimboa Fly",
        description:
          "Book flights from Cameroon and pay with Orange Money or MTN Mobile Money. A service designed for travellers and for clients going through an immigration process.",
      },
      h1: "Flights from Cameroon, payable with Mobile Money",
      intro:
        "Find a flight, compare, pay without an international bank card: Izimboa Fly makes booking flights from Douala and Yaoundé simple.",
      status: "Coming soon",
      photo: "wing",
      stepsTitle: "How it works",
      steps: [
        { title: "Enter your trip", text: "Departure city, destination, dates and number of passengers." },
        { title: "Compare options", text: "Times, stopovers and included baggage, clearly laid out." },
        { title: "Pay with Mobile Money", text: "Orange Money or MTN Mobile Money: no international bank card needed." },
        { title: "Get your ticket", text: "Your e-ticket by email and on WhatsApp." },
      ],
      featuresTitle: "Why book with Izimboa Fly",
      features: [
        { title: "The payment you already use", text: "Pay for your flight with the Mobile Money on your phone." },
        { title: "Linked to your process", text: "Izimboa Go clients: your ticket fits naturally at the end of your journey." },
        { title: "Real people to help", text: "A change, a baggage question? A team answers you." },
      ],
      faqTitle: "Flight tickets FAQ",
      faq: [
        { q: "When will the service be available?", a: "Izimboa Fly will open after our first services. Write to us to be notified." },
        { q: "Can I pay without a bank card?", a: "Yes, that is one of the goals of the service: pay with Orange Money or MTN Mobile Money." },
      ],
      cta: {
        title: "Travelling soon?",
        text: "Write to us to hear when Izimboa Fly opens.",
        button: "Get launch updates",
        subject: "Izimboa Fly - launch updates",
      },
    },
    buy: {
      name: "Izimboa Buy",
      navLabel: "International shopping",
      cardText: "Shop in France and abroad: Izimboa buys for you and delivers to your door in Cameroon.",
      meta: {
        title: "Shop in France and abroad, delivered to Cameroon | Izimboa Buy",
        description:
          "Found a product in France or elsewhere? Send the link, get an all-inclusive quote (price, shipping, customs), pay with Mobile Money: Izimboa buys it for you and delivers it in Cameroon.",
      },
      h1: "Shop in France and abroad, delivered to Cameroon",
      intro:
        "Books, clothes, spare parts, electronics… You find the product, Izimboa buys it for you, handles shipping and customs, and delivers it to your door.",
      status: "Coming soon",
      photo: "parcel",
      stepsTitle: "How it works",
      steps: [
        { title: "Send the product link", text: "Copy the link from the online shop where you found the item." },
        { title: "Get an all-inclusive quote", text: "Item price, shipping and customs fees: one amount, known in advance." },
        { title: "Pay with Mobile Money", text: "Orange Money or MTN Mobile Money, no international bank card needed." },
        { title: "Izimboa buys and delivers", text: "We place the order, track the shipment and deliver to your home." },
      ],
      featuresTitle: "Shopping abroad, without the hassle",
      features: [
        { title: "One final price, no surprises", text: "The quote covers the item, shipping and customs. What you pay is what you pay." },
        { title: "No international bank card", text: "You pay in CFA francs, from your phone." },
        { title: "Delivered to your door", text: "Final delivery is handled by Izimboa Delivery, with tracking and proof of handover." },
      ],
      faqTitle: "International shopping FAQ",
      faq: [
        { q: "Which websites can I order from?", a: "Most online shops, starting with France. We check every request before sending the quote." },
        { q: "Can I order medicines?", a: "Medicines and some other products are subject to strict import rules. We check every item and decline anything that is not allowed." },
        { q: "How long does delivery take?", a: "The estimated timeframe is shown in the quote, depending on the country of purchase and the shipping method." },
      ],
      cta: {
        title: "A product waiting for you abroad?",
        text: "Write to us to hear when Izimboa Buy opens.",
        button: "Get launch updates",
        subject: "Izimboa Buy - launch updates",
      },
    },
  },
  about: {
    meta: {
      title: "About Izimboa: our mission in Central Africa",
      description:
        "Izimboa was born in Cameroon to make moving parcels and people simple, tracked and reliable, starting in Douala and then across Central Africa.",
    },
    h1: "Making movement simple and reliable",
    lead:
      "Sending a parcel, preparing to move abroad, buying from overseas: in Cameroon, these often mean too many calls, too much waiting and too much uncertainty. Izimboa was born to change that.",
    sections: [
      {
        title: "Our mission",
        text: "To move parcels and support people with the same standard: a clear price, tracking at every step and a team that answers.",
      },
      {
        title: "Our vision",
        text: "To become the reference for movement in Central Africa. We are starting in Douala, then opening the CEMAC countries one by one, with services adapted to each.",
      },
      {
        title: "Our approach",
        text: "One platform for all our services: a single account, the payment methods you already use, and technology designed to work even on a weak connection.",
      },
    ],
    valuesTitle: "Our values",
    values: [
      { title: "Trust", text: "Every parcel, every file and every franc is tracked." },
      { title: "Transparency", text: "Prices and steps are known before you commit." },
      { title: "Proximity", text: "A local team you can reach, in English and French." },
    ],
  },
  contact: {
    meta: {
      title: "Contact Izimboa in Cameroon",
      description: "A question, a partnership, want to become a rider? Contact the Izimboa team in Cameroon by email.",
    },
    h1: "Let's talk about your project",
    lead: "Pick the topic that fits: your message will go straight to the right person.",
    channels: [
      { title: "Sellers and merchants", text: "Get your customers delivered in Douala, with cash on delivery paid out the same day.", subject: "Partner seller", button: "Write to the sellers team" },
      { title: "Become a rider", text: "Do you have a motorbike and know Douala? Join the Izimboa riders team.", subject: "Rider application", button: "Apply" },
      { title: "Immigration and travel", text: "A question about Izimboa Go or Izimboa Fly? We will answer.", subject: "Immigration or travel question", button: "Ask a question" },
      { title: "Partners and press", text: "Agencies, businesses, journalists: let's talk.", subject: "Partnership or press", button: "Write to us" },
    ],
    directTitle: "Reach us directly",
    emailLabel: "Email",
    whatsappLabel: "WhatsApp",
    responseNote: "We answer messages Monday to Saturday.",
  },
  legal: {
    meta: {
      title: "Legal notice | Izimboa",
      description: "Legal notice for izimboa.com.",
    },
    h1: "Legal notice",
    sections: [
      { title: "Publisher", text: "[Company name, legal form, share capital, registered address, RCCM number, taxpayer number, publication director: to be completed]" },
      { title: "Contact", text: "contact@izimboa.com" },
      { title: "Hosting", text: "[Hosting provider name and address: to be completed]" },
      { title: "Personal data", text: "Data sent through this site is used only to answer your requests. In accordance with Cameroonian personal data protection law, you may request access to, correction or deletion of your data by writing to contact@izimboa.com." },
      { title: "Photo credits", text: "Photographs from Unsplash, used under the Unsplash licence. Authors are credited under each image." },
    ],
  },
  privacy: {
    meta: {
      title: "Privacy policy | Izimboa",
      description: "How Izimboa collects, uses and protects your data: website, seller space and the Izimboa Livreur app.",
    },
    h1: "Privacy policy",
    sections: [
      { title: "Who we are", text: "Izimboa is a delivery, immigration, travel and shopping services platform based in Cameroon. This policy covers the izimboa.com website, the delivery.izimboa.com seller space and the \"Izimboa Livreur\" Android app. Contact: contact@izimboa.com." },
      { title: "Data we collect", text: "Account: name, e-mail address or phone number, and your account role (customer, seller, courier).\n\nDeliveries: pickup and drop-off addresses and points, recipient name and phone, status and history of the delivery.\n\nLocation (Izimboa Livreur app only): the phone's GPS location, only while the courier is \"online\" or on a delivery, including when the app is in the background. No location is collected while the courier is offline.\n\nDiagnostics: phone model and Android version, app version and, if the app crashes, the technical error report. These reports contain no location and no personal content." },
      { title: "Why we use it", text: "To offer a delivery to the nearest available courier, compute price and distance, let the sender and recipient track the parcel live, prove the handover (handover code and route travelled), handle complaints, prevent fraud and fix app errors. We do not sell your data and do not use it for advertising." },
      { title: "Sharing", text: "The courier's location and route since pickup are visible to the sender and recipient of the current delivery through the tracking link. The courier sees the addresses and phone numbers needed for the delivery. Our technical providers (server hosting, authentication, OpenStreetMap map tiles) process data only to run the service. Data may be disclosed to authorities where required by law." },
      { title: "Security", text: "Traffic is encrypted (HTTPS). On the courier's phone, data waiting to be sent is kept in an encrypted database whose key is protected by the phone. Server-side access is limited to people who need it." },
      { title: "Retention", text: "Detailed courier locations: 90 days at most, unless a dispute is open. Delivery history: as long as accounting and legal obligations require. Diagnostic reports: 90 days. Account: while active, then deleted on request." },
      { id: "account-deletion", title: "Delete your account and data", text: "You can ask for your Izimboa account (including your courier account) and its data to be deleted by writing to contact@izimboa.com from the e-mail or phone linked to the account, with the subject \"Account deletion\". We confirm deletion within 30 days. Data we are legally required to keep (invoices, for example) is kept for the legal period and then deleted.\n\nIn the Izimboa Livreur app, \"Se déconnecter\" (sign out) also erases all data kept on the phone." },
      { title: "Your rights", text: "Under Cameroonian personal data protection law, you may request access to, correction or deletion of your data, or object to its processing, by writing to contact@izimboa.com. Couriers can stop location collection at any time by going offline." },
      { title: "Minors", text: "Izimboa services and the Izimboa Livreur app are not intended for people under 18." },
      { title: "Changes", text: "This policy may change. The current version is always published on this page. Last updated: 28 October 2026." },
    ],
  },
};

export default en;
