package com.izimboa.common.contact;

import com.izimboa.common.Phones;
import com.izimboa.common.mail.Mailer;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Formulaires du site : la demande est d'abord enregistrée (rien ne se perd), puis l'équipe est prévenue
 * par e-mail et le demandeur reçoit un accusé de réception s'il a donné son adresse.
 */
@Service
class ContactService {

    static final int MAX_PER_HOUR = 5;
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("Africa/Douala"));

    private final ContactRequestRepository repository;
    private final Mailer mailer;
    private final Clock clock;
    private final String teamEmail;
    private final Map<String, Deque<Instant>> recent = new ConcurrentHashMap<>();

    ContactService(ContactRequestRepository repository, Mailer mailer, Clock clock,
                   @Value("${izimboa.mail.team}") String teamEmail) {
        this.repository = repository;
        this.mailer = mailer;
        this.clock = clock;
        this.teamEmail = teamEmail;
    }

    record Input(String product, String kind, String company, String contactName, String phone, String email,
                 String city, String activity, String volume, String message, String lang) {}

    @Transactional
    ContactRequest submit(Input in, String clientKey) {
        Instant now = clock.instant();
        throttle(clientKey, now);
        String phone = Phones.normalize(in.phone()).orElseThrow(() -> ContactProblem.invalid("Numéro de téléphone invalide."));
        String lang = "en".equals(in.lang()) ? "en" : "fr";
        ContactRequest r = repository.save(new ContactRequest(now, in.product(), in.kind(), in.company().trim(),
                in.contactName().trim(), phone, blankToNull(in.email()), blankToNull(in.city()), blankToNull(in.activity()),
                blankToNull(in.volume()), blankToNull(in.message()), lang));
        Thread.ofVirtual().start(() -> notify(r));
        return r;
    }

    private void throttle(String key, Instant now) {
        Deque<Instant> times = recent.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (times) {
            while (!times.isEmpty() && times.peekFirst().isBefore(now.minusSeconds(3600))) times.pollFirst();
            if (times.size() >= MAX_PER_HOUR) throw ContactProblem.tooMany();
            times.addLast(now);
        }
        if (recent.size() > 10_000) recent.clear(); // garde-fou mémoire
    }

    private void notify(ContactRequest r) {
        String lines = """
                Nouvelle demande %s (%s) reçue le %s

                Entreprise : %s
                Contact : %s
                Téléphone : %s
                E-mail : %s
                Ville : %s
                Activité : %s
                Colis par mois : %s

                Message :
                %s

                Référence : %s
                """.formatted(r.getKind(), r.getProduct(), WHEN.format(r.getCreatedAt()), r.getCompany(), r.getContactName(),
                r.getPhone(), dash(r.getEmail()), dash(r.getCity()), dash(r.getActivity()), dash(r.getVolume()),
                dash(r.getMessage()), r.getId());
        mailer.send(new Mailer.Message(teamEmail, "Izimboa", r.getEmail(),
                "[Izimboa " + r.getProduct() + "] " + r.getCompany() + " - " + r.getContactName(), lines));
        if (r.getEmail() != null) mailer.send(acknowledgement(r));
    }

    private Mailer.Message acknowledgement(ContactRequest r) {
        boolean en = "en".equals(r.getLang());
        String subject = en ? "Izimboa Delivery: we received your request" : "Izimboa Delivery : nous avons bien reçu votre demande";
        String text = en ? """
                Hello %s,

                Thank you for your interest in Izimboa Delivery. We have received your request for %s.
                A member of our team will call you on %s within one business day to set up your seller account and your first deliveries.

                You can already create your seller account and see our prices: https://delivery.izimboa.com

                The Izimboa team
                %s
                """ : """
                Bonjour %s,

                Merci de votre intérêt pour Izimboa Delivery. Nous avons bien reçu votre demande pour %s.
                Un membre de notre équipe vous appelle au %s sous un jour ouvré pour ouvrir votre compte vendeur et organiser vos premières livraisons.

                Vous pouvez déjà créer votre compte vendeur et voir nos prix : https://delivery.izimboa.com

                L'équipe Izimboa
                %s
                """;
        return new Mailer.Message(r.getEmail(), r.getContactName(), teamEmail, subject,
                text.formatted(r.getContactName(), r.getCompany(), r.getPhone(), teamEmail));
    }

    @Transactional(readOnly = true)
    java.util.List<ContactRequest> latest(int limit) {
        return repository.findAllByOrderByCreatedAtDesc(org.springframework.data.domain.PageRequest.of(0, limit));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String dash(String s) {
        return s == null ? "-" : s;
    }
}
