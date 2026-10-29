package com.izimboa.delivery.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Recherche de lieux au Cameroun (quartiers, rues, lieux connus) dans OpenStreetMap, via le service public
 * Nominatim, en attendant notre propre moteur (lot 5). Règles d'usage respectées : une requête par seconde
 * au plus, identification claire, résultats gardés en mémoire 24 h.
 */
@Component
public class PlaceSearch {

    public record Place(String name, String detail, double lat, double lng) {}

    private static final Logger log = LoggerFactory.getLogger(PlaceSearch.class);
    private static final String ENDPOINT = "https://nominatim.openstreetmap.org/search";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final int CACHE_MAX = 2_000;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
    private final ObjectMapper json = new ObjectMapper();
    private final Map<String, Cached> cache = new LinkedHashMap<>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Cached> eldest) {
            return size() > CACHE_MAX;
        }
    };
    private long lastCallNanos;

    private record Cached(List<Place> places, Instant at) {}

    /** Lieux qui correspondent, les plus proches de {@code near} en premier (vide si le service ne répond pas). */
    public List<Place> search(String query, GeoPoint near) {
        String q = query == null ? "" : query.strip().replaceAll("\\s+", " ");
        if (q.length() < 3 || q.length() > 120) return List.of();
        String key = q.toLowerCase(Locale.ROOT) + "|" + (near == null ? "" : Math.round(near.lat() * 10) + "," + Math.round(near.lng() * 10));
        synchronized (cache) {
            var hit = cache.get(key);
            if (hit != null && hit.at().plus(CACHE_TTL).isAfter(Instant.now())) return hit.places();
        }
        var places = fetch(q, near);
        if (places != null) {
            synchronized (cache) {
                cache.put(key, new Cached(places, Instant.now()));
            }
        }
        return places == null ? List.of() : places;
    }

    private List<Place> fetch(String q, GeoPoint near) {
        if (!takeTurn()) return null;
        StringBuilder url = new StringBuilder(ENDPOINT)
                .append("?format=jsonv2&addressdetails=1&limit=8&countrycodes=cm&accept-language=fr&q=")
                .append(URLEncoder.encode(q, StandardCharsets.UTF_8));
        if (near != null) {
            double d = 0.25; // ≈ 25 km autour : la ville du client en priorité, sans exclure le reste
            url.append("&viewbox=").append(near.lng() - d).append(',').append(near.lat() + d)
                    .append(',').append(near.lng() + d).append(',').append(near.lat() - d);
        }
        try {
            var response = http.send(HttpRequest.newBuilder(URI.create(url.toString()))
                    .timeout(Duration.ofSeconds(6))
                    .header("User-Agent", "Izimboa-Delivery/1.0 (contact@izimboa.com)")
                    .header("Referer", "https://delivery.izimboa.com/")
                    .GET().build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.info("Recherche de lieux : HTTP {}", response.statusCode());
                return null;
            }
            List<Place> out = new ArrayList<>();
            for (JsonNode n : json.readTree(response.body())) {
                double lat = n.path("lat").asDouble(), lng = n.path("lon").asDouble();
                String name = n.path("name").asText("");
                String display = n.path("display_name").asText("");
                if (name.isBlank()) name = display.split(",")[0].strip();
                String detail = display.startsWith(name) ? display.substring(name.length()).replaceFirst("^,\\s*", "") : display;
                detail = detail.replaceAll(",\\s*(Cameroun|Cameroon)$", "");
                out.add(new Place(name, detail, lat, lng));
            }
            if (near != null) {
                out.sort((a, b) -> Double.compare(near.straightLineMetersTo(new GeoPoint(a.lat(), a.lng())),
                        near.straightLineMetersTo(new GeoPoint(b.lat(), b.lng()))));
            }
            return out;
        } catch (Exception e) {
            log.info("Recherche de lieux indisponible : {}", e.getMessage());
            return null;
        }
    }

    /** Une requête par seconde au plus vers le service public (attente courte, sinon on renonce). */
    private synchronized boolean takeTurn() {
        long now = System.nanoTime();
        long wait = lastCallNanos + 1_000_000_000L - now;
        if (wait > 1_500_000_000L) return false;
        if (wait > 0) {
            try {
                Thread.sleep(wait / 1_000_000L + 1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        lastCallNanos = System.nanoTime();
        return true;
    }
}
