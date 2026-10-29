package com.izimboa.keycloak.whatsapp;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * Connexion par code WhatsApp (décision 40).
 * Étape 1 : la personne saisit son numéro, Keycloak envoie un code à 6 chiffres par WhatsApp.
 * Étape 2 : elle saisit le code ; son compte est retrouvé par le numéro, ou créé s'il n'existe pas.
 * Garde-fous : code valable 10 min, 5 essais, renvoi après 60 s (3 envois par tentative de connexion),
 * 5 codes par heure et par numéro, 20 par heure et par adresse IP (coût des messages, abus).
 */
// Attributs « phoneNumber » / « phoneNumberVerified » : ceux que lit la portée standard « phone » de Keycloak
// (claims phone_number et phone_number_verified des jetons).
public class WhatsAppOtpAuthenticator implements Authenticator {

    static final String ATTR_PHONE = "phoneNumber";
    static final String ATTR_PHONE_VERIFIED = "phoneNumberVerified";

    private static final Logger LOG = Logger.getLogger(WhatsAppOtpAuthenticator.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    static final int CODE_TTL_SECONDS = 600;
    static final int MAX_TRIES = 5;
    static final int RESEND_AFTER_SECONDS = 60;
    static final int MAX_SENDS_PER_LOGIN = 3;
    static final int MAX_PER_PHONE_PER_HOUR = 5;
    static final int MAX_PER_IP_PER_HOUR = 20;

    private static final String N_PHONE = "izimboa.wa.phone";
    private static final String N_HASH = "izimboa.wa.hash";
    private static final String N_EXPIRES = "izimboa.wa.expires";
    private static final String N_TRIES = "izimboa.wa.tries";
    private static final String N_SENT_AT = "izimboa.wa.sentAt";
    private static final String N_SENDS = "izimboa.wa.sends";

    private final WhatsAppClient whatsApp;

    public WhatsAppOtpAuthenticator(WhatsAppClient whatsApp) {
        this.whatsApp = whatsApp;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        if (context.getAuthenticationSession().getAuthNote(N_HASH) != null) {
            context.challenge(codeForm(context, null, null));
        } else {
            context.challenge(phoneForm(context, null, null));
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> form = context.getHttpRequest().getDecodedFormParameters();
        AuthenticationSessionModel auth = context.getAuthenticationSession();
        if (form.containsKey("changePhone")) {
            clear(auth);
            context.challenge(phoneForm(context, null, null));
        } else if (form.containsKey("resend")) {
            resend(context);
        } else if (auth.getAuthNote(N_HASH) == null || form.containsKey("phone")) {
            sendFirstCode(context, form.getFirst("phone"));
        } else {
            checkCode(context, form.getFirst("code"));
        }
    }

    private void sendFirstCode(AuthenticationFlowContext context, String rawPhone) {
        String phone = PhoneNumbers.normalize(rawPhone);
        if (phone == null) {
            context.challenge(phoneForm(context, "phone", "waPhoneInvalid"));
            return;
        }
        AuthenticationSessionModel auth = context.getAuthenticationSession();
        clear(auth);
        auth.setAuthNote(N_PHONE, phone);
        auth.setAuthNote(N_SENDS, "0");
        String error = send(context, phone);
        if (error != null) {
            auth.removeAuthNote(N_PHONE);
            context.challenge(phoneForm(context, "phone", error));
            return;
        }
        context.challenge(codeForm(context, null, null));
    }

    private void resend(AuthenticationFlowContext context) {
        AuthenticationSessionModel auth = context.getAuthenticationSession();
        String phone = auth.getAuthNote(N_PHONE);
        if (phone == null) {
            context.challenge(phoneForm(context, null, null));
            return;
        }
        long sentAt = parseLong(auth.getAuthNote(N_SENT_AT));
        if (now() - sentAt < RESEND_AFTER_SECONDS) {
            context.challenge(codeForm(context, "code", "waResendTooSoon"));
            return;
        }
        if (parseLong(auth.getAuthNote(N_SENDS)) >= MAX_SENDS_PER_LOGIN) {
            context.challenge(codeForm(context, "code", "waTooManySends"));
            return;
        }
        String error = send(context, phone);
        if (error == null) {
            context.challenge(codeForm(context, null, null, "waCodeResent"));
        } else {
            context.challenge(codeForm(context, "code", error));
        }
    }

    /** Envoie un nouveau code ; renvoie la clé du message d'erreur, ou {@code null} si tout va bien. */
    private String send(AuthenticationFlowContext context, String phone) {
        KeycloakSession session = context.getSession();
        String ip = context.getConnection() == null ? "?" : context.getConnection().getRemoteAddr();
        if (!take(session, "phone:" + phone, MAX_PER_PHONE_PER_HOUR) || !take(session, "ip:" + ip, MAX_PER_IP_PER_HOUR)) {
            context.getEvent().detail("reason", "whatsapp_rate_limit").error(Errors.SLOW_DOWN);
            return "waTooManySends";
        }
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        String language = language(session);
        if (!whatsApp.sendCode(phone, code, language)) {
            return "waSendFailed";
        }
        AuthenticationSessionModel auth = context.getAuthenticationSession();
        auth.setAuthNote(N_HASH, hash(phone, code));
        auth.setAuthNote(N_EXPIRES, String.valueOf(now() + CODE_TTL_SECONDS));
        auth.setAuthNote(N_TRIES, "0");
        auth.setAuthNote(N_SENT_AT, String.valueOf(now()));
        auth.setAuthNote(N_SENDS, String.valueOf(parseLong(auth.getAuthNote(N_SENDS)) + 1));
        return null;
    }

    private void checkCode(AuthenticationFlowContext context, String rawCode) {
        AuthenticationSessionModel auth = context.getAuthenticationSession();
        String phone = auth.getAuthNote(N_PHONE);
        String code = rawCode == null ? "" : rawCode.replaceAll("\\D", "");
        if (now() > parseLong(auth.getAuthNote(N_EXPIRES))) {
            context.challenge(codeForm(context, "code", "waCodeExpired"));
            return;
        }
        int tries = (int) parseLong(auth.getAuthNote(N_TRIES)) + 1;
        auth.setAuthNote(N_TRIES, String.valueOf(tries));
        boolean ok = code.length() == 6 && MessageDigest.isEqual(
                hash(phone, code).getBytes(StandardCharsets.UTF_8),
                auth.getAuthNote(N_HASH).getBytes(StandardCharsets.UTF_8));
        if (!ok) {
            context.getEvent().detail("reason", "whatsapp_code_invalid").error(Errors.INVALID_USER_CREDENTIALS);
            if (tries >= MAX_TRIES) {
                clear(auth);
                context.challenge(phoneForm(context, "phone", "waTooManyTries"));
            } else {
                context.challenge(codeForm(context, "code", "waCodeInvalid"));
            }
            return;
        }
        UserModel user = findOrCreateUser(context.getSession(), context.getRealm(), phone);
        if (!user.isEnabled()) {
            clear(auth);
            context.getEvent().user(user).error(Errors.USER_DISABLED);
            context.challenge(phoneForm(context, "phone", "waAccountDisabled"));
            return;
        }
        clear(auth);
        context.setUser(user);
        context.success();
    }

    static UserModel findOrCreateUser(KeycloakSession session, RealmModel realm, String phone) {
        UserModel user = session.users().searchForUserByUserAttributeStream(realm, ATTR_PHONE, phone)
                .findFirst()
                .orElseGet(() -> session.users().getUserByUsername(realm, PhoneNumbers.username(phone)));
        if (user == null) {
            user = session.users().addUser(realm, PhoneNumbers.username(phone));
            user.setEnabled(true);
            LOG.infof("Compte créé par code WhatsApp : %s", PhoneNumbers.mask(phone));
        }
        if (!phone.equals(user.getFirstAttribute(ATTR_PHONE))) user.setSingleAttribute(ATTR_PHONE, phone);
        if (!"true".equals(user.getFirstAttribute(ATTR_PHONE_VERIFIED))) user.setSingleAttribute(ATTR_PHONE_VERIFIED, "true");
        return user;
    }

    /** Compteur glissant d'une heure partagé entre les nœuds Keycloak. */
    private static boolean take(KeycloakSession session, String key, int max) {
        SingleUseObjectProvider store = session.singleUseObjects();
        String k = "izimboa-wa-" + key;
        Map<String, String> current = store.get(k);
        int n = current == null ? 0 : (int) parseLong(current.get("n"));
        if (n >= max) return false;
        if (current == null) {
            store.put(k, 3600, Map.of("n", "1"));
        } else {
            store.replace(k, Map.of("n", String.valueOf(n + 1)));
        }
        return true;
    }

    private static String language(KeycloakSession session) {
        Locale locale = session.getContext().resolveLocale(null);
        return locale != null && "en".equals(locale.getLanguage()) ? "en" : "fr";
    }

    private Response phoneForm(AuthenticationFlowContext context, String field, String error) {
        LoginFormsProvider form = prepare(context);
        if (error != null) form.addError(new FormMessage(field, error));
        String phone = context.getHttpRequest().getDecodedFormParameters().getFirst("phone");
        if (phone != null) form.setAttribute("waPhoneValue", phone);
        if (!whatsApp.configured()) form.setAttribute("waUnavailable", true);
        return form.createForm("whatsapp-phone.ftl");
    }

    private Response codeForm(AuthenticationFlowContext context, String field, String error) {
        return codeForm(context, field, error, null);
    }

    private Response codeForm(AuthenticationFlowContext context, String field, String error, String info) {
        LoginFormsProvider form = prepare(context);
        if (error != null) form.addError(new FormMessage(field, error));
        if (info != null) form.setInfo(info);
        form.setAttribute("waPhone", masked(context));
        form.setAttribute("waResendAfter", Math.max(0, RESEND_AFTER_SECONDS - (now() - parseLong(context.getAuthenticationSession().getAuthNote(N_SENT_AT)))));
        return form.createForm("whatsapp-code.ftl");
    }

    private static LoginFormsProvider prepare(AuthenticationFlowContext context) {
        return context.form();
    }

    private static String masked(AuthenticationFlowContext context) {
        return PhoneNumbers.mask(context.getAuthenticationSession().getAuthNote(N_PHONE));
    }

    private static void clear(AuthenticationSessionModel auth) {
        for (String n : new String[] {N_PHONE, N_HASH, N_EXPIRES, N_TRIES, N_SENT_AT, N_SENDS}) auth.removeAuthNote(n);
    }

    static String hash(String phone, String code) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest((phone + ":" + code).getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(d);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static long now() {
        return Instant.now().getEpochSecond();
    }

    private static long parseLong(String s) {
        try {
            return s == null ? 0 : Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {}

    @Override
    public void close() {}
}
