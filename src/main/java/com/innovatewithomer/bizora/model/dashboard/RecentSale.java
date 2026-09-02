package com.innovatewithomer.bizora.model.dashboard;

import com.innovatewithomer.bizora.model.PaymentStatus;
import com.innovatewithomer.bizora.model.SaleStatus;

import java.time.LocalDateTime;

public class RecentSale {

    private final Long id;
    private final String invoiceNumber;
    private final String customerName;
    private final double total;
    private final PaymentStatus paymentStatus;
    private final SaleStatus saleStatus;
    private final LocalDateTime createdAt;

    public RecentSale(
            Long id,
            String invoiceNumber,
            double total,
            PaymentStatus paymentStatus,
            SaleStatus saleStatus,
            LocalDateTime createdAt
    ) {
        this(id, invoiceNumber, "Walk-in", total, paymentStatus, saleStatus, createdAt);
    }

    public RecentSale(
            Long id,
            String invoiceNumber,
            String customerName,
            double total,
            PaymentStatus paymentStatus,
            SaleStatus saleStatus,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.customerName = customerName != null && !customerName.isBlank() ? customerName : "Walk-in";
        this.total = total;
        this.paymentStatus = paymentStatus;
        this.saleStatus = saleStatus;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getTotal() {
        return total;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public SaleStatus getSaleStatus() {
        return saleStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}