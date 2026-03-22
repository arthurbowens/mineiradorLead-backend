package com.leadmapspro.api;

import com.leadmapspro.api.dto.PagedResponse;
import com.leadmapspro.api.dto.SearchHistoryListItemDto;
import com.leadmapspro.api.dto.UpdateSearchHistoryRequest;
import com.leadmapspro.security.CurrentUserId;
import com.leadmapspro.service.SearchHistoryService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search-histories")
@CrossOrigin(origins = "${leadmaps.cors.allowed-origins:http://localhost:4200}")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;
    private final CurrentUserId currentUserId;

    public SearchHistoryController(SearchHistoryService searchHistoryService, CurrentUserId currentUserId) {
        this.searchHistoryService = searchHistoryService;
        this.currentUserId = currentUserId;
    }

    @GetMapping
    public PagedResponse<SearchHistoryListItemDto> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        UUID userId = currentUserId.require();
        return PagedResponse.from(searchHistoryService.listForUser(userId, page, size));
    }

    @PatchMapping("/{id}")
    public SearchHistoryListItemDto update(
            @PathVariable("id") UUID id, @Valid @RequestBody UpdateSearchHistoryRequest body) {
        UUID userId = currentUserId.require();
        return searchHistoryService.update(id, userId, body);
    }
}
