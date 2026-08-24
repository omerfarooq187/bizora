package com.innovatewithomer.bizora.model;

public class PurchaseItem {

    private Long id;

    private Long purchaseId;

    private Long productId;

    private double quantity;

    private double unitPrice;

    private double discount;

    private double subtotal;

    public PurchaseItem() {
    }

    public PurchaseItem(
            Long productId,
            double quantity,
            double unitPrice,
            double discount
    ) {

        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount;

        calculateSubtotal();
    }

    public PurchaseItem(
            Long id,
            Long purchaseId,
            Long productId,
            double quantity,
            double unitPrice,
            double discount,
            double subtotal
    ) {

        this.id = id;
        this.purchaseId = purchaseId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.subtotal = subtotal;
    }

    public void calculateSubtotal() {

        subtotal =
                (quantity * unitPrice)
                        - discount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Long purchaseId) {
        this.purchaseId = purchaseId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}