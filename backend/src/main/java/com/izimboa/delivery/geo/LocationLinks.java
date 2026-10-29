package com.izimboa.delivery.geo;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lit des coordonnées dans ce que le vendeur colle : lien Google Maps (partagé depuis WhatsApp ou Google Maps),
 * lien OpenStreetMap ou Apple Plans, « geo: », ou simplement « 4.0612, 9.7421 ».
 * Les liens courts (maps.app.goo.gl) sont d'abord déroulés par {@link LocationLinkResolver}.
 * Aucune donnée n'est demandée à Google : on lit seulement le lien que le client a lui-même partagé.
 */
public final class LocationLinks {

    private LocationLinks() {}

    private static final String NUM = "(-?\\d{1,3}(?:\\.\\d+)?)";
    private static final String SEP = "\\s*(?:,|%2C|\\+|\\s)\\s*\\+?";

    /** Du plus précis au moins précis : le lieu choisi (!3d!4d), puis les paramètres, puis le centre de la vue (@). */
    private static final List<Pattern> PATTERNS = List.of(
            Pattern.compile("!3d" + NUM + "!4d" + NUM),
            Pattern.compile("[?&](?:q|query|ll|sll|daddr|destination|center|mlat)=(?:loc:)?" + NUM + SEP + NUM, Pattern.CASE_INSENSITIVE),
            Pattern.compile("/(?:place|search|dir/[^/]*)/" + NUM + SEP + NUM),
            Pattern.compile("geo:" + NUM + "," + NUM, Pattern.CASE_INSENSITIVE),
            Pattern.compile("#map=\\d{1,2}/" + NUM + "/" + NUM),
            Pattern.compile("@" + NUM + "," + NUM));

    private static final Pattern MLAT = Pattern.compile("[?&]mlat=" + NUM + "&mlon=" + NUM);
    private static final Pattern PLAIN = Pattern.compile("^\\s*" + NUM + "\\s*[,;\\s]\\s*" + NUM + "\\s*$");
    private static final Pattern DMS = Pattern.compile(
            "(\\d{1,3})°\\s*(\\d{1,2})['’′]\\s*(\\d{1,2}(?:[.,]\\d+)?)[\"”″]?\\s*([NS])[\\s,+]*"
                    + "(\\d{1,3})°\\s*(\\d{1,2})['’′]\\s*(\\d{1,2}(?:[.,]\\d+)?)[\"”″]?\\s*([EW])");
    private static final Pattern URL = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);

    /** Premier lien trouvé dans un texte (ex. « Voici ma position https://maps.app.goo.gl/… »). */
    public static Optional<String> firstUrl(String text) {
        if (text == null) return Optional.empty();
        Matcher m = URL.matcher(text);
        return m.find() ? Optional.of(m.group().replaceAll("[).,;!?»]+$", "")) : Optional.empty();
    }

    /** Coordonnées contenues dans un texte ou un lien (déjà déroulé), si elles sont valides. */
    public static Optional<GeoPoint> parse(String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        String s = text.strip();
        String decoded = URLDecoder.decode(s.replace("+", "%2B"), StandardCharsets.UTF_8);
        for (String candidate : List.of(s, decoded)) {
            Matcher plain = PLAIN.matcher(candidate);
            if (plain.matches()) return valid(plain.group(1), plain.group(2));
            Matcher osm = MLAT.matcher(candidate);
            if (osm.find()) return valid(osm.group(1), osm.group(2));
            for (Pattern p : PATTERNS) {
                Matcher m = p.matcher(candidate);
                if (m.find()) {
                    var point = valid(m.group(1), m.group(2));
                    if (point.isPresent()) return point;
                }
            }
            Matcher dms = DMS.matcher(candidate);
            if (dms.find()) {
                double lat = dms(dms.group(1), dms.group(2), dms.group(3), "S".equals(dms.group(4)));
                double lng = dms(dms.group(5), dms.group(6), dms.group(7), "W".equals(dms.group(8)));
                return valid(lat, lng);
            }
        }
        return Optional.empty();
    }

    private static double dms(String d, String m, String s, boolean negative) {
        double v = Double.parseDouble(d) + Double.parseDouble(m) / 60 + Double.parseDouble(s.replace(',', '.')) / 3600;
        return negative ? -v : v;
    }

    private static Optional<GeoPoint> valid(String lat, String lng) {
        try {
            return valid(Double.parseDouble(lat), Double.parseDouble(lng));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Optional<GeoPoint> valid(double lat, double lng) {
        if (Math.abs(lat) > 90 || Math.abs(lng) > 180 || (lat == 0 && lng == 0)) return Optional.empty();
        return Optional.of(new GeoPoint(round(lat), round(lng)));
    }

    private static double round(double v) {
        return Math.round(v * 1e6) / 1e6;
    }
}
