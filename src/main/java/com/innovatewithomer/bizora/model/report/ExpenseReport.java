package com.innovatewithomer.bizora.model.report;

import java.util.Collections;
import java.util.Map;

public class ExpenseReport {

    private final double totalExpenses;
    private final long expenseCount;
    private final Map<String, Double> expensesByCategory;

    public ExpenseReport(
            double totalExpenses,
            long expenseCount,
            Map<String, Double> expensesByCategory
    ) {
        this.totalExpenses = totalExpenses;
        this.expenseCount = expenseCount;
        this.expensesByCategory =
                expensesByCategory == null
                        ? Collections.emptyMap()
                        : Map.copyOf(expensesByCategory);
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public long getExpenseCount() {
        return expenseCount;
    }

    public Map<String, Double> getExpensesByCategory() {
        return expensesByCategory;
    }
}