package com.innovatewithomer.bizora.model.dashboard;

public class LowStockProduct {

    private final Long id;
    private final String name;
    private final String sku;
    private final double stockQuantity;

    public LowStockProduct(
            Long id,
            String name,
            String sku,
            double stockQuantity
    ) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.stockQuantity = stockQuantity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public double getStockQuantity() {
        return stockQuantity;
    }
}