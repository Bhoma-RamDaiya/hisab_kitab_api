package com.hisabkitab.backend.billing.application;

import com.hisabkitab.backend.billing.domain.BillEntity;
import com.hisabkitab.backend.billing.domain.BillStatus;
import com.hisabkitab.backend.billing.infrastructure.BillPaymentAllocationRepository;
import com.hisabkitab.backend.billing.infrastructure.BillRepository;
import com.hisabkitab.backend.payment.infrastructure.CustomerCreditAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BillStatusService {

    private final BillRepository billRepository;

    private final BillPaymentAllocationRepository
            billPaymentAllocationRepository;

    private final CustomerCreditAllocationRepository
            customerCreditAllocationRepository;


    @Transactional
    public void updateBillPaymentStatus(
            BillEntity bill) {

        /*
         * Do not modify cancelled bills.
         */
        if (bill.getStatus() ==
                BillStatus.CANCELLED) {

            return;
        }


        /*
         * Do not modify draft bills.
         */
        if (bill.getStatus() ==
                BillStatus.DRAFT) {

            return;
        }


        /*
         * Total amount paid through normal
         * customer payments.
         */
        BigDecimal directPayments =
                billPaymentAllocationRepository
                        .sumAllocatedAmountByBill(
                                bill.getId()
                        );

        if (directPayments == null) {
            directPayments = BigDecimal.ZERO;
        }


        /*
         * Total amount paid using customer credit.
         */
        BigDecimal creditPayments =
                customerCreditAllocationRepository
                        .sumCreditAllocatedToBill(
                                bill.getId()
                        );

        if (creditPayments == null) {
            creditPayments = BigDecimal.ZERO;
        }


        /*
         * Total amount paid toward this bill.
         *
         * Direct payments
         * +
         * Customer credit
         */
        BigDecimal totalPaid =
                directPayments
                        .add(creditPayments);


        /*
         * Nothing has been paid.
         */
        if (totalPaid.compareTo(
                BigDecimal.ZERO) <= 0) {

            bill.setStatus(
                    BillStatus.ISSUED
            );
        }


        /*
         * Entire bill has been paid.
         */
        else if (totalPaid.compareTo(
                bill.getTotalAmount()) >= 0) {

            bill.setStatus(
                    BillStatus.PAID
            );
        }


        /*
         * Some amount has been paid,
         * but the bill is not fully paid.
         */
        else {

            bill.setStatus(
                    BillStatus.PARTIALLY_PAID
            );
        }


        /*
         * Persist the updated bill status.
         */
        billRepository.save(bill);
    }
}