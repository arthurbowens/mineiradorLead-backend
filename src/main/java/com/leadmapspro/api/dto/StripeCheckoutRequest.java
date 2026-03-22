package com.leadmapspro.api.dto;

import com.leadmapspro.domain.SubscriptionPlanCode;
import jakarta.validation.constraints.NotNull;

public class StripeCheckoutRequest {

    @NotNull
    private SubscriptionPlanCode plan;

    public SubscriptionPlanCode getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlanCode plan) {
        this.plan = plan;
    }
}
