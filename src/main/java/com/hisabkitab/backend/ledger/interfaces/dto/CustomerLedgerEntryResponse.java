package com.hisabkitab.backend.ledger.interfaces.dto;

import com.hisabkitab.backend.ledger.domain.LedgerEntryType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerLedgerEntryResponse {

    private Long id;

    private LocalDateTime date;

    private LedgerEntryType type;

    private String reference;

    private String description;

    private BigDecimal debit;

    private BigDecimal credit;

    private BigDecimal balance;

    /*
     * Present for BILL entries.
     */
    private Long billId;

    private String billNumber;

    /*
     * Present for PAYMENT entries.
     */
    private Long paymentId;

    /*
     * Orders included inside this bill.
     */
    @Builder.Default
    private List<LedgerOrderReferenceResponse> orders =
            new ArrayList<>();

    /*
     * Bills to which this payment was allocated.
     */
    @Builder.Default
    private List<LedgerBillReferenceResponse> bills =
            new ArrayList<>();
}