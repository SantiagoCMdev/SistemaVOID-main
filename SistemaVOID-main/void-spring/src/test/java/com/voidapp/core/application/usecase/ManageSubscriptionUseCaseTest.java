package com.voidapp.core.application.usecase;

import com.voidapp.core.domain.exception.ActiveSubscriptionExistsError;
import com.voidapp.core.domain.exception.UserNotFoundError;
import com.voidapp.domain.model.Invoice;
import com.voidapp.domain.model.Plan;
import com.voidapp.domain.model.Subscription;
import com.voidapp.infrastructure.repository.InvoiceRepository;
import com.voidapp.infrastructure.repository.PlanRepository;
import com.voidapp.infrastructure.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageSubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private ManageSubscriptionUseCase manageSubscriptionUseCase;

    @Nested
    @DisplayName("subscribeInitial")
    class SubscribeInitialTests {

        @Test
        @DisplayName("Debe crear suscripción y factura exitosamente cuando no hay suscripción activa")
        void shouldSubscribeInitialSuccessfully() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID planId = UUID.randomUUID();
            UUID subId = UUID.randomUUID();
            String idempotencyKey = "idemp_123456";

            Plan plan = Plan.builder()
                    .id(planId)
                    .name("Plan Pro")
                    .priceCents(1499)
                    .currency("USD")
                    .maxConcurrentStreams(2)
                    .maxResolution("4K")
                    .isActive(true)
                    .build();

            when(subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                    eq(userId), eq("ACTIVE"), any(ZonedDateTime.class)))
                    .thenReturn(Optional.empty());

            when(planRepository.findByIdAndIsActiveTrue(planId)).thenReturn(Optional.of(plan));

            when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
                Subscription s = invocation.getArgument(0);
                s.setId(subId);
                return s;
            });

            // Act
            Subscription result = manageSubscriptionUseCase.subscribeInitial(userId, planId, idempotencyKey);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(subId);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getPlan()).isEqualTo(plan);
            assertThat(result.getStatus()).isEqualTo("ACTIVE");
            assertThat(result.getCurrentPeriodStart()).isNotNull();
            assertThat(result.getCurrentPeriodEnd()).isNotNull();

            ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceRepository).save(invoiceCaptor.capture());
            Invoice savedInvoice = invoiceCaptor.getValue();

            assertThat(savedInvoice.getUserId()).isEqualTo(userId);
            assertThat(savedInvoice.getSubscriptionId()).isEqualTo(subId);
            assertThat(savedInvoice.getIdempotencyKey()).isEqualTo(idempotencyKey);
            assertThat(savedInvoice.getAmountCents()).isEqualTo(1499);
            assertThat(savedInvoice.getCurrency()).isEqualTo("USD");
            assertThat(savedInvoice.getStatus()).isEqualTo("SUCCEEDED");
            assertThat(savedInvoice.getBillingReason()).isEqualTo("NEW_SUBSCRIPTION");
        }

        @Test
        @DisplayName("Debe lanzar ActiveSubscriptionExistsError cuando el usuario ya tiene una suscripción activa")
        void shouldThrowActiveSubscriptionExistsErrorWhenAlreadyHasActive() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID planId = UUID.randomUUID();
            Subscription activeSub = Subscription.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .status("ACTIVE")
                    .build();

            when(subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                    eq(userId), eq("ACTIVE"), any(ZonedDateTime.class)))
                    .thenReturn(Optional.of(activeSub));

            // Act & Assert
            assertThatThrownBy(() -> manageSubscriptionUseCase.subscribeInitial(userId, planId, "idem_1"))
                    .isInstanceOf(ActiveSubscriptionExistsError.class)
                    .hasMessage("El usuario ya tiene una suscripción activa.");

            verifyNoInteractions(planRepository);
            verify(invoiceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar UserNotFoundError cuando el plan seleccionado no existe o está inactivo")
        void shouldThrowUserNotFoundErrorWhenPlanNotFoundOrInactive() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID planId = UUID.randomUUID();

            when(subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                    eq(userId), eq("ACTIVE"), any(ZonedDateTime.class)))
                    .thenReturn(Optional.empty());
            when(planRepository.findByIdAndIsActiveTrue(planId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> manageSubscriptionUseCase.subscribeInitial(userId, planId, "idem_1"))
                    .isInstanceOf(UserNotFoundError.class)
                    .hasMessage("Plan no encontrado o no disponible.");

            verify(subscriptionRepository, never()).save(any());
            verify(invoiceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("changePlan")
    class ChangePlanTests {

        @Test
        @DisplayName("Debe aplicar UPGRADE inmediatamente cobrando la prorrata")
        void shouldApplyUpgradeSuccessfully() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID currentPlanId = UUID.randomUUID();
            UUID newPlanId = UUID.randomUUID();
            String idempotencyKey = "upgrade_idem";

            Plan currentPlan = Plan.builder()
                    .id(currentPlanId)
                    .name("Plan Básico")
                    .priceCents(1000)
                    .currency("USD")
                    .build();

            Plan targetPlan = Plan.builder()
                    .id(newPlanId)
                    .name("Plan Premium")
                    .priceCents(2000)
                    .currency("USD")
                    .build();

            ZonedDateTime start = ZonedDateTime.now().minusDays(15);
            ZonedDateTime end = ZonedDateTime.now().plusDays(15);

            Subscription currentSub = Subscription.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .plan(currentPlan)
                    .status("ACTIVE")
                    .currentPeriodStart(start)
                    .currentPeriodEnd(end)
                    .build();

            ManageSubscriptionUseCase.ChangePlanInput input =
                    new ManageSubscriptionUseCase.ChangePlanInput(userId, newPlanId, idempotencyKey);

            when(subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                    eq(userId), eq("ACTIVE"), any(ZonedDateTime.class)))
                    .thenReturn(Optional.of(currentSub));
            when(planRepository.findByIdAndIsActiveTrue(newPlanId)).thenReturn(Optional.of(targetPlan));

            // Act
            Map<String, Object> result = manageSubscriptionUseCase.changePlan(input);

            // Assert
            assertThat(result).containsEntry("type", "UPGRADE_APPLIED");
            assertThat(result).containsKey("prorationChargedCents");
            assertThat(currentSub.getPlan()).isEqualTo(targetPlan);
            assertThat(currentSub.getPendingDowngradePlanId()).isNull();

            verify(invoiceRepository).save(any(Invoice.class));
            verify(subscriptionRepository).save(currentSub);
        }

        @Test
        @DisplayName("Debe programar DOWNGRADE para el fin del período sin cobro inmediato")
        void shouldScheduleDowngradeSuccessfully() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID currentPlanId = UUID.randomUUID();
            UUID newPlanId = UUID.randomUUID();

            Plan currentPlan = Plan.builder()
                    .id(currentPlanId)
                    .name("Plan Premium")
                    .priceCents(2000)
                    .currency("USD")
                    .build();

            Plan targetPlan = Plan.builder()
                    .id(newPlanId)
                    .name("Plan Básico")
                    .priceCents(1000)
                    .currency("USD")
                    .build();

            Subscription currentSub = Subscription.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .plan(currentPlan)
                    .status("ACTIVE")
                    .currentPeriodStart(ZonedDateTime.now().minusDays(10))
                    .currentPeriodEnd(ZonedDateTime.now().plusDays(20))
                    .build();

            ManageSubscriptionUseCase.ChangePlanInput input =
                    new ManageSubscriptionUseCase.ChangePlanInput(userId, newPlanId, "downgrade_idem");

            when(subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                    eq(userId), eq("ACTIVE"), any(ZonedDateTime.class)))
                    .thenReturn(Optional.of(currentSub));
            when(planRepository.findByIdAndIsActiveTrue(newPlanId)).thenReturn(Optional.of(targetPlan));

            // Act
            Map<String, Object> result = manageSubscriptionUseCase.changePlan(input);

            // Assert
            assertThat(result).containsEntry("type", "DOWNGRADE_SCHEDULED");
            assertThat(currentSub.getPendingDowngradePlanId()).isEqualTo(newPlanId);

            verifyNoInteractions(invoiceRepository);
            verify(subscriptionRepository).save(currentSub);
        }

        @Test
        @DisplayName("Debe lanzar UserNotFoundError cuando no hay suscripción activa para cambiar de plan")
        void shouldThrowWhenNoActiveSubscriptionForChangePlan() {
            // Arrange
            UUID userId = UUID.randomUUID();
            ManageSubscriptionUseCase.ChangePlanInput input =
                    new ManageSubscriptionUseCase.ChangePlanInput(userId, UUID.randomUUID(), "key");

            when(subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                    eq(userId), eq("ACTIVE"), any(ZonedDateTime.class)))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> manageSubscriptionUseCase.changePlan(input))
                    .isInstanceOf(UserNotFoundError.class)
                    .hasMessage("No se encontró una suscripción activa para este usuario.");

            verifyNoInteractions(planRepository);
            verify(subscriptionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("cancelAutoRenewal")
    class CancelAutoRenewalTests {

        @Test
        @DisplayName("Debe marcar la suscripción para no renovar al fin del período")
        void shouldCancelAutoRenewalSuccessfully() {
            // Arrange
            UUID userId = UUID.randomUUID();
            ZonedDateTime periodEnd = ZonedDateTime.now().plusDays(10);
            Subscription sub = Subscription.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .status("ACTIVE")
                    .currentPeriodEnd(periodEnd)
                    .cancelAtPeriodEnd(false)
                    .build();

            when(subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                    eq(userId), eq("ACTIVE"), any(ZonedDateTime.class)))
                    .thenReturn(Optional.of(sub));

            // Act
            Map<String, Object> result = manageSubscriptionUseCase.cancelAutoRenewal(userId);

            // Assert
            assertThat(result).containsKey("message");
            assertThat(result.get("accessUntil")).isEqualTo(periodEnd);
            assertThat(sub.getCancelAtPeriodEnd()).isTrue();
            assertThat(sub.getCanceledAt()).isNotNull();

            verify(subscriptionRepository).save(sub);
        }

        @Test
        @DisplayName("Debe lanzar UserNotFoundError cuando no existe suscripción activa para cancelar renovación")
        void shouldThrowWhenNoActiveSubscriptionToCancel() {
            // Arrange
            UUID userId = UUID.randomUUID();
            when(subscriptionRepository.findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(
                    eq(userId), eq("ACTIVE"), any(ZonedDateTime.class)))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> manageSubscriptionUseCase.cancelAutoRenewal(userId))
                    .isInstanceOf(UserNotFoundError.class)
                    .hasMessage("No se encontró una suscripción activa para cancelar.");

            verify(subscriptionRepository, never()).save(any());
        }
    }
}

