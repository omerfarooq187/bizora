package com.innovatewithomer.bizora.model.report;

import java.time.LocalDate;

public class FinancialSummary {

    private LocalDate from;
    private LocalDate to;

    private double totalSales;
    private double totalCostOfGoodsSold;
    private double totalExpenses;

    private double grossProfit;
    private double netProfit;

    public FinancialSummary() {
    }

    public FinancialSummary(
            LocalDate from,
            LocalDate to,
            double totalSales,
            double totalCostOfGoodsSold,
            double totalExpenses
    ) {
        this.from = from;
        this.to = to;

        this.totalSales = totalSales;
        this.totalCostOfGoodsSold = totalCostOfGoodsSold;
        this.totalExpenses = totalExpenses;

        calculateProfits();
    }

    public void calculateProfits() {

        grossProfit =
                totalSales
                        - totalCostOfGoodsSold;

        netProfit =
                grossProfit
                        - totalExpenses;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
        calculateProfits();
    }

    public double getTotalCostOfGoodsSold() {
        return totalCostOfGoodsSold;
    }

    public void setTotalCostOfGoodsSold(double totalCostOfGoodsSold) {
        this.totalCostOfGoodsSold = totalCostOfGoodsSold;
        calculateProfits();
    }

    /** @deprecated Use {@link #getTotalCostOfGoodsSold()}. */
    @Deprecated
    public double getTotalPurchases() {
        return totalCostOfGoodsSold;
    }

    /** @deprecated Use {@link #setTotalCostOfGoodsSold(double)}. */
    @Deprecated
    public void setTotalPurchases(double totalPurchases) {
        setTotalCostOfGoodsSold(totalPurchases);
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
        calculateProfits();
    }

    public double getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public double getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(double netProfit) {
        this.netProfit = netProfit;
    }
}
