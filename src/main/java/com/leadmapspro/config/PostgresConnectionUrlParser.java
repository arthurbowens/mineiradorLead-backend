package com.leadmapspro.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/** Converte {@code postgres(ql)://} em JDBC + credenciais (Render, Heroku, etc.). */
public final class PostgresConnectionUrlParser {

    private PostgresConnectionUrlParser() {}

    public record Parsed(String jdbcUrl, String username, String password) {}

    public static Parsed parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("DATABASE_URL vazia");
        }
        raw = raw.trim();
        if (!raw.startsWith("postgres://") && !raw.startsWith("postgresql://")) {
            throw new IllegalArgumentException("DATABASE_URL deve começar com postgres:// ou postgresql://");
        }
        try {
            String normalized = raw.replaceFirst("^postgres(ql)?://", "http://");
            URI uri = new URI(normalized);
            String userInfo = uri.getUserInfo();
            String user = "postgres";
            String password = "";
            if (userInfo != null && !userInfo.isEmpty()) {
                int colon = userInfo.indexOf(':');
                if (colon >= 0) {
                    user = URLDecoder.decode(userInfo.substring(0, colon), StandardCharsets.UTF_8);
                    password = URLDecoder.decode(userInfo.substring(colon + 1), StandardCharsets.UTF_8);
                } else {
                    user = URLDecoder.decode(userInfo, StandardCharsets.UTF_8);
                }
            }
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                throw new IllegalArgumentException("host ausente em DATABASE_URL");
            }
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String path = uri.getPath();
            if (path != null && path.startsWith("/")) {
                path = path.substring(1);
            }
            String database = (path != null && !path.isEmpty()) ? path : "postgres";
            String jdbcUrl =
                    String.format("jdbc:postgresql://%s:%d/%s?sslmode=require", host, port, database);
            return new Parsed(jdbcUrl, user, password);
        } catch (Exception e) {
            throw new IllegalStateException("DATABASE_URL inválida: " + e.getMessage(), e);
        }
    }
}
