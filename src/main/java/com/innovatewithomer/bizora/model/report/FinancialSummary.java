package com.innovatewithomer.bizora.model.report;

import java.time.LocalDate;

public class FinancialSummary {

    private LocalDate from;
    private LocalDate to;

    private double totalSales;
    private double totalPurchases;
    private double totalExpenses;

    private double grossProfit;
    private double netProfit;

    public FinancialSummary() {
    }

    public FinancialSummary(
            LocalDate from,
            LocalDate to,
            double totalSales,
            double totalPurchases,
            double totalExpenses
    ) {
        this.from = from;
        this.to = to;

        this.totalSales = totalSales;
        this.totalPurchases = totalPurchases;
        this.totalExpenses = totalExpenses;

        calculateProfits();
    }

    public void calculateProfits() {

        grossProfit =
                totalSales
                        - totalPurchases;

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
    }

    public double getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(double totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
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