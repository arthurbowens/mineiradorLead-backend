package com.leadmapspro.api;

import com.leadmapspro.api.dto.StripeCheckoutRequest;
import com.leadmapspro.security.CurrentUserId;
import com.leadmapspro.service.StripeCheckoutService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stripe")
@CrossOrigin(origins = "${leadmaps.cors.allowed-origins:http://localhost:4200}")
public class StripeController {

    private final StripeCheckoutService stripeCheckoutService;
    private final CurrentUserId currentUserId;

    public StripeController(StripeCheckoutService stripeCheckoutService, CurrentUserId currentUserId) {
        this.stripeCheckoutService = stripeCheckoutService;
        this.currentUserId = currentUserId;
    }

    @PostMapping("/checkout")
    public Map<String, String> checkout(@Valid @RequestBody StripeCheckoutRequest request) {
        String url = stripeCheckoutService.createCheckoutSession(currentUserId.require(), request);
        return Map.of("url", url);
    }

    @PostMapping("/portal")
    public Map<String, String> portal() {
        String url = stripeCheckoutService.createBillingPortalSession(currentUserId.require());
        return Map.of("url", url);
    }
}
