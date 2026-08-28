package com.hisabkitab.backend.product.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "product_measurements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_measurement",
                        columnNames = {
                                "product_id",
                                "measurement_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMeasurementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "measurement_id", nullable = false)
    private MeasurementDefinitionEntity measurement;

    @Builder.Default
    @Column(nullable = false)
    private Boolean required = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    private Integer displayOrder;
}