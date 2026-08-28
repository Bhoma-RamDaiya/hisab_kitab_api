package com.hisabkitab.backend.billing.domain;

import com.hisabkitab.backend.order.domain.OrderEntity;
import com.hisabkitab.backend.order.domain.OrderItemEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "bill_items",
        indexes = {
                @Index(
                        name = "idx_bill_item_bill",
                        columnList = "bill_id"
                ),
                @Index(
                        name = "idx_bill_item_order",
                        columnList = "order_id"
                ),
                @Index(
                        name = "idx_bill_item_order_item",
                        columnList = "order_item_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bill_id",
            nullable = false
    )
    private BillEntity bill;

    /*
     * Original order from which this bill item came.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    /*
     * Exact order item that was billed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItemEntity orderItem;

    /*
     * Product/item name copied into the bill.
     *
     * This is a historical snapshot.
     */
    @Column(nullable = false)
    private String itemName;

    /*
     * Quantity billed.
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal quantity;

    /*
     * Selling rate used for this bill item.
     *
     * Historical snapshot.
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal rate;

    /*
     * quantity × rate
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal amount;
}