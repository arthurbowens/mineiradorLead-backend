package com.leadmapspro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "search_histories")
public class SearchHistory {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 512)
    private String keyword;

    @Column(nullable = false, length = 512)
    private String location;

    @Column(name = "full_query", nullable = false, length = 1024)
    private String fullQuery;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SearchStatus status = SearchStatus.PENDING;

    @Column(name = "leads_found", nullable = false)
    private int leadsFound = 0;

    @Column(name = "credits_charged", nullable = false)
    private int creditsCharged = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "user_contacted", nullable = false)
    private boolean userContacted = false;

    @Column(name = "contacted_at")
    private Instant contactedAt;

    @Column(name = "user_note", columnDefinition = "TEXT")
    private String userNote;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
