package com.leadmapspro.api.dto;

public class UpdateLeadRequest {

    /** Se null, não altera. */
    private Boolean userContacted;

    public Boolean getUserContacted() {
        return userContacted;
    }

    public void setUserContacted(Boolean userContacted) {
        this.userContacted = userContacted;
    }
}
