package com.hisabkitab.backend.billing.application;

import com.hisabkitab.backend.billing.domain.BillEntity;
import com.hisabkitab.backend.billing.infrastructure.BillPaymentAllocationRepository;
import com.hisabkitab.backend.payment.infrastructure.CustomerCreditAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BillPaymentCalculationService {

    private final BillPaymentAllocationRepository
            billPaymentAllocationRepository;

    private final CustomerCreditAllocationRepository
            customerCreditAllocationRepository;


    /*
     * Calculates the actual remaining amount
     * that can still be paid against a bill.
     *
     * Includes:
     *
     * 1. Direct payment allocations
     * 2. Customer credit allocations
     */
    public BigDecimal getBillRemainingAmount(
            BillEntity bill) {

        BigDecimal directPayments =
                billPaymentAllocationRepository
                        .sumAllocatedAmountByBill(
                                bill.getId()
                        );

        if (directPayments == null) {
            directPayments = BigDecimal.ZERO;
        }


        BigDecimal creditPayments =
                customerCreditAllocationRepository
                        .sumCreditAllocatedToBill(
                                bill.getId()
                        );

        if (creditPayments == null) {
            creditPayments = BigDecimal.ZERO;
        }


        BigDecimal totalPaid =
                directPayments
                        .add(creditPayments);


        return bill.getTotalAmount()
                .subtract(totalPaid)
                .max(BigDecimal.ZERO);
    }

    public BigDecimal getDirectPaymentAmount(Long billId) {

        BigDecimal directPayments =
                billPaymentAllocationRepository
                        .sumAllocatedAmountByBill(billId);

        return directPayments != null
                ? directPayments
                : BigDecimal.ZERO;
    }

    //    private BigDecimal getBillRemainingAmount(
    //            BillEntity bill) {
    //
    //        BigDecimal directPayments =
    //                billPaymentAllocationRepository
    //                        .sumAllocatedAmountByBill(
    //                                bill.getId()
    //                        );
    //
    //        if (directPayments == null) {
    //            directPayments = BigDecimal.ZERO;
    //        }
    //
    //        BigDecimal creditPayments =
    //                customerCreditAllocationRepository
    //                        .sumCreditAllocatedToBill(
    //                                bill.getId()
    //                        );
    //
    //        if (creditPayments == null) {
    //            creditPayments = BigDecimal.ZERO;
    //        }
    //
    //        BigDecimal totalPaid =
    //                directPayments
    //                        .add(creditPayments);
    //
    //        return bill.getTotalAmount()
    //                .subtract(totalPaid)
    //                .max(BigDecimal.ZERO);
    //    }


}