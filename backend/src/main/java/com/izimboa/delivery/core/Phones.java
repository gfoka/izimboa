package com.izimboa.delivery.core;

import java.util.Optional;
import java.util.regex.Pattern;

/** Numéros de téléphone au format international (E.164), Cameroun par défaut. */
final class Phones {

    private static final Pattern E164 = Pattern.compile("\\+[1-9]\\d{7,14}");

    private Phones() {}

    /** « 6 99 12 34 56 », « 00237699123456 », « +237 699-12-34-56 » → « +237699123456 ». */
    static Optional<String> normalize(String raw) {
        if (raw == null) return Optional.empty();
        String s = raw.trim();
        boolean plus = s.startsWith("+");
        String digits = s.replaceAll("\\D", "");
        if (!plus && digits.startsWith("00")) { digits = digits.substring(2); plus = true; }
        if (!plus && digits.length() == 9 && (digits.startsWith("6") || digits.startsWith("2"))) {
            digits = "237" + digits;
            plus = true;
        }
        if (!plus && digits.startsWith("237") && digits.length() == 12) plus = true;
        String result = "+" + digits;
        return plus && E164.matcher(result).matches() ? Optional.of(result) : Optional.empty();
    }
}
