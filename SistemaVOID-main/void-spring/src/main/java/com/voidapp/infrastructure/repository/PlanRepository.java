package com.voidapp.infrastructure.repository;

import com.voidapp.domain.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> findByIdAndIsActiveTrue(UUID id);
}
