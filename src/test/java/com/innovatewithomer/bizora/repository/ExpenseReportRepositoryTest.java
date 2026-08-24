package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.report.ExpenseReport;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseReportRepositoryTest {

    private ExpenseReportRepository expenseReportRepository;

    private Connection keepAliveConnection;

    @BeforeEach
    void setUp() throws Exception {

        DatabaseManager.setJdbcUrl(
                "jdbc:sqlite:file:bizora_test_"
                        + System.nanoTime()
                        + "?mode=memory&cache=shared"
        );

        keepAliveConnection =
                DatabaseManager.getConnection();

        try (Statement statement =
                     keepAliveConnection.createStatement()) {

            statement.execute("""
                PRAGMA foreign_keys = ON
                """);

            // -----------------------------------------
            // EXPENSES
            // -----------------------------------------

            statement.execute("""
                CREATE TABLE expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category TEXT NOT NULL,
                    description TEXT,
                    amount REAL NOT NULL,
                    expense_date TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
                """);
        }

        expenseReportRepository =
                new ExpenseReportRepository();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    @Test
    void shouldCalculateTotalExpenses() {

        insertExpense(
                "Rent",
                "Shop rent",
                10000,
                LocalDate.of(2026, 1, 5)
        );

        insertExpense(
                "Utilities",
                "Electricity bill",
                5000,
                LocalDate.of(2026, 1, 10)
        );

        insertExpense(
                "Transport",
                "Delivery fuel",
                2000,
                LocalDate.of(2026, 1, 15)
        );

        ExpenseReport report =
                expenseReportRepository.getExpenseReport(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31)
                );

        assertEquals(
                17000,
                report.getTotalExpenses()
        );
    }

    @Test
    void shouldCalculateExpenseCount() {

        insertExpense(
                "Rent",
                "Shop rent",
                10000,
                LocalDate.of(2026, 1, 5)
        );

        insertExpense(
                "Utilities",
                "Electricity",
                5000,
                LocalDate.of(2026, 1, 10)
        );

        insertExpense(
                "Transport",
                "Fuel",
                2000,
                LocalDate.of(2026, 1, 15)
        );

        ExpenseReport report =
                expenseReportRepository.getExpenseReport(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31)
                );

        assertEquals(
                3,
                report.getExpenseCount()
        );
    }

    @Test
    void shouldGroupExpensesByCategory() {

        insertExpense(
                "Rent",
                "January rent",
                10000,
                LocalDate.of(2026, 1, 5)
        );

        insertExpense(
                "Rent",
                "Additional rent",
                2000,
                LocalDate.of(2026, 1, 15)
        );

        insertExpense(
                "Utilities",
                "Electricity",
                5000,
                LocalDate.of(2026, 1, 10)
        );

        insertExpense(
                "Transport",
                "Fuel",
                3000,
                LocalDate.of(2026, 1, 20)
        );

        ExpenseReport report =
                expenseReportRepository.getExpenseReport(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31)
                );

        assertEquals(
                12000,
                report.getExpensesByCategory()
                        .get("Rent")
        );

        assertEquals(
                5000,
                report.getExpensesByCategory()
                        .get("Utilities")
        );

        assertEquals(
                3000,
                report.getExpensesByCategory()
                        .get("Transport")
        );
    }

    @Test
    void shouldIgnoreExpensesOutsideDateRange() {

        insertExpense(
                "Rent",
                "December rent",
                10000,
                LocalDate.of(2025, 12, 20)
        );

        insertExpense(
                "Utilities",
                "January electricity",
                5000,
                LocalDate.of(2026, 1, 10)
        );

        insertExpense(
                "Transport",
                "February fuel",
                3000,
                LocalDate.of(2026, 2, 10)
        );

        ExpenseReport report =
                expenseReportRepository.getExpenseReport(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31)
                );

        assertEquals(
                5000,
                report.getTotalExpenses()
        );

        assertEquals(
                1,
                report.getExpenseCount()
        );

        assertEquals(
                5000,
                report.getExpensesByCategory()
                        .get("Utilities")
        );

        assertFalse(
                report.getExpensesByCategory()
                        .containsKey("Rent")
        );

        assertFalse(
                report.getExpensesByCategory()
                        .containsKey("Transport")
        );
    }

    @Test
    void shouldIncludeExpensesOnBoundaryDates() {

        insertExpense(
                "Rent",
                "Start date expense",
                5000,
                LocalDate.of(2026, 1, 1)
        );

        insertExpense(
                "Utilities",
                "End date expense",
                3000,
                LocalDate.of(2026, 1, 31)
        );

        ExpenseReport report =
                expenseReportRepository.getExpenseReport(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31)
                );

        assertEquals(
                8000,
                report.getTotalExpenses()
        );

        assertEquals(
                2,
                report.getExpenseCount()
        );
    }

    @Test
    void shouldReturnEmptyReportWhenNoExpensesExist() {

        ExpenseReport report =
                expenseReportRepository.getExpenseReport(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31)
                );

        assertNotNull(report);

        assertEquals(
                0,
                report.getTotalExpenses()
        );

        assertEquals(
                0,
                report.getExpenseCount()
        );

        assertTrue(
                report.getExpensesByCategory()
                        .isEmpty()
        );
    }

    @Test
    void shouldReturnEmptyReportWhenNoExpensesMatchDateRange() {

        insertExpense(
                "Rent",
                "December rent",
                10000,
                LocalDate.of(2025, 12, 20)
        );

        ExpenseReport report =
                expenseReportRepository.getExpenseReport(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31)
                );

        assertEquals(
                0,
                report.getTotalExpenses()
        );

        assertEquals(
                0,
                report.getExpenseCount()
        );

        assertTrue(
                report.getExpensesByCategory()
                        .isEmpty()
        );
    }

    @Test
    void shouldAggregateMultipleExpensesInSameCategory() {

        insertExpense(
                "Utilities",
                "Electricity",
                3000,
                LocalDate.of(2026, 1, 5)
        );

        insertExpense(
                "Utilities",
                "Water",
                1500,
                LocalDate.of(2026, 1, 10)
        );

        insertExpense(
                "Utilities",
                "Internet",
                2000,
                LocalDate.of(2026, 1, 15)
        );

        ExpenseReport report =
                expenseReportRepository.getExpenseReport(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31)
                );

        assertEquals(
                6500,
                report.getExpensesByCategory()
                        .get("Utilities")
        );

        assertEquals(
                6500,
                report.getTotalExpenses()
        );

        assertEquals(
                3,
                report.getExpenseCount()
        );
    }

    private void insertExpense(
            String category,
            String description,
            double amount,
            LocalDate expenseDate
    ) {

        String sql = """
                INSERT INTO expenses (
                    category,
                    description,
                    amount,
                    expense_date,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                var statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    category
            );

            statement.setString(
                    2,
                    description
            );

            statement.setDouble(
                    3,
                    amount
            );

            statement.setString(
                    4,
                    expenseDate.toString()
            );

            statement.setString(
                    5,
                    "2026-01-01T10:00:00"
            );

            statement.executeUpdate();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to insert test expense.",
                    e
            );
        }
    }
}