package com.leadmapspro.api;

import com.leadmapspro.api.dto.LeadResponse;
import com.leadmapspro.api.dto.UpdateLeadRequest;
import com.leadmapspro.security.CurrentUserId;
import com.leadmapspro.service.LeadService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${leadmaps.cors.allowed-origins:http://localhost:4200}")
public class LeadController {

    private final LeadService leadService;
    private final CurrentUserId currentUserId;

    public LeadController(LeadService leadService, CurrentUserId currentUserId) {
        this.leadService = leadService;
        this.currentUserId = currentUserId;
    }

    @GetMapping("/search-histories/{searchHistoryId}/leads")
    public List<LeadResponse> listBySearchHistory(@PathVariable("searchHistoryId") UUID searchHistoryId) {
        UUID userId = currentUserId.require();
        return leadService.listBySearchHistory(searchHistoryId, userId);
    }

    @PatchMapping("/leads/{id}")
    public LeadResponse patch(@PathVariable("id") UUID id, @Valid @RequestBody UpdateLeadRequest body) {
        UUID userId = currentUserId.require();
        return leadService.update(id, userId, body);
    }

    @DeleteMapping("/leads/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") UUID id) {
        UUID userId = currentUserId.require();
        leadService.deleteIfOwned(id, userId);
    }
}
