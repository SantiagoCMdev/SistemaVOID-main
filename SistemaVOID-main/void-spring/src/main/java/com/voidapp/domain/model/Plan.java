package com.voidapp.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "price_cents", nullable = false)
    private Integer priceCents;

    @Column(name = "max_concurrent_streams", nullable = false)
    @Builder.Default
    private Integer maxConcurrentStreams = 1;

    @Column(name = "max_resolution", nullable = false, length = 10)
    @Builder.Default
    private String maxResolution = "1080p";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "USD";
}
