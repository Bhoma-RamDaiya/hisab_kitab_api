package com.hisabkitab.backend.order.domain;

import com.hisabkitab.backend.product.domain.MeasurementDefinitionEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "order_item_measurements",
        indexes = {
                @Index(
                        name = "idx_order_item_measurement_item",
                        columnList = "order_item_id"
                ),
                @Index(
                        name = "idx_order_item_measurement_definition",
                        columnList = "measurement_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemMeasurementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItemEntity orderItem;

    /**
     * Original measurement definition from the organization's
     * product configuration.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measurement_id")
    private MeasurementDefinitionEntity measurement;

    /**
     * Snapshot of the measurement name.
     *
     * Important:
     * Even if the organization later renames the measurement,
     * the old order still displays the original name.
     */
    @Column(nullable = false, length = 100)
    private String measurementName;

    /**
     * Snapshot of the unit.
     */
    @Column(length = 50)
    private String unit;

    /**
     * Actual value given for this particular order.
     *
     * Example:
     * Chest = 42
     * Sleeve = 16
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;
}