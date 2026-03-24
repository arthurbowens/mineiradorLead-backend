package com.leadmapspro.service;

import com.leadmapspro.api.dto.ExtractRequest;
import com.leadmapspro.api.dto.ExtractResponse;
import com.leadmapspro.api.dto.LeadResponse;
import com.leadmapspro.domain.AppUser;
import com.leadmapspro.domain.Lead;
import com.leadmapspro.domain.SearchHistory;
import com.leadmapspro.domain.SearchStatus;
import com.leadmapspro.apify.ApifyGoogleMapsActorClient;
import com.leadmapspro.config.LeadMapsProperties;
import com.leadmapspro.repository.AppUserRepository;
import com.leadmapspro.repository.LeadRepository;
import com.leadmapspro.repository.SearchHistoryRepository;
import com.leadmapspro.scrape.GoogleMapsPlaywrightScraper;
import com.leadmapspro.scrape.RawLeadRow;
import com.leadmapspro.security.CurrentUserId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LeadExtractService {

    private final CurrentUserId currentUserId;
    private final AppUserRepository userRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final LeadRepository leadRepository;
    private final LeadMapsProperties leadMapsProperties;
    private final GoogleMapsPlaywrightScraper scraper;
    private final ApifyGoogleMapsActorClient apifyClient;
    private final SocialEnrichmentService enrichmentService;
    private final ExtractCompletionService completionService;

    public LeadExtractService(
            CurrentUserId currentUserId,
            AppUserRepository userRepository,
            SearchHistoryRepository searchHistoryRepository,
            LeadRepository leadRepository,
            LeadMapsProperties leadMapsProperties,
            GoogleMapsPlaywrightScraper scraper,
            ApifyGoogleMapsActorClient apifyClient,
            SocialEnrichmentService enrichmentService,
            ExtractCompletionService completionService) {
        this.currentUserId = currentUserId;
        this.userRepository = userRepository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.leadRepository = leadRepository;
        this.leadMapsProperties = leadMapsProperties;
        this.scraper = scraper;
        this.apifyClient = apifyClient;
        this.enrichmentService = enrichmentService;
        this.completionService = completionService;
    }

    public ExtractResponse extract(ExtractRequest request) {
        UUID userId = currentUserId.require();
        AppUser user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        int max = request.getMaxResults();
        if (user.getCreditBalance() < max) {
            throw new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    "Créditos insuficientes. Necessário: "
                            + max
                            + ", saldo: "
                            + user.getCreditBalance()
                            + ". Cada lead extraído consome 1 crédito.");
        }

        String fullQuery = request.getKeyword().trim() + " em " + request.getLocation().trim();
        SearchHistory history = startHistory(user, request, fullQuery);

        try {
            List<RawLeadRow> raw;
            if (leadMapsProperties.getExtract().isApify()) {
                if (!leadMapsProperties.getApify().isConfigured()) {
                    throw new ResponseStatusException(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            "Modo Apify ativo: defina APIFY_TOKEN (Railway → Variables).");
                }
                raw = apifyClient.scrape(request.getKeyword(), request.getLocation(), max);
            } else {
                raw = scraper.scrape(request.getKeyword(), request.getLocation(), max);
            }
            int toCharge = Math.min(raw.size(), max);
            List<Lead> saved = persistAndEnrich(history, raw.subList(0, toCharge));
            int newBalance = completionService.applySuccess(user, history, saved.size(), toCharge);
            return toResponse(history.getId(), newBalance, saved);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            completionService.markFailed(history, e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Extração falhou: " + e.getMessage(), e);
        }
    }

    private SearchHistory startHistory(AppUser user, ExtractRequest request, String fullQuery) {
        SearchHistory h = new SearchHistory();
        h.setUser(user);
        h.setKeyword(request.getKeyword().trim());
        h.setLocation(request.getLocation().trim());
        h.setFullQuery(fullQuery);
        h.setStatus(SearchStatus.RUNNING);
        return searchHistoryRepository.save(h);
    }

    private List<Lead> persistAndEnrich(SearchHistory history, List<RawLeadRow> rows) {
        List<Lead> saved = new ArrayList<>();
        for (RawLeadRow r : rows) {
            Lead lead = new Lead();
            lead.setSearchHistory(history);
            lead.setName(r.name());
            lead.setPhone(r.phone());
            lead.setWebsite(r.website());
            lead.setRating(r.rating());
            lead.setReviewCount(r.reviewCount());
            lead.setMapsUrl(r.mapsUrl());

            if (r.website() != null && !r.website().isBlank()) {
                enrichmentService.findInstagramUrl(r.website()).ifPresent(lead::setInstagramUrl);
                enrichmentService.findFacebookUrl(r.website()).ifPresent(lead::setFacebookUrl);
            }

            saved.add(leadRepository.save(lead));
        }
        return saved;
    }

    private ExtractResponse toResponse(UUID searchId, int creditsRemaining, List<Lead> leads) {
        ExtractResponse res = new ExtractResponse();
        res.setSearchHistoryId(searchId);
        res.setLeadsExtracted(leads.size());
        res.setCreditsCharged(leads.size());
        res.setCreditsRemaining(creditsRemaining);
        List<LeadResponse> dto = new ArrayList<>();
        for (Lead l : leads) {
            dto.add(mapLead(l));
        }
        res.setLeads(dto);
        return res;
    }

    private static LeadResponse mapLead(Lead l) {
        LeadResponse r = new LeadResponse();
        r.setId(l.getId());
        r.setName(l.getName());
        r.setPhone(l.getPhone());
        r.setWebsite(l.getWebsite());
        r.setRating(l.getRating());
        r.setReviewCount(l.getReviewCount());
        r.setInstagramUrl(l.getInstagramUrl());
        r.setFacebookUrl(l.getFacebookUrl());
        r.setMapsUrl(l.getMapsUrl());
        r.setUserContacted(l.isUserContacted());
        r.setContactedAt(l.getContactedAt());
        return r;
    }
}
