package com.innovatewithomer.bizora.model.report;

public class PurchaseReport {

    private final long totalPurchases;
    private final double totalCost;
    private final double averagePurchaseValue;

    public PurchaseReport(
            long totalPurchases,
            double totalCost,
            double averagePurchaseValue
    ) {
        this.totalPurchases = totalPurchases;
        this.totalCost = totalCost;
        this.averagePurchaseValue = averagePurchaseValue;
    }

    public long getTotalPurchases() {
        return totalPurchases;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public double getAveragePurchaseValue() {
        return averagePurchaseValue;
    }
}