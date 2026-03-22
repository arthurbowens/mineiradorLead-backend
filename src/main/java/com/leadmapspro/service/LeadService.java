package com.leadmapspro.service;

import com.leadmapspro.api.dto.LeadResponse;
import com.leadmapspro.api.dto.UpdateLeadRequest;
import com.leadmapspro.domain.Lead;
import com.leadmapspro.repository.LeadRepository;
import com.leadmapspro.repository.SearchHistoryRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    public LeadService(LeadRepository leadRepository, SearchHistoryRepository searchHistoryRepository) {
        this.leadRepository = leadRepository;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    public List<LeadResponse> listBySearchHistory(UUID searchHistoryId, UUID userId) {
        if (!searchHistoryRepository.existsByIdAndUser_Id(searchHistoryId, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Busca não encontrada.");
        }
        List<Lead> leads = leadRepository.findBySearchHistory_IdOrderByCreatedAtAsc(searchHistoryId);
        List<LeadResponse> out = new ArrayList<>(leads.size());
        for (Lead l : leads) {
            out.add(toResponse(l));
        }
        return out;
    }

    @Transactional
    public LeadResponse update(UUID leadId, UUID userId, UpdateLeadRequest req) {
        if (req.getUserContacted() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe userContacted.");
        }
        Lead lead =
                leadRepository
                        .findByIdAndSearchHistory_User_Id(leadId, userId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead não encontrado."));
        lead.setUserContacted(req.getUserContacted());
        if (Boolean.TRUE.equals(req.getUserContacted())) {
            lead.setContactedAt(Instant.now());
        } else {
            lead.setContactedAt(null);
        }
        return toResponse(leadRepository.save(lead));
    }

    @Transactional
    public void deleteIfOwned(UUID leadId, UUID userId) {
        Lead lead =
                leadRepository
                        .findByIdAndSearchHistory_User_Id(leadId, userId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead não encontrado."));
        leadRepository.delete(lead);
    }

    public LeadResponse toResponse(Lead l) {
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
