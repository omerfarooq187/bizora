package com.innovatewithomer.bizora.model.dashboard;

public class DashboardSummary {

    private final double todayRevenue;
    private final long todaySalesCount;
    private final long totalProductCount;
    private final long lowStockCount;

    public DashboardSummary(
            double todayRevenue,
            long todaySalesCount,
            long totalProductCount,
            long lowStockCount
    ) {
        this.todayRevenue = todayRevenue;
        this.todaySalesCount = todaySalesCount;
        this.totalProductCount = totalProductCount;
        this.lowStockCount = lowStockCount;
    }

    public double getTodayRevenue() {
        return todayRevenue;
    }

    public long getTodaySalesCount() {
        return todaySalesCount;
    }

    public long getTotalProductCount() {
        return totalProductCount;
    }

    public long getLowStockCount() {
        return lowStockCount;
    }
}