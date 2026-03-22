package com.leadmapspro.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Busca rápida na home do site por links de Instagram / Facebook (HTML bruto). */
@Service
public class SocialEnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(SocialEnrichmentService.class);

    private static final Pattern IG =
            Pattern.compile("https?://(?:www\\.)?instagram\\.com/[a-zA-Z0-9._]+/?", Pattern.CASE_INSENSITIVE);
    private static final Pattern FB = Pattern.compile(
            "https?://(?:www\\.)?(?:facebook\\.com|fb\\.com)/[a-zA-Z0-9._/-]+/?", Pattern.CASE_INSENSITIVE);

    private final HttpClient http =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public Optional<String> findInstagramUrl(String websiteUrl) {
        return fetchHome(websiteUrl).flatMap(html -> firstMatch(IG, html));
    }

    public Optional<String> findFacebookUrl(String websiteUrl) {
        return fetchHome(websiteUrl).flatMap(html -> firstMatch(FB, html));
    }

    private Optional<String> firstMatch(Pattern p, String html) {
        Matcher m = p.matcher(html);
        if (m.find()) {
            return Optional.of(m.group());
        }
        return Optional.empty();
    }

    private Optional<String> fetchHome(String websiteUrl) {
        if (websiteUrl == null || websiteUrl.isBlank()) {
            return Optional.empty();
        }
        String url = websiteUrl.trim();
        if (!url.startsWith("http")) {
            url = "https://" + url;
        }
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(6))
                    .header(
                            "User-Agent",
                            "Mozilla/5.0 (compatible; LeadMapsPro/0.1; +https://leadmaps.pro)")
                    .GET()
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 200 && res.statusCode() < 400) {
                return Optional.ofNullable(res.body());
            }
        } catch (Exception e) {
            log.debug("Enriquecimento falhou para {}: {}", url, e.getMessage());
        }
        return Optional.empty();
    }
}
