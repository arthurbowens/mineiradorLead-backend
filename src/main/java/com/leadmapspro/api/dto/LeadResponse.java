package com.leadmapspro.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class LeadResponse {

    private UUID id;
    private String name;
    private String phone;
    private String website;
    private BigDecimal rating;
    private Integer reviewCount;
    private String instagramUrl;
    private String facebookUrl;
    private String mapsUrl;
    private boolean userContacted;
    private Instant contactedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getMapsUrl() {
        return mapsUrl;
    }

    public void setMapsUrl(String mapsUrl) {
        this.mapsUrl = mapsUrl;
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
}
