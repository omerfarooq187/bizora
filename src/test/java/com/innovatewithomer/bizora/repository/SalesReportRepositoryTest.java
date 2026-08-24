package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.PaymentStatus;
import com.innovatewithomer.bizora.model.Sale;
import com.innovatewithomer.bizora.model.SaleStatus;
import com.innovatewithomer.bizora.model.SalesReport;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SalesReportRepositoryTest {

    private SalesReportRepository repository;

    private SaleRepository saleRepository;

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
        }

        repository =
                new SalesReportRepository();

        saleRepository =
                new SaleRepository();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    @Test
    void shouldCalculateSalesReport() {

        saveSale(
                "INV-001",
                10000,
                SaleStatus.COMPLETED,
                LocalDate.of(
                        2026,
                        8,
                        20
                )
        );

        saveSale(
                "INV-002",
                5000,
                SaleStatus.COMPLETED,
                LocalDate.of(
                        2026,
                        8,
                        20
                )
        );

        SalesReport report =
                repository.getSalesReport(
                        LocalDate.of(
                                2026,
                                8,
                                20
                        ),
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        assertEquals(
                2,
                report.getTotalSales()
        );

        assertEquals(
                15000,
                report.getTotalRevenue()
        );

        assertEquals(
                7500,
                report.getAverageSaleValue()
        );
    }

    @Test
    void shouldIgnoreCancelledSales() {

        saveSale(
                "INV-003",
                10000,
                SaleStatus.COMPLETED,
                LocalDate.of(
                        2026,
                        8,
                        20
                )
        );

        saveSale(
                "INV-004",
                5000,
                SaleStatus.CANCELLED,
                LocalDate.of(
                        2026,
                        8,
                        20
                )
        );

        SalesReport report =
                repository.getSalesReport(
                        LocalDate.of(
                                2026,
                                8,
                                20
                        ),
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        assertEquals(
                1,
                report.getTotalSales()
        );

        assertEquals(
                10000,
                report.getTotalRevenue()
        );

        assertEquals(
                10000,
                report.getAverageSaleValue()
        );
    }

    @Test
    void shouldFilterByDateRange() {

        saveSale(
                "INV-005",
                10000,
                SaleStatus.COMPLETED,
                LocalDate.of(
                        2026,
                        8,
                        19
                )
        );

        saveSale(
                "INV-006",
                20000,
                SaleStatus.COMPLETED,
                LocalDate.of(
                        2026,
                        8,
                        20
                )
        );

        saveSale(
                "INV-007",
                30000,
                SaleStatus.COMPLETED,
                LocalDate.of(
                        2026,
                        8,
                        21
                )
        );

        SalesReport report =
                repository.getSalesReport(
                        LocalDate.of(
                                2026,
                                8,
                                20
                        ),
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        assertEquals(
                1,
                report.getTotalSales()
        );

        assertEquals(
                20000,
                report.getTotalRevenue()
        );
    }

    @Test
    void shouldIncludeEntireEndDate() {

        saveSale(
                "INV-008",
                15000,
                SaleStatus.COMPLETED,
                LocalDate.of(
                        2026,
                        8,
                        20
                )
        );

        SalesReport report =
                repository.getSalesReport(
                        LocalDate.of(
                                2026,
                                8,
                                20
                        ),
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        assertEquals(
                1,
                report.getTotalSales()
        );

        assertEquals(
                15000,
                report.getTotalRevenue()
        );
    }

    @Test
    void shouldReturnZeroWhenNoSalesExist() {

        SalesReport report =
                repository.getSalesReport(
                        LocalDate.of(
                                2026,
                                8,
                                20
                        ),
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        assertEquals(
                0,
                report.getTotalSales()
        );

        assertEquals(
                0,
                report.getTotalRevenue()
        );

        assertEquals(
                0,
                report.getAverageSaleValue()
        );
    }

    private void saveSale(
            String invoiceNumber,
            double total,
            SaleStatus status,
            LocalDate date
    ) {

        Sale sale =
                new Sale(
                        invoiceNumber
                );

        sale.setSubtotal(total);
        sale.setDiscount(0);
        sale.setTax(0);
        sale.setTotal(total);

        sale.setPaymentStatus(
                PaymentStatus.PAID
        );

        sale.setSaleStatus(
                status
        );

        sale.setCreatedAt(
                date.atTime(12, 0)
        );

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            saleRepository.save(
                    connection,
                    sale
            );

        } catch (Exception e) {

            fail(e);
        }
    }
}