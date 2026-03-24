package com.leadmapspro.scrape;

import com.leadmapspro.config.LeadMapsProperties;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.Proxy;
import com.microsoft.playwright.options.WaitUntilState;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Scraping do Google Maps via Playwright (sem Places API).
 * <p>
 * A lista lateral costuma trazer só o nome; telefone, site e avaliações completas aparecem no
 * <strong>painel de detalhes</strong>. Por isso, após listar os links {@code /maps/place/}, navegamos
 * em cada um e extraímos do painel aberto.
 */
@Component
public class GoogleMapsPlaywrightScraper {

    private static final Logger log = LoggerFactory.getLogger(GoogleMapsPlaywrightScraper.class);

    /** Ex.: "4,5" ou "4.5" seguido de estrela ou fim */
    private static final Pattern RATING_STRICT =
            Pattern.compile("(?:^|\\s)([0-5])([.,])([0-9])\\b");
    /** "64 avaliações", "(64)", "64 opiniões" */
    private static final Pattern REVIEWS_PT =
            Pattern.compile(
                    "(?:\\()?(\\d[\\d.]*)\\s*(?:avalia[çc][õo]es|opini[õo]es|reviews|cr[íi]ticas)\\)?",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern REVIEWS_PARENS = Pattern.compile("\\((\\d[\\d.]*)\\)");

    private final LeadMapsProperties properties;

    public GoogleMapsPlaywrightScraper(LeadMapsProperties properties) {
        this.properties = properties;
    }

    public List<RawLeadRow> scrape(String keyword, String location, int maxResults) {
        String q = keyword.trim() + " em " + location.trim();
        String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8);
        String searchUrl = "https://www.google.com/maps/search/" + encoded;

        LeadMapsProperties.Playwright pw = properties.getPlaywright();
        List<RawLeadRow> rows = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launch =
                    new BrowserType.LaunchOptions()
                            .setHeadless(pw.isHeadless())
                            .setChromiumSandbox(false)
                            .setArgs(
                                    Arrays.asList(
                                            "--no-sandbox",
                                            "--disable-setuid-sandbox",
                                            "--disable-dev-shm-usage",
                                            "--disable-gpu",
                                            "--disable-extensions",
                                            // Menos processos/RAM em VM pequena (Railway free/hobby).
                                            "--disable-background-networking",
                                            "--disable-renderer-backgrounding",
                                            "--disable-background-timer-throttling",
                                            "--disable-backgrounding-occluded-windows",
                                            "--renderer-process-limit=2"));

            Browser browser = playwright.chromium().launch(launch);
            int vw = Math.max(800, pw.getViewportWidth());
            int vh = Math.max(600, pw.getViewportHeight());
            Browser.NewContextOptions ctxOpts =
                    new Browser.NewContextOptions()
                            .setUserAgent(
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                                            + "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                            .setLocale("pt-BR")
                            .setViewportSize(vw, vh);

            if (pw.hasProxy()) {
                Proxy proxy = new Proxy(pw.getProxyServer());
                if (pw.getProxyUsername() != null && !pw.getProxyUsername().isBlank()) {
                    proxy.setUsername(pw.getProxyUsername());
                }
                if (pw.getProxyPassword() != null && !pw.getProxyPassword().isBlank()) {
                    proxy.setPassword(pw.getProxyPassword());
                }
                ctxOpts.setProxy(proxy);
            }

            try (BrowserContext context = browser.newContext(ctxOpts)) {
                Page page = context.newPage();
                page.setDefaultTimeout(pw.getDefaultTimeoutMs());
                page.setDefaultNavigationTimeout(pw.getNavigationTimeoutMs());
                if (pw.isBlockHeavyResources()) {
                    blockHeavyResources(page);
                }
                navigateMaps(page, searchUrl);

                dismissCookieIfPresent(page);
                waitForResultsPane(page);
                scrollFeed(page, Math.min(14, maxResults / 2 + 6));

                List<String> placeUrls = collectPlaceUrls(page, maxResults);
                log.info("Links de lugares coletados na lista: {}", placeUrls.size());

                for (int i = 0; i < placeUrls.size() && rows.size() < maxResults; i++) {
                    String placeUrl = placeUrls.get(i);
                    try {
                        navigateMaps(page, ensureAbsoluteMapsUrl(placeUrl));
                        actionDelay(page, 1200, 3200);
                        waitForDetailHints(page);
                        RawLeadRow row = extractFromDetailPanel(page);
                        if (row.name() != null && !row.name().isBlank()) {
                            rows.add(row);
                        }
                    } catch (Exception ex) {
                        log.warn("Falha ao extrair lugar {}: {}", placeUrl, ex.getMessage());
                    }
                    actionDelay(page);
                }

                if (rows.isEmpty()) {
                    log.warn("Extração por painel vazia; voltando à busca e tentando lista.");
                    navigateMaps(page, searchUrl);
                    actionDelay(page, 900, 2200);
                    dismissCookieIfPresent(page);
                    waitForResultsPane(page);
                    scrollFeed(page, 10);
                    rows.addAll(parseVisibleCardsQuick(page, maxResults));
                }
            } finally {
                browser.close();
            }
        } catch (Exception e) {
            log.error("Falha no Playwright ao extrair Maps: {}", e.getMessage(), e);
            throw new IllegalStateException("Não foi possível concluir a extração no Google Maps.", e);
        }

        return rows;
    }

    /**
     * No datacenter (Render) o evento {@code load} costuma não disparar a tempo; {@code domcontentloaded} é mais
     * realista. Ainda assim, Google pode bloquear IP de servidor — aí só proxy residencial ({@code PROXY_SERVER}).
     */
    /** Pausa configurável (leadmaps.playwright.action-delay-*) com jitter. */
    private void actionDelay(Page page) {
        LeadMapsProperties.Playwright pw = properties.getPlaywright();
        actionDelay(page, pw.getActionDelayMinMs(), pw.getActionDelayMaxMs());
    }

    private static void actionDelay(Page page, int minMs, int maxMs) {
        int lo = Math.min(minMs, maxMs);
        int hi = Math.max(minMs, maxMs);
        int ms = lo == hi ? lo : ThreadLocalRandom.current().nextInt(lo, hi + 1);
        page.waitForTimeout(ms);
    }

    private static void navigateMaps(Page page, String url) {
        page.navigate(
                url,
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    private static void blockHeavyResources(Page page) {
        page.route(
                "**/*",
                route -> {
                    String rt = route.request().resourceType();
                    if ("image".equals(rt) || "media".equals(rt) || "font".equals(rt)) {
                        route.abort();
                    } else {
                        route.resume();
                    }
                });
    }

    private static String ensureAbsoluteMapsUrl(String href) {
        if (href == null) {
            return "";
        }
        if (href.startsWith("http")) {
            return href;
        }
        return "https://www.google.com" + (href.startsWith("/") ? href : "/" + href);
    }

    private List<String> collectPlaceUrls(Page page, int maxResults) {
        Set<String> seen = new LinkedHashSet<>();
        List<String> out = new ArrayList<>();
        Locator links = page.locator("div[role='feed'] a[href*='/maps/place/']");
        int n = links.count();
        if (n == 0) {
            links = page.locator("a[href*='/maps/place/']");
            n = links.count();
        }
        for (int i = 0; i < n && out.size() < maxResults; i++) {
            try {
                String href = links.nth(i).getAttribute("href");
                if (href == null || href.isBlank()) {
                    continue;
                }
                String normalized = stripPlaceUrlToCanonical(href);
                if (normalized.isEmpty() || seen.contains(normalized)) {
                    continue;
                }
                seen.add(normalized);
                out.add(href.startsWith("http") ? href : ensureAbsoluteMapsUrl(href));
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    /** Remove fragmentos duplicados do mesmo lugar (query diferentes). */
    private static String stripPlaceUrlToCanonical(String href) {
        try {
            URI u = URI.create(href.startsWith("http") ? href : "https://www.google.com" + href);
            String path = u.getPath();
            return path != null ? path : href;
        } catch (Exception e) {
            return href;
        }
    }

    private void waitForDetailHints(Page page) {
        try {
            page.locator("h1").first().waitFor(new Locator.WaitForOptions().setTimeout(12_000));
        } catch (Exception e) {
            log.debug("Timeout aguardando h1 no painel: {}", e.getMessage());
        }
    }

    /**
     * Extrai nome, telefone, site, nota e avaliações do painel de detalhe (página do lugar).
     */
    private RawLeadRow extractFromDetailPanel(Page page) {
        String name = firstNonBlank(
                textFirst(page, "h1.DUwDvf"),
                textFirst(page, "h1[class*='DUwDvf']"),
                textFirst(page, "div[role='main'] h1"),
                textFirst(page, "h1"));

        String mapsUrl = page.url();

        String phone = extractPhone(page);
        String website = extractWebsite(page);

        String blob = safePageTextForParsing(page);
        BigDecimal rating = parseRatingStrict(blob);
        Integer reviews = parseReviewsPt(blob);

        return new RawLeadRow(emptyToNull(name), emptyToNull(phone), emptyToNull(website), rating, reviews, mapsUrl);
    }

    private static String extractPhone(Page page) {
        Locator telLinks = page.locator("a[href^='tel:']");
        int c = telLinks.count();
        for (int i = 0; i < c; i++) {
            try {
                String href = telLinks.nth(i).getAttribute("href");
                if (href != null && href.startsWith("tel:")) {
                    String p = href.substring("tel:".length()).trim().replaceAll("\\s+", "");
                    if (!p.isEmpty()) {
                        return p;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        String mainTxt = mainInnerText(page);
        Matcher phoneMatcher =
                Pattern.compile("\\(\\s*\\d{2}\\s*\\)\\s*[\\d\\s-]{8,}").matcher(mainTxt);
        if (phoneMatcher.find()) {
            return phoneMatcher.group().replaceAll("\\D+", "");
        }
        return null;
    }

    private static String mainInnerText(Page page) {
        Locator main = page.locator("div[role='main']").first();
        if (main.count() == 0) {
            return "";
        }
        try {
            return main.innerText();
        } catch (Exception e) {
            return "";
        }
    }

    private static String extractWebsite(Page page) {
        String[] selectors = {
            "a[data-item-id='authority']",
            "a[data-item-id=\"authority\"]",
            "[data-item-id='authority'] a[href^='http']",
            "a[href^='http'][aria-label*='Website']",
            "a[href^='http'][aria-label*='website']",
            "a[href^='http'][aria-label*='Site']",
            "a[href^='http'][aria-label*='site']",
        };
        for (String sel : selectors) {
            Locator all = page.locator(sel);
            int total = all.count();
            for (int j = 0; j < total; j++) {
                try {
                    String href = all.nth(j).getAttribute("href");
                    if (isBusinessWebsite(href)) {
                        return href;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        Locator httpLinks = page.locator("div[role='main'] a[href^='http']");
        int n = httpLinks.count();
        for (int i = 0; i < n; i++) {
            try {
                String href = httpLinks.nth(i).getAttribute("href");
                if (isBusinessWebsite(href)) {
                    return href;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static boolean isBusinessWebsite(String href) {
        if (href == null || href.length() < 10) {
            return false;
        }
        String h = href.toLowerCase();
        return !h.contains("google.com")
                && !h.contains("goo.gl")
                && !h.contains("maps.app.goo.gl")
                && !h.contains("instagram.com")
                && !h.contains("facebook.com")
                && !h.contains("youtube.com")
                && !h.contains("wa.me")
                && !h.contains("whatsapp.com");
    }

    private static String safePageTextForParsing(Page page) {
        try {
            Locator main = page.locator("div[role='main']").first();
            if (main.count() > 0) {
                return main.innerText();
            }
        } catch (Exception ignored) {
        }
        return safeInnerText(page.locator("body"));
    }

    private static String textFirst(Page page, String selector) {
        Locator loc = page.locator(selector).first();
        if (loc.count() == 0) {
            return "";
        }
        try {
            return loc.innerText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static String safeInnerText(Locator root) {
        try {
            if (root.count() == 0) {
                return "";
            }
            return root.innerText();
        } catch (Exception e) {
            return "";
        }
    }

    private static String firstNonBlank(String... parts) {
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                return p;
            }
        }
        return "";
    }

    /** Nota com contexto de estrelas (evita “5” solto no endereço). */
    private static BigDecimal parseRatingStrict(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher m = RATING_STRICT.matcher(text);
        if (m.find()) {
            try {
                return new BigDecimal(m.group(1) + "." + m.group(3));
            } catch (NumberFormatException ignored) {
            }
        }
        Matcher m2 = Pattern.compile("([0-5])[.,]([0-9])\\s*(?:estrelas|stars|⭐)?").matcher(text);
        if (m2.find()) {
            try {
                return new BigDecimal(m2.group(1) + "." + m2.group(2));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static Integer parseReviewsPt(String text) {
        if (text == null) {
            return null;
        }
        Matcher m = Pattern.compile("(\\d[\\d.]*)\\s*avalia", Pattern.CASE_INSENSITIVE).matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1).replace(".", ""));
            } catch (NumberFormatException ignored) {
            }
        }
        m = REVIEWS_PT.matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1).replace(".", ""));
            } catch (NumberFormatException ignored) {
            }
        }
        m = REVIEWS_PARENS.matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1).replace(".", ""));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private void dismissCookieIfPresent(Page page) {
        try {
            Locator accept =
                    page.locator(
                            "button:has-text('Aceitar tudo'), button:has-text('Accept all'), "
                                    + "button:has-text('Aceitar'), button:has-text('Accept')");
            if (accept.count() > 0) {
                accept.first().click();
                actionDelay(page, 600, 1200);
            }
        } catch (Exception ignored) {
        }
    }

    private void waitForResultsPane(Page page) {
        Locator feed = page.locator("div[role='feed']");
        feed.waitFor();
        actionDelay(page, 900, 1700);
    }

    private void scrollFeed(Page page, int rounds) {
        Locator feed = page.locator("div[role='feed']").first();
        for (int i = 0; i < rounds; i++) {
            try {
                feed.evaluate("e => { e.scrollTop = e.scrollHeight; }");
            } catch (Exception ex) {
                log.debug("Scroll feed: {}", ex.getMessage());
            }
            actionDelay(page, 650, 1300);
        }
    }

    /** Fallback rápido na lista (dados incompletos). */
    private List<RawLeadRow> parseVisibleCardsQuick(Page page, int maxResults) {
        Set<String> seen = new LinkedHashSet<>();
        List<RawLeadRow> out = new ArrayList<>();
        Locator articles = page.locator("div[role='article']");
        int n = articles.count();
        for (int i = 0; i < n && out.size() < maxResults; i++) {
            Locator card = articles.nth(i);
            try {
                RawLeadRow row = extractFromCard(card);
                if (row != null && row.name() != null && !row.name().isBlank()) {
                    String key = row.mapsUrl() != null ? row.mapsUrl() : row.name();
                    if (!seen.contains(key)) {
                        seen.add(key);
                        out.add(row);
                    }
                }
            } catch (Exception ex) {
                log.debug("Card fallback {}: {}", i, ex.getMessage());
            }
        }
        if (out.isEmpty()) {
            out.addAll(fallbackPlaceLinks(page, maxResults, seen));
        }
        return out;
    }

    private List<RawLeadRow> fallbackPlaceLinks(Page page, int maxResults, Set<String> seen) {
        List<RawLeadRow> out = new ArrayList<>();
        Locator links = page.locator("a[href*='/maps/place/']");
        int n = links.count();
        for (int i = 0; i < n && out.size() < maxResults; i++) {
            try {
                String href = links.nth(i).getAttribute("href");
                if (href == null || seen.contains(href)) {
                    continue;
                }
                seen.add(href);
                String label = links.nth(i).getAttribute("aria-label");
                if (label == null || label.isBlank()) {
                    label = links.nth(i).innerText().trim();
                }
                String abs = ensureAbsoluteMapsUrl(href);
                out.add(new RawLeadRow(label, null, null, null, null, abs));
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    private RawLeadRow extractFromCard(Locator card) {
        String mapsUrl = firstHref(card, "a[href*='/maps/place/']");
        String name = textFrom(card, "div[role='heading']");
        if (name.isBlank()) {
            name = textFrom(card, ".qBF1Pd");
        }
        if (name.isBlank()) {
            name = textFrom(card, ".fontHeadlineSmall");
        }
        if (name.isBlank()) {
            Locator aria = card.locator("a[aria-label]").first();
            if (aria.count() > 0) {
                String al = aria.getAttribute("aria-label");
                if (al != null) {
                    name = al.trim();
                }
            }
        }

        String phone = firstHref(card, "a[href^='tel:']");
        if (phone != null && phone.startsWith("tel:")) {
            phone = phone.substring("tel:".length()).trim();
        }

        String website = firstExternalWebsite(card);
        String blob = safeInnerText(card);
        BigDecimal rating = parseRatingStrict(blob);
        Integer reviews = parseReviewsPt(blob);

        return new RawLeadRow(emptyToNull(name), emptyToNull(phone), emptyToNull(website), rating, reviews, mapsUrl);
    }

    private static String firstHref(Locator root, String selector) {
        Locator loc = root.locator(selector).first();
        if (loc.count() == 0) {
            return null;
        }
        return loc.getAttribute("href");
    }

    private static String firstExternalWebsite(Locator card) {
        Locator httpLinks = card.locator("a[href^='http']");
        int c = httpLinks.count();
        for (int i = 0; i < c; i++) {
            String href = httpLinks.nth(i).getAttribute("href");
            if (isBusinessWebsite(href)) {
                return href;
            }
        }
        return null;
    }

    private static String textFrom(Locator root, String selector) {
        Locator loc = root.locator(selector).first();
        if (loc.count() == 0) {
            return "";
        }
        return loc.innerText().trim();
    }

    private static String emptyToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s;
    }
}
