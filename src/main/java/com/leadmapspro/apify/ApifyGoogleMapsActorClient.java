package com.leadmapspro.apify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.leadmapspro.config.LeadMapsProperties;
import com.leadmapspro.scrape.RawLeadRow;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Dispara o Google Maps Scraper (Actor) na Apify e converte o dataset em {@link RawLeadRow}.
 * Input alinhado ao JSON padrão do Actor (compass e forks).
 */
@Service
public class ApifyGoogleMapsActorClient {

    private static final Logger log = LoggerFactory.getLogger(ApifyGoogleMapsActorClient.class);

    private static final String API_BASE = "https://api.apify.com/v2";

    private final LeadMapsProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public ApifyGoogleMapsActorClient(LeadMapsProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(API_BASE).build();
    }

    public List<RawLeadRow> scrape(String keyword, String location, int maxResults) {
        LeadMapsProperties.Apify ap = properties.getApify();
        if (!ap.isConfigured()) {
            throw new IllegalStateException("APIFY_TOKEN não configurado.");
        }
        int max = Math.min(Math.max(1, maxResults), 80);
        String actorPath = normalizeActorPathSegment(ap.getActorId());
        ObjectNode input = buildActorInput(keyword.trim(), location.trim(), max, ap.getLanguage());
        log.info("Apify: iniciando run do actor [{}]", actorPath);

        String runJson =
                restClient
                        .post()
                        .uri(
                                uriBuilder ->
                                        uriBuilder
                                                .path("/acts/{actorId}/runs")
                                                .queryParam("token", ap.getToken())
                                                .build(actorPath))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(input.toString())
                        .retrieve()
                        .body(String.class);

        String runId = parseRunId(runJson);
        log.info("Apify run iniciado: {}", runId);

        String datasetId = waitForSuccessAndDataset(runId, ap);
        List<RawLeadRow> rows = fetchDatasetItems(datasetId, ap);
        if (rows.size() > max) {
            return new ArrayList<>(rows.subList(0, max));
        }
        return rows;
    }

    /**
     * Path do {@code POST /v2/acts/:actorId/runs}: use {@code usuario~nome-do-actor}, ou o ID curto do console
     * (ex. {@code nwua9Gu5YrADL7ZDj}). Barras viram til.
     */
    private static String normalizeActorPathSegment(String actorId) {
        if (actorId == null || actorId.isBlank()) {
            return "compass~crawler-google-places";
        }
        return actorId.trim().replace('/', '~');
    }

    private ObjectNode buildActorInput(String keyword, String location, int maxPlaces, String language) {
        ObjectNode n = objectMapper.createObjectNode();
        n.put("includeWebResults", false);
        n.put("language", normalizeApifyLanguage(language));
        n.put("locationQuery", location);
        n.put("maxCrawledPlacesPerSearch", maxPlaces);
        n.put("maximumLeadsEnrichmentRecords", 0);
        n.put("scrapeContacts", false);
        n.put("scrapeDirectories", false);
        n.put("scrapeImageAuthors", false);
        n.put("scrapePlaceDetailPage", false);
        n.put("scrapeReviewsPersonalData", true);
        ObjectNode social = n.putObject("scrapeSocialMediaProfiles");
        social.put("facebooks", false);
        social.put("instagrams", false);
        social.put("tiktoks", false);
        social.put("twitters", false);
        social.put("youtubes", false);
        n.put("scrapeTableReservationProvider", false);
        ArrayNode searches = n.putArray("searchStringsArray");
        searches.add(keyword);
        n.put("skipClosedPlaces", false);
        return n;
    }

    /**
     * O Actor só aceita códigos fixos (ex. {@code pt-BR}), não {@code pt}.
     *
     * @see <a href="https://apify.com/compass/crawler-google-places/input">Input schema</a>
     */
    private static String normalizeApifyLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "pt-BR";
        }
        String code = language.trim();
        if ("pt".equalsIgnoreCase(code)) {
            return "pt-BR";
        }
        return code;
    }

    private String parseRunId(String runJson) {
        try {
            JsonNode root = objectMapper.readTree(runJson);
            JsonNode data = root.path("data");
            String id = data.path("id").asText("");
            if (id.isBlank()) {
                throw new IllegalStateException("Resposta Apify sem data.id: " + runJson);
            }
            return id;
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao interpretar resposta do run Apify.", e);
        }
    }

    private String waitForSuccessAndDataset(String runId, LeadMapsProperties.Apify ap) {
        long deadline = System.currentTimeMillis() + ap.getMaxWaitMs();
        String terminal = null;
        while (System.currentTimeMillis() < deadline) {
            String body =
                    restClient
                            .get()
                            .uri(
                                    uriBuilder ->
                                            uriBuilder
                                                    .path("/actor-runs/{runId}")
                                                    .queryParam("token", ap.getToken())
                                                    .build(runId))
                            .retrieve()
                            .body(String.class);
            try {
                JsonNode data = objectMapper.readTree(body).path("data");
                String status = data.path("status").asText("");
                if ("SUCCEEDED".equalsIgnoreCase(status)) {
                    String ds = data.path("defaultDatasetId").asText(null);
                    if (ds == null || ds.isBlank()) {
                        throw new IllegalStateException("Run OK mas sem defaultDatasetId.");
                    }
                    return ds;
                }
                if ("FAILED".equalsIgnoreCase(status)
                        || "ABORTED".equalsIgnoreCase(status)
                        || "TIMED-OUT".equalsIgnoreCase(status)) {
                    terminal = data.path("statusMessage").asText(status);
                    break;
                }
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException("Erro ao ler status do run Apify.", e);
            }
            sleep(ap.getPollIntervalMs());
        }
        if (terminal != null) {
            throw new IllegalStateException("Apify run falhou: " + terminal);
        }
        throw new IllegalStateException("Timeout aguardando run Apify (" + ap.getMaxWaitMs() + " ms).");
    }

    private List<RawLeadRow> fetchDatasetItems(String datasetId, LeadMapsProperties.Apify ap) {
        String body =
                restClient
                        .get()
                        .uri(
                                uriBuilder ->
                                        uriBuilder
                                                .path("/datasets/{datasetId}/items")
                                                .queryParam("token", ap.getToken())
                                                .queryParam("clean", "true")
                                                .build(datasetId))
                        .retrieve()
                        .body(String.class);
        try {
            JsonNode arr = objectMapper.readTree(body);
            if (!arr.isArray()) {
                throw new IllegalStateException("Dataset Apify não é array JSON.");
            }
            List<RawLeadRow> out = new ArrayList<>();
            for (JsonNode item : arr) {
                RawLeadRow row = mapPlaceItem(item);
                if (row != null) {
                    out.add(row);
                }
            }
            return out;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao ler itens do dataset Apify.", e);
        }
    }

    private RawLeadRow mapPlaceItem(JsonNode item) {
        String title = textOrNull(item, "title");
        if (title == null || title.isBlank()) {
            return null;
        }
        if (item.path("permanentlyClosed").asBoolean(false)) {
            return null;
        }
        String phone = textOrNull(item, "phone");
        if (phone == null || phone.isBlank()) {
            phone = textOrNull(item, "phoneUnformatted");
        }
        String website = textOrNull(item, "website");
        BigDecimal rating = null;
        if (item.has("totalScore") && !item.get("totalScore").isNull()) {
            try {
                rating = BigDecimal.valueOf(item.get("totalScore").asDouble());
            } catch (Exception ignored) {
            }
        }
        Integer reviews = null;
        if (item.has("reviewsCount") && item.get("reviewsCount").canConvertToInt()) {
            reviews = item.get("reviewsCount").asInt();
        }
        String mapsUrl = textOrNull(item, "url");
        if (mapsUrl == null || mapsUrl.isBlank()) {
            String placeId = textOrNull(item, "placeId");
            if (placeId != null && !placeId.isBlank()) {
                mapsUrl =
                        "https://www.google.com/maps/search/?api=1&query_google_nav=1&query_place_id="
                                + URLEncoder.encode(placeId, StandardCharsets.UTF_8);
            }
        }
        return new RawLeadRow(title, emptyToNull(phone), emptyToNull(website), rating, reviews, mapsUrl);
    }

    private static String textOrNull(JsonNode n, String field) {
        if (!n.has(field) || n.get(field).isNull()) {
            return null;
        }
        String t = n.get(field).asText();
        return t == null || t.isBlank() ? null : t.trim();
    }

    private static String emptyToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s;
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrompido aguardando Apify.");
        }
    }
}
