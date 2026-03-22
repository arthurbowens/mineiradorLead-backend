package com.leadmapspro.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

/**
 * Quando o Render injeta {@code DATABASE_URL} ({@code postgresql://...}), cria o {@link DataSource}
 * aqui — mais confiável que {@code EnvironmentPostProcessor} em fat JAR.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@Conditional(PostgresDatabaseUrlPresentCondition.class)
public class RenderPostgresDataSourceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RenderPostgresDataSourceConfiguration.class);

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        String raw = env.getProperty("DATABASE_URL");
        if (raw == null || raw.isBlank()) {
            raw = System.getenv("DATABASE_URL");
        }
        PostgresConnectionUrlParser.Parsed p = PostgresConnectionUrlParser.parse(raw);
        log.info("Datasource a partir de DATABASE_URL (host JDBC): {}", maskJdbc(p.jdbcUrl()));

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(p.jdbcUrl());
        ds.setUsername(p.username());
        ds.setPassword(p.password());
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(10);
        return ds;
    }

    private static String maskJdbc(String jdbcUrl) {
        if (jdbcUrl == null) {
            return "";
        }
        return jdbcUrl.length() > 80 ? jdbcUrl.substring(0, 80) + "…" : jdbcUrl;
    }
}
