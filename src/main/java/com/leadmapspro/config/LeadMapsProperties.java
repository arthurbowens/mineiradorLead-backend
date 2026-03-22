package com.leadmapspro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "leadmaps")
public class LeadMapsProperties {

    private final Cors cors = new Cors();
    private final Playwright playwright = new Playwright();
    private final Stripe stripe = new Stripe();
    private final Supabase supabase = new Supabase();
    private final Jwt jwt = new Jwt();

    public Cors getCors() {
        return cors;
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

    public static class Playwright {
        private boolean headless = true;
        /** Bloqueia imagem/fonte/mídia no Maps — menos RAM e CPU no Docker/Render. */
        private boolean blockHeavyResources = true;
        /** Timeout de navegação (goto); Maps em datacenter pode precisar de mais tempo. */
        private int navigationTimeoutMs = 120_000;
        /** Timeout padrão de ações (locator, click, etc.). */
        private int defaultTimeoutMs = 120_000;
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
