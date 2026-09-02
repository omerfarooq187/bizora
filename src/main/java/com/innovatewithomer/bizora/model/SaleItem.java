package com.innovatewithomer.bizora.model;

public class SaleItem {

    private Long id;

    private Long saleId;

    private Long productId;

    private double quantity;

    private double unitPrice;

    private double costPrice;

    private double discount;

    private double subtotal;

    public SaleItem() {
    }

    public SaleItem(
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

    public SaleItem(
            Long productId,
            double quantity,
            double unitPrice,
            double costPrice,
            double discount
    ) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.costPrice = costPrice;
        this.discount = discount;
        calculateSubtotal();
    }

    public SaleItem(
            Long id,
            Long saleId,
            Long productId,
            double quantity,
            double unitPrice,
            double discount,
            double subtotal
    ) {
        this.id = id;
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.subtotal = subtotal;
    }

    public SaleItem(
            Long id,
            Long saleId,
            Long productId,
            double quantity,
            double unitPrice,
            double costPrice,
            double discount,
            double subtotal
    ) {
        this.id = id;
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.costPrice = costPrice;
        this.discount = discount;
        this.subtotal = subtotal;
    }

    private void calculateSubtotal() {

        this.subtotal =
                (quantity * unitPrice) - discount;
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
        calculateSubtotal();
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        calculateSubtotal();
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
        calculateSubtotal();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
