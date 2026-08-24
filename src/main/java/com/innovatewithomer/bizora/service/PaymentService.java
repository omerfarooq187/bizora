package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.infrastructure.TransactionManager;
import com.innovatewithomer.bizora.model.Payment;
import com.innovatewithomer.bizora.model.PaymentStatus;
import com.innovatewithomer.bizora.model.Sale;
import com.innovatewithomer.bizora.model.SaleStatus;
import com.innovatewithomer.bizora.repository.PaymentRepositoryPort;
import com.innovatewithomer.bizora.repository.SaleRepositoryPort;

import java.util.List;

public class PaymentService {

    private final PaymentRepositoryPort paymentRepository;
    private final SaleRepositoryPort saleRepository;

    public PaymentService(
            PaymentRepositoryPort paymentRepository,
            SaleRepositoryPort saleRepository
    ) {

        if (paymentRepository == null) {
            throw new IllegalArgumentException(
                    "Payment repository cannot be null."
            );
        }

        if (saleRepository == null) {
            throw new IllegalArgumentException(
                    "Sale repository cannot be null."
            );
        }

        this.paymentRepository =
                paymentRepository;

        this.saleRepository =
                saleRepository;
    }

    public Payment addPayment(
            Payment payment
    ) {

        validatePayment(payment);

        return TransactionManager.executeReturning(
                connection -> {

                    Sale sale =
                            saleRepository.findById(
                                    connection,
                                    payment.getSaleId()
                            );

                    if (sale == null) {
                        throw new IllegalArgumentException(
                                "Sale does not exist: "
                                        + payment.getSaleId()
                        );
                    }

                    if (sale.getSaleStatus()
                            == SaleStatus.CANCELLED) {

                        throw new IllegalArgumentException(
                                "Cannot add payment to a cancelled sale."
                        );
                    }

                    List<Payment> existingPayments =
                            paymentRepository.findBySaleId(
                                    connection,
                                    sale.getId()
                            );

                    double alreadyPaid =
                            existingPayments.stream()
                                    .mapToDouble(
                                            Payment::getAmount
                                    )
                                    .sum();

                    double newPaidAmount =
                            alreadyPaid
                                    + payment.getAmount();

                    if (newPaidAmount > sale.getTotal()) {

                        throw new IllegalArgumentException(
                                "Payment exceeds the remaining balance."
                        );
                    }

                    paymentRepository.save(
                            connection,
                            payment
                    );

                    PaymentStatus newStatus =
                            determinePaymentStatus(
                                    newPaidAmount,
                                    sale.getTotal()
                            );

                    saleRepository.updatePaymentStatus(
                            connection,
                            sale.getId(),
                            newStatus
                    );

                    return payment;
                }
        );
    }

    public List<Payment> getPaymentsForSale(
            Long saleId
    ) {

        if (saleId == null) {
            throw new IllegalArgumentException(
                    "Sale ID cannot be null."
            );
        }

        return paymentRepository.findBySaleId(
                saleId
        );
    }

    private void validatePayment(
            Payment payment
    ) {

        if (payment == null) {
            throw new IllegalArgumentException(
                    "Payment cannot be null."
            );
        }

        if (payment.getSaleId() == null) {
            throw new IllegalArgumentException(
                    "Payment must belong to a sale."
            );
        }

        if (payment.getAmount() <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero."
            );
        }

        if (payment.getPaymentMethod() == null) {
            throw new IllegalArgumentException(
                    "Payment method is required."
            );
        }
    }

    private PaymentStatus determinePaymentStatus(
            double paidAmount,
            double saleTotal
    ) {

        if (paidAmount <= 0) {
            return PaymentStatus.UNPAID;
        }

        if (paidAmount < saleTotal) {
            return PaymentStatus.PARTIALLY_PAID;
        }

        return PaymentStatus.PAID;
    }
}