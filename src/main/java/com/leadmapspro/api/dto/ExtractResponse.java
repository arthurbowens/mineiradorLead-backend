package com.leadmapspro.api.dto;

import java.util.List;
import java.util.UUID;

public class ExtractResponse {

    private UUID searchHistoryId;
    private int leadsExtracted;
    private int creditsCharged;
    private int creditsRemaining;
    private List<LeadResponse> leads;

    public UUID getSearchHistoryId() {
        return searchHistoryId;
    }

    public void setSearchHistoryId(UUID searchHistoryId) {
        this.searchHistoryId = searchHistoryId;
    }

    public int getLeadsExtracted() {
        return leadsExtracted;
    }

    public void setLeadsExtracted(int leadsExtracted) {
        this.leadsExtracted = leadsExtracted;
    }

    public int getCreditsCharged() {
        return creditsCharged;
    }

    public void setCreditsCharged(int creditsCharged) {
        this.creditsCharged = creditsCharged;
    }

    public int getCreditsRemaining() {
        return creditsRemaining;
    }

    public void setCreditsRemaining(int creditsRemaining) {
        this.creditsRemaining = creditsRemaining;
    }

    public List<LeadResponse> getLeads() {
        return leads;
    }

    public void setLeads(List<LeadResponse> leads) {
        this.leads = leads;
    }
}
