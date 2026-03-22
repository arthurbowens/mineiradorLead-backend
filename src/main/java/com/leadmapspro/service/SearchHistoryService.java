package com.leadmapspro.service;

import com.leadmapspro.api.dto.SearchHistoryListItemDto;
import com.leadmapspro.api.dto.UpdateSearchHistoryRequest;
import com.leadmapspro.domain.SearchHistory;
import com.leadmapspro.repository.SearchHistoryRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SearchHistoryService {

    private static final int MAX_PAGE_SIZE = 50;

    private final SearchHistoryRepository searchHistoryRepository;

    public SearchHistoryService(SearchHistoryRepository searchHistoryRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
    }

    public Page<SearchHistoryListItemDto> listForUser(UUID userId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return searchHistoryRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable).map(this::toDto);
    }

    @Transactional
    public SearchHistoryListItemDto update(UUID searchHistoryId, UUID userId, UpdateSearchHistoryRequest req) {
        if (req.getUserContacted() == null && req.getUserNote() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe userContacted e/ou userNote.");
        }
        SearchHistory h =
                searchHistoryRepository.findByIdAndUser_Id(searchHistoryId, userId).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Busca não encontrada."));
        if (req.getUserContacted() != null) {
            h.setUserContacted(req.getUserContacted());
            if (Boolean.TRUE.equals(req.getUserContacted())) {
                h.setContactedAt(Instant.now());
            } else {
                h.setContactedAt(null);
            }
        }
        if (req.getUserNote() != null) {
            String note = req.getUserNote().trim();
            h.setUserNote(note.isEmpty() ? null : note);
        }
        return toDto(searchHistoryRepository.save(h));
    }

    private SearchHistoryListItemDto toDto(SearchHistory h) {
        SearchHistoryListItemDto d = new SearchHistoryListItemDto();
        d.setId(h.getId());
        d.setKeyword(h.getKeyword());
        d.setLocation(h.getLocation());
        d.setFullQuery(h.getFullQuery());
        d.setStatus(h.getStatus());
        d.setLeadsFound(h.getLeadsFound());
        d.setCreditsCharged(h.getCreditsCharged());
        d.setErrorMessage(h.getErrorMessage());
        d.setCreatedAt(h.getCreatedAt());
        d.setUpdatedAt(h.getUpdatedAt());
        d.setUserContacted(h.isUserContacted());
        d.setContactedAt(h.getContactedAt());
        d.setUserNote(h.getUserNote());
        return d;
    }
}
