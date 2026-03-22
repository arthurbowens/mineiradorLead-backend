package com.leadmapspro.api;

import com.leadmapspro.security.CurrentUserId;
import com.leadmapspro.service.LeadCsvExportService;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search-histories")
@CrossOrigin(origins = "${leadmaps.cors.allowed-origins:http://localhost:4200}")
public class LeadExportController {

    private final LeadCsvExportService csvExportService;
    private final CurrentUserId currentUserId;

    public LeadExportController(LeadCsvExportService csvExportService, CurrentUserId currentUserId) {
        this.csvExportService = csvExportService;
        this.currentUserId = currentUserId;
    }

    @GetMapping("/{id}/export.csv")
    public ResponseEntity<Resource> exportCsv(@PathVariable("id") UUID searchHistoryId) {
        UUID userId = currentUserId.require();
        byte[] data = csvExportService.exportSearchHistory(searchHistoryId, userId);
        ByteArrayResource resource = new ByteArrayResource(data);
        String filename = "leadmaps-pro-busca-" + searchHistoryId + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(data.length)
                .body(resource);
    }
}
