package com.leadmapspro.api.dto;

import com.leadmapspro.domain.SearchStatus;
import java.time.Instant;
import java.util.UUID;

public class SearchHistoryListItemDto {

    private UUID id;
    private String keyword;
    private String location;
    private String fullQuery;
    private SearchStatus status;
    private int leadsFound;
    private int creditsCharged;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean userContacted;
    private Instant contactedAt;
    private String userNote;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFullQuery() {
        return fullQuery;
    }

    public void setFullQuery(String fullQuery) {
        this.fullQuery = fullQuery;
    }

    public SearchStatus getStatus() {
        return status;
    }

    public void setStatus(SearchStatus status) {
        this.status = status;
    }

    public int getLeadsFound() {
        return leadsFound;
    }

    public void setLeadsFound(int leadsFound) {
        this.leadsFound = leadsFound;
    }

    public int getCreditsCharged() {
        return creditsCharged;
    }

    public void setCreditsCharged(int creditsCharged) {
        this.creditsCharged = creditsCharged;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isUserContacted() {
        return userContacted;
    }

    public void setUserContacted(boolean userContacted) {
        this.userContacted = userContacted;
    }

    public Instant getContactedAt() {
        return contactedAt;
    }

    public void setContactedAt(Instant contactedAt) {
        this.contactedAt = contactedAt;
    }

    public String getUserNote() {
        return userNote;
    }

    public void setUserNote(String userNote) {
        this.userNote = userNote;
    }
}
