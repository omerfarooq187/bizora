package com.innovatewithomer.bizora.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Purchase {

    private Long id;

    private Long supplierId;

    private String invoiceNumber;

    private double subtotal;

    private double discount;

    private double tax;

    private double total;

    private PaymentStatus paymentStatus;

    private PurchaseStatus purchaseStatus;

    private LocalDateTime createdAt;

    private List<PurchaseItem> items = new ArrayList<>();

    public Purchase() {

        this.paymentStatus =
                PaymentStatus.UNPAID;

        this.purchaseStatus =
                PurchaseStatus.COMPLETED;

        this.createdAt =
                LocalDateTime.now();
    }

    public Purchase(String invoiceNumber) {

        this();

        this.invoiceNumber =
                invoiceNumber;
    }

    public void addItem(PurchaseItem item) {

        items.add(item);
    }

    public void calculateTotals() {

        subtotal = 0;

        for (PurchaseItem item : items) {

            item.calculateSubtotal();

            subtotal +=
                    item.getSubtotal();
        }

        total =
                subtotal
                        - discount
                        + tax;
    }

    public Long getId() {
        return id;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
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
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
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

    public PurchaseStatus getPurchaseStatus() {
        return purchaseStatus;
    }

    public void setPurchaseStatus(
            PurchaseStatus purchaseStatus
    ) {
        this.purchaseStatus = purchaseStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }

    public List<PurchaseItem> getItems() {
        return items;
    }

    public void setItems(
            List<PurchaseItem> items
    ) {
        this.items = items;
    }
}