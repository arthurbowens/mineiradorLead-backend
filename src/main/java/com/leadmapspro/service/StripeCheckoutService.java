package com.leadmapspro.service;

import com.leadmapspro.api.dto.StripeCheckoutRequest;
import com.leadmapspro.config.LeadMapsProperties;
import com.leadmapspro.domain.AppUser;
import com.leadmapspro.domain.SubscriptionPlanCode;
import com.leadmapspro.repository.AppUserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StripeCheckoutService {

    private final LeadMapsProperties properties;
    private final AppUserRepository userRepository;

    public StripeCheckoutService(LeadMapsProperties properties, AppUserRepository userRepository) {
        this.properties = properties;
        this.userRepository = userRepository;
    }

    public String createCheckoutSession(UUID authenticatedUserId, StripeCheckoutRequest req) {
        LeadMapsProperties.Stripe s = properties.getStripe();
        if (s.getSecretKey() == null
                || s.getSecretKey().isBlank()
                || s.getSecretKey().contains("placeholder")) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Stripe não configurado. Defina STRIPE_SECRET_KEY e os price IDs.");
        }
        Stripe.apiKey = s.getSecretKey();

        AppUser user = userRepository
                .findById(authenticatedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        String priceId = priceIdForPlan(s, req.getPlan());
        if (priceId == null || priceId.contains("placeholder")) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Price ID Stripe ausente para o plano " + req.getPlan());
        }

        try {
            SessionCreateParams.Builder b = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(s.getSuccessUrl())
                    .setCancelUrl(s.getCancelUrl())
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPrice(priceId)
                            .build())
                    .putMetadata("user_id", user.getId().toString())
                    .putMetadata("plan", req.getPlan().name());

            if (user.getStripeCustomerId() != null && !user.getStripeCustomerId().isBlank()) {
                b.setCustomer(user.getStripeCustomerId());
            } else {
                b.setCustomerEmail(user.getEmail());
            }

            Session session = Session.create(b.build());
            return session.getUrl();
        } catch (StripeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Erro ao criar sessão Stripe: " + e.getMessage(), e);
        }
    }

    public String createBillingPortalSession(UUID userId) {
        LeadMapsProperties.Stripe s = properties.getStripe();
        Stripe.apiKey = s.getSecretKey();
        AppUser user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        if (user.getStripeCustomerId() == null || user.getStripeCustomerId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cliente Stripe ainda não vinculado a este usuário.");
        }
        try {
            com.stripe.param.billingportal.SessionCreateParams params =
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(user.getStripeCustomerId())
                            .setReturnUrl(s.getPortalReturnUrl())
                            .build();
            com.stripe.model.billingportal.Session portal =
                    com.stripe.model.billingportal.Session.create(params);
            return portal.getUrl();
        } catch (StripeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Erro no portal Stripe: " + e.getMessage(), e);
        }
    }

    private static String priceIdForPlan(LeadMapsProperties.Stripe s, SubscriptionPlanCode plan) {
        return switch (plan) {
            case STARTER -> s.getPriceStarter();
            case PRO -> s.getPricePro();
            case AGENCY -> s.getPriceAgency();
        };
    }
}
