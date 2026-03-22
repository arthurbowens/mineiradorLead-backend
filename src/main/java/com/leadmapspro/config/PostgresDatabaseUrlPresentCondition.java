package com.leadmapspro.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/** Ativo quando {@code DATABASE_URL} está no formato {@code postgres(ql)://...}. */
public class PostgresDatabaseUrlPresentCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String u = context.getEnvironment().getProperty("DATABASE_URL");
        if (u == null || u.isBlank()) {
            u = System.getenv("DATABASE_URL");
        }
        if (u == null) {
            return false;
        }
        u = u.trim();
        return u.startsWith("postgres://") || u.startsWith("postgresql://");
    }
}
