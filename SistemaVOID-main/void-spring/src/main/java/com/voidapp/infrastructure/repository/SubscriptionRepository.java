package com.voidapp.infrastructure.repository;

import com.voidapp.domain.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findFirstByUserIdAndStatusAndCurrentPeriodEndGreaterThanEqual(UUID userId, String status, ZonedDateTime now);
}
