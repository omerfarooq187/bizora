package com.innovatewithomer.bizora.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {

    private Long id;

    private Long customerId;

    private String invoiceNumber;

    private double subtotal;

    private double discount;

    private double tax;

    private double total;

    private PaymentStatus paymentStatus;

    private SaleStatus saleStatus;

    private LocalDateTime createdAt;

    private List<SaleItem> items = new ArrayList<>();

    public Sale() {
        this.createdAt = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.UNPAID;
        this.saleStatus = SaleStatus.COMPLETED;
    }

    public Sale(
            String invoiceNumber
    ) {
        this();

        this.invoiceNumber = invoiceNumber;
    }

    public void addItem(SaleItem item) {

        items.add(item);

        calculateTotals();
    }

    public void removeItem(SaleItem item) {

        items.remove(item);

        calculateTotals();
    }

    public void calculateTotals() {

        subtotal =
                items.stream()
                        .mapToDouble(
                                SaleItem::getSubtotal
                        )
                        .sum();

        total =
                subtotal
                        - discount
                        + tax;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(
            String invoiceNumber
    ) {
        this.invoiceNumber = invoiceNumber;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
        calculateTotals();
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
        calculateTotals();
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(
            PaymentStatus paymentStatus
    ) {
        this.paymentStatus = paymentStatus;
    }

    public SaleStatus getSaleStatus() {
        return saleStatus;
    }

    public void setSaleStatus(
            SaleStatus saleStatus
    ) {
        this.saleStatus = saleStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(
            List<SaleItem> items
    ) {
        this.items = items;

        calculateTotals();
    }
}