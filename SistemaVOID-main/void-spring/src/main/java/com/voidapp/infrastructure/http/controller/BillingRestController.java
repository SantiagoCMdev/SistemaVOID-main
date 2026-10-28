package com.voidapp.infrastructure.http.controller;

import com.voidapp.core.application.usecase.ManageSubscriptionUseCase;
import com.voidapp.domain.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingRestController {

    private final ManageSubscriptionUseCase manageSubscriptionUseCase;

    public record SubscribeRequest(UUID planId) {}
    public record ChangePlanRequest(UUID newPlanId) {}

    @PostMapping("/subscribe")
    public ResponseEntity<Subscription> subscribeInitial(
            @RequestBody SubscribeRequest request,
            @RequestHeader(value = "idempotency-key", required = false) String idempotencyKey,
            Authentication authentication) {
            
        UUID userId = (UUID) authentication.getPrincipal();
        
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            idempotencyKey = "default-idemp-" + System.currentTimeMillis();
        }

        Subscription result = manageSubscriptionUseCase.subscribeInitial(userId, request.planId(), idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/change-plan")
    public ResponseEntity<Map<String, Object>> changePlan(
            @RequestBody ChangePlanRequest request,
            @RequestHeader("idempotency-key") String idempotencyKey,
            Authentication authentication) {
            
        UUID userId = (UUID) authentication.getPrincipal();

        ManageSubscriptionUseCase.ChangePlanInput input = new ManageSubscriptionUseCase.ChangePlanInput(
                userId, request.newPlanId(), idempotencyKey
        );

        Map<String, Object> result = manageSubscriptionUseCase.changePlan(input);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cancel-renewal")
    public ResponseEntity<Map<String, Object>> cancelRenewal(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        Map<String, Object> result = manageSubscriptionUseCase.cancelAutoRenewal(userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(Map.of("received", true));
    }
}
