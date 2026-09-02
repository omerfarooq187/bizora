package com.innovatewithomer.bizora.model.report;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FinancialSummaryTest {

    @Test
    void shouldCalculateGrossProfit() {

        FinancialSummary summary =
                new FinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31),
                        100000,
                        60000,
                        10000
                );

        assertEquals(
                40000,
                summary.getGrossProfit()
        );
    }

    @Test
    void shouldCalculateNetProfit() {

        FinancialSummary summary =
                new FinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31),
                        100000,
                        60000,
                        10000
                );

        assertEquals(
                30000,
                summary.getNetProfit()
        );
    }

    @Test
    void shouldCalculateProfitWhenThereAreNoExpenses() {

        FinancialSummary summary =
                new FinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31),
                        100000,
                        60000,
                        0
                );

        assertEquals(
                40000,
                summary.getGrossProfit()
        );

        assertEquals(
                40000,
                summary.getNetProfit()
        );
    }

    @Test
    void shouldAllowNegativeNetProfit() {

        FinancialSummary summary =
                new FinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31),
                        50000,
                        40000,
                        20000
                );

        assertEquals(
                10000,
                summary.getGrossProfit()
        );

        assertEquals(
                -10000,
                summary.getNetProfit()
        );
    }

    @Test
    void shouldStoreReportPeriod() {

        LocalDate from =
                LocalDate.of(2026, 8, 1);

        LocalDate to =
                LocalDate.of(2026, 8, 31);

        FinancialSummary summary =
                new FinancialSummary(
                        from,
                        to,
                        100000,
                        60000,
                        10000
                );

        assertEquals(
                from,
                summary.getFrom()
        );

        assertEquals(
                to,
                summary.getTo()
        );
    }

    @Test
    void shouldStoreFinancialValues() {

        FinancialSummary summary =
                new FinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31),
                        100000,
                        60000,
                        10000
                );

        assertEquals(
                100000,
                summary.getTotalSales()
        );

        assertEquals(
                60000,
                summary.getTotalCostOfGoodsSold()
        );

        assertEquals(
                10000,
                summary.getTotalExpenses()
        );
    }

    @Test
    void shouldRecalculateProfitsAfterValuesChange() {

        FinancialSummary summary =
                new FinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31),
                        100000,
                        60000,
                        10000
                );

        summary.setTotalSales(150000);
        summary.setTotalCostOfGoodsSold(80000);
        summary.setTotalExpenses(20000);

        summary.calculateProfits();

        assertEquals(
                70000,
                summary.getGrossProfit()
        );

        assertEquals(
                50000,
                summary.getNetProfit()
        );
    }

    @Test
    void shouldCreateEmptySummary() {

        FinancialSummary summary =
                new FinancialSummary();

        assertNull(
                summary.getFrom()
        );

        assertNull(
                summary.getTo()
        );

        assertEquals(
                0,
                summary.getTotalSales()
        );

        assertEquals(
                0,
                summary.getTotalCostOfGoodsSold()
        );

        assertEquals(
                0,
                summary.getTotalExpenses()
        );

        assertEquals(
                0,
                summary.getGrossProfit()
        );

        assertEquals(
                0,
                summary.getNetProfit()
        );
    }
}
