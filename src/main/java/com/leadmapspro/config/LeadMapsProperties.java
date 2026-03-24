package com.leadmapspro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "leadmaps")
public class LeadMapsProperties {

    private final Cors cors = new Cors();
    private final Extract extract = new Extract();
    private final Apify apify = new Apify();
    private final Playwright playwright = new Playwright();
    private final Stripe stripe = new Stripe();
    private final Supabase supabase = new Supabase();
    private final Jwt jwt = new Jwt();

    public Cors getCors() {
        return cors;
    }

    public Extract getExtract() {
        return extract;
    }

    public Apify getApify() {
        return apify;
    }

    public Playwright getPlaywright() {
        return playwright;
    }

    public Stripe getStripe() {
        return stripe;
    }

    public Supabase getSupabase() {
        return supabase;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public static class Jwt {
        /** Mínimo 32 caracteres (256 bits) para HS256. Use JWT_SECRET em produção. */
        private String secret =
                "dev-only-change-me-leadmaps-pro-32chars-minimum-secret-key!!";
        private long accessExpirationMinutes = 15;
        private long refreshExpirationDays = 14;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getAccessExpirationMinutes() {
            return accessExpirationMinutes;
        }

        public void setAccessExpirationMinutes(long accessExpirationMinutes) {
            this.accessExpirationMinutes = accessExpirationMinutes;
        }

        public long getRefreshExpirationDays() {
            return refreshExpirationDays;
        }

        public void setRefreshExpirationDays(long refreshExpirationDays) {
            this.refreshExpirationDays = refreshExpirationDays;
        }
    }

    public static class Cors {
        private String allowedOrigins = "http://localhost:4200";

        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    /** {@code playwright} = browser local. {@code apify} = Google Maps Scraper na Apify (API). */
    public static class Extract {
        private String provider = "playwright";

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public boolean isApify() {
            return "apify".equalsIgnoreCase(provider);
        }
    }

    /** Ver Console Apify → Integrations → API token. Actor: id tipo {@code usuario~nome} ou id curto. */
    public static class Apify {
        private String token = "";
        /** Ex.: compass~crawler-google-places ou ID curto do console Apify. */
        private String actorId = "compass~crawler-google-places";
        /** Código exigido pelo Actor (ex. pt-BR, en) — não use só "pt". */
        private String language = "pt-BR";
        private int pollIntervalMs = 3000;
        /** Tempo máximo esperando o run (ms). */
        private long maxWaitMs = 600_000L;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getActorId() {
            return actorId;
        }

        public void setActorId(String actorId) {
            this.actorId = actorId;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public int getPollIntervalMs() {
            return pollIntervalMs;
        }

        public void setPollIntervalMs(int pollIntervalMs) {
            this.pollIntervalMs = pollIntervalMs;
        }

        public long getMaxWaitMs() {
            return maxWaitMs;
        }

        public void setMaxWaitMs(long maxWaitMs) {
            this.maxWaitMs = maxWaitMs;
        }

        public boolean isConfigured() {
            return token != null && !token.isBlank();
        }
    }

    public static class Playwright {
        private boolean headless = true;
        private boolean blockHeavyResources = true;
        private int navigationTimeoutMs = 120_000;
        private int defaultTimeoutMs = 120_000;
        /** Viewport menor = menos RAM no container (Railway etc.). */
        private int viewportWidth = 1280;
        private int viewportHeight = 720;
        /**
         * Pausa aleatória entre ações (ms). Padrão um pouco “humano”; no cloud pode aumentar max para
         * reduzir throttling (não substitui proxy residencial).
         */
        private int actionDelayMinMs = 400;
        private int actionDelayMaxMs = 1400;
        private String proxyServer = "";
        private String proxyUsername = "";
        private String proxyPassword = "";

        public boolean isHeadless() {
            return headless;
        }

        public void setHeadless(boolean headless) {
            this.headless = headless;
        }

        public boolean isBlockHeavyResources() {
            return blockHeavyResources;
        }

        public void setBlockHeavyResources(boolean blockHeavyResources) {
            this.blockHeavyResources = blockHeavyResources;
        }

        public int getNavigationTimeoutMs() {
            return navigationTimeoutMs;
        }

        public void setNavigationTimeoutMs(int navigationTimeoutMs) {
            this.navigationTimeoutMs = navigationTimeoutMs;
        }

        public int getDefaultTimeoutMs() {
            return defaultTimeoutMs;
        }

        public void setDefaultTimeoutMs(int defaultTimeoutMs) {
            this.defaultTimeoutMs = defaultTimeoutMs;
        }

        public int getViewportWidth() {
            return viewportWidth;
        }

        public void setViewportWidth(int viewportWidth) {
            this.viewportWidth = viewportWidth;
        }

        public int getViewportHeight() {
            return viewportHeight;
        }

        public void setViewportHeight(int viewportHeight) {
            this.viewportHeight = viewportHeight;
        }

        public int getActionDelayMinMs() {
            return actionDelayMinMs;
        }

        public void setActionDelayMinMs(int actionDelayMinMs) {
            this.actionDelayMinMs = actionDelayMinMs;
        }

        public int getActionDelayMaxMs() {
            return actionDelayMaxMs;
        }

        public void setActionDelayMaxMs(int actionDelayMaxMs) {
            this.actionDelayMaxMs = actionDelayMaxMs;
        }

        public String getProxyServer() {
            return proxyServer;
        }

        public void setProxyServer(String proxyServer) {
            this.proxyServer = proxyServer;
        }

        public String getProxyUsername() {
            return proxyUsername;
        }

        public void setProxyUsername(String proxyUsername) {
            this.proxyUsername = proxyUsername;
        }

        public String getProxyPassword() {
            return proxyPassword;
        }

        public void setProxyPassword(String proxyPassword) {
            this.proxyPassword = proxyPassword;
        }

        public boolean hasProxy() {
            return proxyServer != null && !proxyServer.isBlank();
        }
    }

    public static class Stripe {
        private String secretKey = "";
        private String webhookSecret = "";
        private String priceStarter = "";
        private String pricePro = "";
        private String priceAgency = "";
        private String successUrl = "";
        private String cancelUrl = "";
        private String portalReturnUrl = "";

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }

        public String getPriceStarter() {
            return priceStarter;
        }

        public void setPriceStarter(String priceStarter) {
            this.priceStarter = priceStarter;
        }

        public String getPricePro() {
            return pricePro;
        }

        public void setPricePro(String pricePro) {
            this.pricePro = pricePro;
        }

        public String getPriceAgency() {
            return priceAgency;
        }

        public void setPriceAgency(String priceAgency) {
            this.priceAgency = priceAgency;
        }

        public String getSuccessUrl() {
            return successUrl;
        }

        public void setSuccessUrl(String successUrl) {
            this.successUrl = successUrl;
        }

        public String getCancelUrl() {
            return cancelUrl;
        }

        public void setCancelUrl(String cancelUrl) {
            this.cancelUrl = cancelUrl;
        }

        public String getPortalReturnUrl() {
            return portalReturnUrl;
        }

        public void setPortalReturnUrl(String portalReturnUrl) {
            this.portalReturnUrl = portalReturnUrl;
        }
    }

    public static class Supabase {
        private String jwtIssuer = "";
        private String jwkSetUri = "";

        public String getJwtIssuer() {
            return jwtIssuer;
        }

        public void setJwtIssuer(String jwtIssuer) {
            this.jwtIssuer = jwtIssuer;
        }

        public String getJwkSetUri() {
            return jwkSetUri;
        }

        public void setJwkSetUri(String jwkSetUri) {
            this.jwkSetUri = jwkSetUri;
        }

        public boolean isConfigured() {
            return jwkSetUri != null && !jwkSetUri.isBlank();
        }
    }
}
