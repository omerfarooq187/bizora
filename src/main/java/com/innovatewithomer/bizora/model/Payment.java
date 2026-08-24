package com.innovatewithomer.bizora.model;

import java.time.LocalDateTime;

public class Payment {

    private Long id;

    private Long saleId;

    private double amount;

    private PaymentMethod paymentMethod;

    private String reference;

    private LocalDateTime createdAt;

    public Payment() {

        this.createdAt =
                LocalDateTime.now();
    }

    public Payment(
            Long saleId,
            double amount,
            PaymentMethod paymentMethod
    ) {

        this();

        this.saleId = saleId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(
            PaymentMethod paymentMethod
    ) {

        this.paymentMethod = paymentMethod;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {

        this.createdAt = createdAt;
    }
}