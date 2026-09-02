package com.innovatewithomer.bizora.model.dashboard;

public class DashboardSummary {

    private final double todayRevenue;
    private final long todaySalesCount;
    private final long totalProductCount;
    private final long lowStockCount;
    private final double todayPurchases;
    private final double todayExpenses;
    private final double todayProfit;

    public DashboardSummary(
            double todayRevenue,
            long todaySalesCount,
            long totalProductCount,
            long lowStockCount
    ) {
        this(todayRevenue, todaySalesCount, totalProductCount, lowStockCount, 0, 0, todayRevenue);
    }

    public DashboardSummary(
            double todayRevenue,
            long todaySalesCount,
            long totalProductCount,
            long lowStockCount,
            double todayPurchases,
            double todayExpenses,
            double todayProfit
    ) {
        this.todayRevenue = todayRevenue;
        this.todaySalesCount = todaySalesCount;
        this.totalProductCount = totalProductCount;
        this.lowStockCount = lowStockCount;
        this.todayPurchases = todayPurchases;
        this.todayExpenses = todayExpenses;
        this.todayProfit = todayProfit;
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

    public double getTodayPurchases() {
        return todayPurchases;
    }

    public double getTodayExpenses() {
        return todayExpenses;
    }

    public double getTodayProfit() {
        return todayProfit;
    }
}