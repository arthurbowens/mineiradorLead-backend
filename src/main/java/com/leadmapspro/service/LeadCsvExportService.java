package com.leadmapspro.service;

import com.leadmapspro.domain.Lead;
import com.leadmapspro.repository.LeadRepository;
import com.leadmapspro.repository.SearchHistoryRepository;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exportação CSV (equivalente ao fluxo com csv-writer no Node). Usa Apache Commons CSV no Java.
 */
@Service
public class LeadCsvExportService {

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader(
                    "nome",
                    "telefone",
                    "site",
                    "nota",
                    "total_avaliacoes",
                    "instagram",
                    "facebook",
                    "maps_url")
            .build();

    private final LeadRepository leadRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    public LeadCsvExportService(LeadRepository leadRepository, SearchHistoryRepository searchHistoryRepository) {
        this.leadRepository = leadRepository;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    public byte[] exportSearchHistory(UUID searchHistoryId, UUID ownerUserId) {
        if (!searchHistoryRepository.existsByIdAndUser_Id(searchHistoryId, ownerUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Histórico de busca não encontrado ou sem permissão.");
        }
        List<Lead> leads = leadRepository.findBySearchHistory_IdOrderByCreatedAtAsc(searchHistoryId);
        try (StringWriter sw = new StringWriter();
                CSVPrinter printer = new CSVPrinter(sw, FORMAT)) {
            for (Lead l : leads) {
                printer.printRecord(
                        nz(l.getName()),
                        nz(l.getPhone()),
                        nz(l.getWebsite()),
                        l.getRating() != null ? l.getRating().toPlainString() : "",
                        l.getReviewCount() != null ? l.getReviewCount().toString() : "",
                        nz(l.getInstagramUrl()),
                        nz(l.getFacebookUrl()),
                        nz(l.getMapsUrl()));
            }
            printer.flush();
            return sw.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao gerar CSV.", e);
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
