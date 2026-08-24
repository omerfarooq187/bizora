package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.report.FinancialSummary;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FinancialSummaryRepositoryTest {

    private FinancialSummaryRepository financialSummaryRepository;

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
            // SALES
            // -----------------------------------------

            statement.execute("""
                CREATE TABLE sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER,
                    invoice_number TEXT NOT NULL UNIQUE,
                    subtotal REAL NOT NULL DEFAULT 0,
                    discount REAL NOT NULL DEFAULT 0,
                    tax REAL NOT NULL DEFAULT 0,
                    total REAL NOT NULL DEFAULT 0,
                    payment_status TEXT NOT NULL,
                    sale_status TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
                """);

            // -----------------------------------------
            // PURCHASES
            // -----------------------------------------

            statement.execute("""
                CREATE TABLE purchases (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    supplier_id INTEGER,
                    invoice_number TEXT NOT NULL UNIQUE,
                    subtotal REAL NOT NULL DEFAULT 0,
                    discount REAL NOT NULL DEFAULT 0,
                    tax REAL NOT NULL DEFAULT 0,
                    total REAL NOT NULL DEFAULT 0,
                    payment_status TEXT NOT NULL,
                    purchase_status TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
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

        financialSummaryRepository =
                new FinancialSummaryRepository();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    @Test
    void shouldCalculateFinancialSummary() {

        insertSale(
                "INV-001",
                100000,
                "2026-08-10T10:00:00"
        );

        insertPurchase(
                "PUR-001",
                60000,
                "2026-08-11T10:00:00"
        );

        insertExpense(
                "Rent",
                "Shop rent",
                10000,
                LocalDate.of(2026, 8, 12)
        );

        FinancialSummary summary =
                financialSummaryRepository.getFinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        assertEquals(
                100000,
                summary.getTotalSales()
        );

        assertEquals(
                60000,
                summary.getTotalPurchases()
        );

        assertEquals(
                10000,
                summary.getTotalExpenses()
        );

        assertEquals(
                40000,
                summary.getGrossProfit()
        );

        assertEquals(
                30000,
                summary.getNetProfit()
        );
    }

    @Test
    void shouldCalculateGrossProfit() {

        insertSale(
                "INV-002",
                150000,
                "2026-08-10T10:00:00"
        );

        insertPurchase(
                "PUR-002",
                90000,
                "2026-08-11T10:00:00"
        );

        FinancialSummary summary =
                financialSummaryRepository.getFinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        assertEquals(
                60000,
                summary.getGrossProfit()
        );
    }

    @Test
    void shouldCalculateNetProfitAfterExpenses() {

        insertSale(
                "INV-003",
                150000,
                "2026-08-10T10:00:00"
        );

        insertPurchase(
                "PUR-003",
                90000,
                "2026-08-11T10:00:00"
        );

        insertExpense(
                "Utilities",
                "Electricity",
                15000,
                LocalDate.of(2026, 8, 15)
        );

        insertExpense(
                "Transport",
                "Fuel",
                5000,
                LocalDate.of(2026, 8, 20)
        );

        FinancialSummary summary =
                financialSummaryRepository.getFinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        assertEquals(
                60000,
                summary.getGrossProfit()
        );

        assertEquals(
                40000,
                summary.getNetProfit()
        );
    }

    @Test
    void shouldIgnoreRecordsOutsideDateRange() {

        insertSale(
                "INV-OUT-001",
                50000,
                "2026-07-31T10:00:00"
        );

        insertSale(
                "INV-IN-001",
                100000,
                "2026-08-10T10:00:00"
        );

        insertSale(
                "INV-OUT-002",
                70000,
                "2026-09-01T10:00:00"
        );

        insertPurchase(
                "PUR-OUT-001",
                20000,
                "2026-07-31T10:00:00"
        );

        insertPurchase(
                "PUR-IN-001",
                60000,
                "2026-08-15T10:00:00"
        );

        insertPurchase(
                "PUR-OUT-002",
                30000,
                "2026-09-01T10:00:00"
        );

        insertExpense(
                "Rent",
                "July rent",
                5000,
                LocalDate.of(2026, 7, 31)
        );

        insertExpense(
                "Utilities",
                "August electricity",
                10000,
                LocalDate.of(2026, 8, 20)
        );

        insertExpense(
                "Transport",
                "September fuel",
                3000,
                LocalDate.of(2026, 9, 1)
        );

        FinancialSummary summary =
                financialSummaryRepository.getFinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        assertEquals(
                100000,
                summary.getTotalSales()
        );

        assertEquals(
                60000,
                summary.getTotalPurchases()
        );

        assertEquals(
                10000,
                summary.getTotalExpenses()
        );

        assertEquals(
                40000,
                summary.getGrossProfit()
        );

        assertEquals(
                30000,
                summary.getNetProfit()
        );
    }

    @Test
    void shouldIncludeRecordsOnBoundaryDates() {

        insertSale(
                "INV-START",
                50000,
                "2026-08-01T00:00:00"
        );

        insertSale(
                "INV-END",
                30000,
                "2026-08-31T23:59:59"
        );

        insertPurchase(
                "PUR-START",
                20000,
                "2026-08-01T08:00:00"
        );

        insertPurchase(
                "PUR-END",
                10000,
                "2026-08-31T18:00:00"
        );

        insertExpense(
                "Rent",
                "Start date expense",
                5000,
                LocalDate.of(2026, 8, 1)
        );

        insertExpense(
                "Utilities",
                "End date expense",
                3000,
                LocalDate.of(2026, 8, 31)
        );

        FinancialSummary summary =
                financialSummaryRepository.getFinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        assertEquals(
                80000,
                summary.getTotalSales()
        );

        assertEquals(
                30000,
                summary.getTotalPurchases()
        );

        assertEquals(
                8000,
                summary.getTotalExpenses()
        );

        assertEquals(
                50000,
                summary.getGrossProfit()
        );

        assertEquals(
                42000,
                summary.getNetProfit()
        );
    }

    @Test
    void shouldReturnZeroValuesWhenNoDataExists() {

        FinancialSummary summary =
                financialSummaryRepository.getFinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        assertNotNull(summary);

        assertEquals(
                0,
                summary.getTotalSales()
        );

        assertEquals(
                0,
                summary.getTotalPurchases()
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

    @Test
    void shouldHandleLoss() {

        insertSale(
                "INV-LOSS",
                50000,
                "2026-08-10T10:00:00"
        );

        insertPurchase(
                "PUR-LOSS",
                70000,
                "2026-08-11T10:00:00"
        );

        insertExpense(
                "Rent",
                "Shop rent",
                10000,
                LocalDate.of(2026, 8, 15)
        );

        FinancialSummary summary =
                financialSummaryRepository.getFinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        assertEquals(
                -20000,
                summary.getGrossProfit()
        );

        assertEquals(
                -30000,
                summary.getNetProfit()
        );
    }

    @Test
    void shouldStoreRequestedDateRange() {

        LocalDate from =
                LocalDate.of(2026, 8, 1);

        LocalDate to =
                LocalDate.of(2026, 8, 31);

        FinancialSummary summary =
                financialSummaryRepository.getFinancialSummary(
                        from,
                        to
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

    private void insertSale(
            String invoiceNumber,
            double total,
            String createdAt
    ) {

        String sql = """
                INSERT INTO sales (
                    invoice_number,
                    subtotal,
                    discount,
                    tax,
                    total,
                    payment_status,
                    sale_status,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                var statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    invoiceNumber
            );

            statement.setDouble(
                    2,
                    total
            );

            statement.setDouble(
                    3,
                    0
            );

            statement.setDouble(
                    4,
                    0
            );

            statement.setDouble(
                    5,
                    total
            );

            statement.setString(
                    6,
                    "UNPAID"
            );

            statement.setString(
                    7,
                    "COMPLETED"
            );

            statement.setString(
                    8,
                    createdAt
            );

            statement.executeUpdate();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to insert test sale.",
                    e
            );
        }
    }

    private void insertPurchase(
            String invoiceNumber,
            double total,
            String createdAt
    ) {

        String sql = """
                INSERT INTO purchases (
                    invoice_number,
                    subtotal,
                    discount,
                    tax,
                    total,
                    payment_status,
                    purchase_status,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseManager.getConnection();

                var statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    invoiceNumber
            );

            statement.setDouble(
                    2,
                    total
            );

            statement.setDouble(
                    3,
                    0
            );

            statement.setDouble(
                    4,
                    0
            );

            statement.setDouble(
                    5,
                    total
            );

            statement.setString(
                    6,
                    "UNPAID"
            );

            statement.setString(
                    7,
                    "RECEIVED"
            );

            statement.setString(
                    8,
                    createdAt
            );

            statement.executeUpdate();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to insert test purchase.",
                    e
            );
        }
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
                    "2026-08-01T10:00:00"
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