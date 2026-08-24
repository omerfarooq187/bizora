package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.report.PurchaseReport;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseReportRepositoryTest {

    private PurchaseReportRepository repository;

    private Connection keepAliveConnection;

    @BeforeEach
    void setUp() throws Exception {

        DatabaseManager.setJdbcUrl(
                "jdbc:sqlite:file:bizora_purchase_report_test_"
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
        }

        repository =
                new PurchaseReportRepository();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    private void insertPurchase(
            String invoiceNumber,
            double total,
            String status,
            String createdAt
    ) throws Exception {

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

        try (var statement =
                     keepAliveConnection.prepareStatement(sql)) {

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
                    status
            );

            statement.setString(
                    8,
                    createdAt
            );

            statement.executeUpdate();
        }
    }

    @Test
    void shouldGeneratePurchaseReport() throws Exception {

        insertPurchase(
                "PUR-001",
                10000,
                "COMPLETED",
                "2026-08-10T10:00:00"
        );

        insertPurchase(
                "PUR-002",
                20000,
                "COMPLETED",
                "2026-08-10T12:00:00"
        );

        PurchaseReport report =
                repository.getPurchaseReport(
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 8, 10)
                );

        assertEquals(
                2,
                report.getTotalPurchases()
        );

        assertEquals(
                30000,
                report.getTotalCost()
        );

        assertEquals(
                15000,
                report.getAveragePurchaseValue()
        );
    }

    @Test
    void shouldExcludeCancelledPurchases()
            throws Exception {

        insertPurchase(
                "PUR-003",
                10000,
                "COMPLETED",
                "2026-08-10T10:00:00"
        );

        insertPurchase(
                "PUR-004",
                50000,
                "CANCELLED",
                "2026-08-10T12:00:00"
        );

        PurchaseReport report =
                repository.getPurchaseReport(
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 8, 10)
                );

        assertEquals(
                1,
                report.getTotalPurchases()
        );

        assertEquals(
                10000,
                report.getTotalCost()
        );

        assertEquals(
                10000,
                report.getAveragePurchaseValue()
        );
    }

    @Test
    void shouldFilterPurchasesByDateRange()
            throws Exception {

        insertPurchase(
                "PUR-005",
                10000,
                "COMPLETED",
                "2026-08-09T23:59:59"
        );

        insertPurchase(
                "PUR-006",
                20000,
                "COMPLETED",
                "2026-08-10T10:00:00"
        );

        insertPurchase(
                "PUR-007",
                30000,
                "COMPLETED",
                "2026-08-11T15:00:00"
        );

        PurchaseReport report =
                repository.getPurchaseReport(
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 8, 10)
                );

        assertEquals(
                1,
                report.getTotalPurchases()
        );

        assertEquals(
                20000,
                report.getTotalCost()
        );

        assertEquals(
                20000,
                report.getAveragePurchaseValue()
        );
    }

    @Test
    void shouldIncludeEntireEndDate()
            throws Exception {

        insertPurchase(
                "PUR-008",
                25000,
                "COMPLETED",
                "2026-08-10T23:59:59"
        );

        PurchaseReport report =
                repository.getPurchaseReport(
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 8, 10)
                );

        assertEquals(
                1,
                report.getTotalPurchases()
        );

        assertEquals(
                25000,
                report.getTotalCost()
        );
    }

    @Test
    void shouldReturnZeroWhenNoPurchasesExist() {

        PurchaseReport report =
                repository.getPurchaseReport(
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 8, 10)
                );

        assertEquals(
                0,
                report.getTotalPurchases()
        );

        assertEquals(
                0,
                report.getTotalCost()
        );

        assertEquals(
                0,
                report.getAveragePurchaseValue()
        );
    }

    @Test
    void shouldRejectNullFromDate() {

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.getPurchaseReport(
                        null,
                        LocalDate.of(2026, 8, 10)
                )
        );
    }

    @Test
    void shouldRejectNullToDate() {

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.getPurchaseReport(
                        LocalDate.of(2026, 8, 10),
                        null
                )
        );
    }

    @Test
    void shouldRejectInvalidDateRange() {

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.getPurchaseReport(
                        LocalDate.of(2026, 8, 11),
                        LocalDate.of(2026, 8, 10)
                )
        );
    }
}