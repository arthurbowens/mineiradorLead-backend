package com.leadmapspro.scrape;

import java.math.BigDecimal;

/**
 * Linha bruta antes de persistir / enriquecer. Seletores do Maps mudam com frequência — ajuste em
 * {@link com.leadmapspro.scrape.GoogleMapsPlaywrightScraper}.
 */
public record RawLeadRow(
        String name,
        String phone,
        String website,
        BigDecimal rating,
        Integer reviewCount,
        String mapsUrl
) {
}
