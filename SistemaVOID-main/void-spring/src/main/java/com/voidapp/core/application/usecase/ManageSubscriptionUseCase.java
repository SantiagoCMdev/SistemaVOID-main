package com.voidapp.core.application.usecase;

import com.voidapp.core.domain.exception.UserNotFoundError;
import com.voidapp.domain.model.Invoice;
import com.voidapp.domain.model.Plan;
import com.voidapp.domain.model.Subscription;
import com.voidapp.infrastructure.repository.InvoiceRepository;
import com.voidapp.infrastructure.repository.PlanRepository;
import com.voidapp.infrastructure.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManageSubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final InvoiceRepository invoiceRepository;

    public record ChangePlanInput(UUID userId, UUID newPlanId, String idempotencyKey) {}

    @Transactional
    @SuppressWarnings("null")
    public Subscription subscribeInitial(UUID userId, UUID planId, String idempotencyKey) {
        Optional<Subscription> currentSub = subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                userId, "ACTIVE", ZonedDateTime.now());

        if (currentSub.isPresent()) {
            throw new com.voidapp.core.domain.exception.ActiveSubscriptionExistsError("El usuario ya tiene una suscripción activa.");
        }

        Plan plan = planRepository.findByIdAndIsActiveTrue(planId)
                .orElseThrow(() -> new UserNotFoundError("Plan no encontrado o no disponible."));

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime end = now.plusDays(30);

        Subscription newSub = Subscription.builder()
                .userId(userId)
                .plan(plan)
                .status("ACTIVE")
                .currentPeriodStart(now)
                .currentPeriodEnd(end)
                .build();

        newSub = subscriptionRepository.save(newSub);

        Invoice invoice = Invoice.builder()
                .userId(userId)
                .subscriptionId(newSub.getId())
                .idempotencyKey(idempotencyKey)
                .amountCents(plan.getPriceCents())
                .currency(plan.getCurrency())
                .status("SUCCEEDED") // Simulado
                .billingReason("NEW_SUBSCRIPTION")
                .build();

        invoiceRepository.save(invoice);

        return newSub;
    }

    @Transactional
    @SuppressWarnings("null")
    public Map<String, Object> changePlan(ChangePlanInput input) {
        Subscription currentSub = subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                input.userId(), "ACTIVE", ZonedDateTime.now())
                .orElseThrow(() -> new UserNotFoundError("No se encontró una suscripción activa para este usuario."));

        Plan targetPlan = planRepository.findByIdAndIsActiveTrue(input.newPlanId())
                .orElseThrow(() -> new UserNotFoundError("El plan seleccionado no existe o no está disponible."));

        ZonedDateTime now = ZonedDateTime.now();
        boolean isUpgrade = targetPlan.getPriceCents() > currentSub.getPlan().getPriceCents();

        if (isUpgrade) {
            long totalPeriodMs = currentSub.getCurrentPeriodEnd().toInstant().toEpochMilli() - currentSub.getCurrentPeriodStart().toInstant().toEpochMilli();
            long remainingMs = Math.max(0, currentSub.getCurrentPeriodEnd().toInstant().toEpochMilli() - now.toInstant().toEpochMilli());
            double remainingFraction = (double) remainingMs / totalPeriodMs;

            int unusedCreditCents = (int) Math.round(currentSub.getPlan().getPriceCents() * remainingFraction);
            int newPlanCostFractionCents = (int) Math.round(targetPlan.getPriceCents() * remainingFraction);
            int prorationChargeCents = Math.max(0, newPlanCostFractionCents - unusedCreditCents);

            Invoice invoice = Invoice.builder()
                    .userId(input.userId())
                    .subscriptionId(currentSub.getId())
                    .idempotencyKey(input.idempotencyKey())
                    .amountCents(prorationChargeCents)
                    .currency(targetPlan.getCurrency())
                    .status("SUCCEEDED")
                    .billingReason("UPGRADE_PRORATION")
                    .build();
            
            invoiceRepository.save(invoice);

            currentSub.setPlan(targetPlan);
            currentSub.setPendingDowngradePlanId(null);
            subscriptionRepository.save(currentSub);

            return Map.of(
                    "type", "UPGRADE_APPLIED",
                    "subscription", currentSub,
                    "prorationChargedCents", prorationChargeCents,
                    "effectiveDate", now
            );
        } else {
            currentSub.setPendingDowngradePlanId(targetPlan.getId());
            subscriptionRepository.save(currentSub);

            return Map.of(
                    "type", "DOWNGRADE_SCHEDULED",
                    "subscription", currentSub,
                    "effectiveDate", currentSub.getCurrentPeriodEnd(),
                    "message", "El plan cambiará a " + targetPlan.getName() + " el " + currentSub.getCurrentPeriodEnd()
            );
        }
    }

    @Transactional
    public Map<String, Object> cancelAutoRenewal(UUID userId) {
        Subscription sub = subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                userId, "ACTIVE", ZonedDateTime.now())
                .orElseThrow(() -> new UserNotFoundError("No se encontró una suscripción activa para cancelar."));

        sub.setCancelAtPeriodEnd(true);
        sub.setCanceledAt(ZonedDateTime.now());
        subscriptionRepository.save(sub);

        return Map.of(
                "message", "Renovación automática cancelada. Tu acceso continuará activo hasta la fecha de expiración.",
                "accessUntil", sub.getCurrentPeriodEnd()
        );
    }
}
