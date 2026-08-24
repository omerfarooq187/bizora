package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.report.ExpenseReport;
import com.innovatewithomer.bizora.repository.ExpenseReportRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseReportServiceTest {

    @Test
    void shouldReturnExpenseReport() {

        ExpenseReportService service = getService();

        ExpenseReport report =
                service.getExpenseReport(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31)
                );

        assertNotNull(report);

        assertEquals(
                15000,
                report.getTotalExpenses()
        );

        assertEquals(
                3,
                report.getExpenseCount()
        );

        assertEquals(
                10000,
                report.getExpensesByCategory()
                        .get("Rent")
        );

        assertEquals(
                5000,
                report.getExpensesByCategory()
                        .get("Utilities")
        );
    }

    private static ExpenseReportService getService() {
        ExpenseReportRepositoryPort repository =
                (from, to) ->
                        new ExpenseReport(
                                15000,
                                3,
                                Map.of(
                                        "Rent",
                                        10000.0,
                                        "Utilities",
                                        5000.0
                                )
                        );

        ExpenseReportService service =
                new ExpenseReportService(
                        repository
                );
        return service;
    }

    @Test
    void shouldRejectNullRepository() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new ExpenseReportService(null)
        );
    }

    @Test
    void shouldRejectNullFromDate() {

        ExpenseReportRepositoryPort repository =
                (from, to) ->
                        new ExpenseReport(
                                0,
                                0,
                                Map.of()
                        );

        ExpenseReportService service =
                new ExpenseReportService(
                        repository
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getExpenseReport(
                        null,
                        LocalDate.of(2026, 1, 31)
                )
        );
    }

    @Test
    void shouldRejectNullToDate() {

        ExpenseReportRepositoryPort repository =
                (from, to) ->
                        new ExpenseReport(
                                0,
                                0,
                                Map.of()
                        );

        ExpenseReportService service =
                new ExpenseReportService(
                        repository
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getExpenseReport(
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );
    }

    @Test
    void shouldRejectInvalidDateRange() {

        ExpenseReportRepositoryPort repository =
                (from, to) ->
                        new ExpenseReport(
                                0,
                                0,
                                Map.of()
                        );

        ExpenseReportService service =
                new ExpenseReportService(
                        repository
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getExpenseReport(
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 1, 1)
                )
        );
    }
}