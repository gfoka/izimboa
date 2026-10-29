package com.izimboa.delivery.geo;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Déroule les liens courts de localisation (maps.app.goo.gl, goo.gl/maps) pour lire les coordonnées
 * de l'adresse complète. Seuls quelques domaines de cartes sont suivis (jamais une adresse quelconque).
 */
@Component
public class LocationLinkResolver {

    private static final Logger log = LoggerFactory.getLogger(LocationLinkResolver.class);
    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "maps.app.goo.gl", "goo.gl", "g.co", "maps.google.com", "www.google.com", "google.com",
            "maps.google.cm", "www.google.cm", "google.cm", "consent.google.com", "www.google.fr", "maps.google.fr");
    private static final int MAX_HOPS = 6;
    private static final Pattern ANY_MAP_URL = Pattern.compile("https://(?:www\\.)?google\\.[a-z.]+/maps[^\"'\\s<>]+");

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /** Coordonnées lues dans le texte collé : directement, ou après avoir déroulé un lien court. */
    public Optional<GeoPoint> resolve(String text) {
        var direct = LocationLinks.parse(text);
        if (direct.isPresent()) return direct;
        var url = LocationLinks.firstUrl(text);
        if (url.isEmpty()) return Optional.empty();
        var fromUrl = LocationLinks.parse(url.get());
        if (fromUrl.isPresent()) return fromUrl;
        return follow(url.get());
    }

    private Optional<GeoPoint> follow(String start) {
        String current = start;
        for (int hop = 0; hop < MAX_HOPS; hop++) {
            URI uri;
            try {
                uri = URI.create(current);
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || !ALLOWED_HOSTS.contains(uri.getHost().toLowerCase())) {
                return Optional.empty();
            }
            // Page de consentement (serveur en Europe) : la vraie adresse est dans « continue ».
            if ("consent.google.com".equalsIgnoreCase(uri.getHost())) {
                var cont = queryParam(uri.getRawQuery(), "continue");
                if (cont.isEmpty()) return Optional.empty();
                current = cont.get();
                var p = LocationLinks.parse(current);
                if (p.isPresent()) return p;
                continue;
            }
            try {
                var response = http.send(HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(6))
                        .header("User-Agent", "Mozilla/5.0 (compatible; IzimboaBot/1.0; +https://izimboa.com)")
                        .GET().build(), HttpResponse.BodyHandlers.ofString());
                var location = response.headers().firstValue("location");
                if (response.statusCode() / 100 == 3 && location.isPresent()) {
                    current = uri.resolve(location.get()).toString();
                    var p = LocationLinks.parse(current);
                    if (p.isPresent()) return p;
                    continue;
                }
                // Certaines pages renvoient l'adresse complète dans leur contenu.
                Matcher m = ANY_MAP_URL.matcher(response.body() == null ? "" : response.body());
                while (m.find()) {
                    var p = LocationLinks.parse(m.group().replace("\\u0026", "&").replace("&amp;", "&"));
                    if (p.isPresent()) return p;
                }
                return Optional.empty();
            } catch (IOException e) {
                log.info("Lien de localisation injoignable : {}", e.getMessage());
                return Optional.empty();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static Optional<String> queryParam(String rawQuery, String name) {
        if (rawQuery == null) return Optional.empty();
        for (String part : rawQuery.split("&")) {
            int i = part.indexOf('=');
            if (i > 0 && part.substring(0, i).equals(name)) {
                return Optional.of(URLDecoder.decode(part.substring(i + 1), StandardCharsets.UTF_8));
            }
        }
        return Optional.empty();
    }
}
