package com.leadmapspro.api.dto;

import jakarta.validation.constraints.Size;

public class UpdateSearchHistoryRequest {

    /** Se null, não altera o flag. */
    private Boolean userContacted;

    @Size(max = 2000)
    private String userNote;

    public Boolean getUserContacted() {
        return userContacted;
    }

    public void setUserContacted(Boolean userContacted) {
        this.userContacted = userContacted;
    }

    public String getUserNote() {
        return userNote;
    }

    public void setUserNote(String userNote) {
        this.userNote = userNote;
    }
}
