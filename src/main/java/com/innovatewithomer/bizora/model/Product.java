package com.innovatewithomer.bizora.model;

import java.time.LocalDateTime;

public class Product {

    private Long id;

    private String name;

    private String sku;

    private double sellingPrice;

    private double purchasePrice;

    private double stockQuantity;

    private LocalDateTime createdAt;

    public Product() {
    }

    public Product(
            Long id,
            String name,
            String sku,
            double sellingPrice,
            double purchasePrice,
            double stockQuantity,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.sellingPrice = sellingPrice;
        this.purchasePrice = purchasePrice;
        this.stockQuantity = stockQuantity;
        this.createdAt = createdAt;
    }

    public Product(
            String name,
            String sku,
            double sellingPrice,
            double purchasePrice,
            double stockQuantity
    ) {
        this(
                null,
                name,
                sku,
                sellingPrice,
                purchasePrice,
                stockQuantity,
                LocalDateTime.now()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(double stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}