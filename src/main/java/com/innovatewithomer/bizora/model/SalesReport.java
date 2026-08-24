package com.innovatewithomer.bizora.model;

public class SalesReport {

    private final long totalSales;
    private final double totalRevenue;
    private final double averageSaleValue;

    public SalesReport(
            long totalSales,
            double totalRevenue,
            double averageSaleValue
    ) {
        this.totalSales = totalSales;
        this.totalRevenue = totalRevenue;
        this.averageSaleValue = averageSaleValue;
    }

    public long getTotalSales() {
        return totalSales;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public double getAverageSaleValue() {
        return averageSaleValue;
    }
}