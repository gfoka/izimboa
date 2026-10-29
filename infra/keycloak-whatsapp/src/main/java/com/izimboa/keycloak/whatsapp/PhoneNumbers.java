package com.izimboa.keycloak.whatsapp;

/** Numéros de téléphone : normalisation au format international (E.164) et masquage pour l'affichage. */
public final class PhoneNumbers {

    private PhoneNumbers() {}

    /**
     * Ramène un numéro saisi au format « +237699123456 ». Un numéro local à 9 chiffres est considéré camerounais.
     * Renvoie {@code null} si le numéro n'est pas plausible.
     */
    public static String normalize(String input) {
        if (input == null) return null;
        String s = input.strip().replaceAll("[\\s.\\-()/]", "");
        if (s.startsWith("00")) s = "+" + s.substring(2);
        String digits;
        if (s.startsWith("+")) {
            digits = s.substring(1);
        } else if (s.startsWith("237") && s.length() == 12) {
            digits = s;
        } else if (s.length() == 9) {
            digits = "237" + s;
        } else {
            return null;
        }
        if (!digits.matches("[1-9][0-9]{7,14}")) return null;
        // Cameroun : 9 chiffres après l'indicatif, mobiles en 6, fixes en 2.
        if (digits.startsWith("237") && !digits.substring(3).matches("[26][0-9]{8}")) return null;
        return "+" + digits;
    }

    /** « +237699123456 » → « +237 6•• ••• 456 » : assez pour reconnaître son numéro, pas assez pour le deviner. */
    public static String mask(String e164) {
        if (e164 == null || e164.length() < 8) return "";
        if (e164.startsWith("+237") && e164.length() == 13) {
            return "+237 " + e164.charAt(4) + "•• ••• " + e164.substring(10);
        }
        return e164.substring(0, 4) + "•".repeat(e164.length() - 7) + e164.substring(e164.length() - 3);
    }

    /** Identifiant de compte dérivé du numéro : « tel-237699123456 ». */
    public static String username(String e164) {
        return "tel-" + e164.substring(1);
    }
}
