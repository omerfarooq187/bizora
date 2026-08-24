package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.PaymentStatus;
import com.innovatewithomer.bizora.model.Sale;
import com.innovatewithomer.bizora.model.SaleStatus;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaleRepositoryTest {

    private SaleRepository repository;
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
    CREATE TABLE customers (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        phone TEXT,
        email TEXT,
        address TEXT,
        created_at TEXT NOT NULL
    )
    """);

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
        created_at TEXT NOT NULL,

        FOREIGN KEY (customer_id)
            REFERENCES customers(id)
    )
    """);
        }

        repository =
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
    void shouldSaveSale() {

        Sale sale =
                new Sale("INV-001");

        sale.setSubtotal(10000);
        sale.setDiscount(500);
        sale.setTax(0);
        sale.setTotal(9500);
        sale.setPaymentStatus(
                PaymentStatus.PAID
        );
        sale.setSaleStatus(
                SaleStatus.COMPLETED
        );

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            Sale saved =
                    repository.save(
                            connection,
                            sale
                    );

            assertNotNull(saved.getId());
            assertEquals(
                    "INV-001",
                    saved.getInvoiceNumber()
            );
            assertEquals(
                    9500,
                    saved.getTotal()
            );
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void shouldFindSaleById() {

        Sale sale =
                new Sale("INV-002");

        sale.setSubtotal(5000);
        sale.setTotal(5000);

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            Sale saved =
                    repository.save(
                            connection,
                            sale
                    );

            Sale found =
                    repository.findById(
                            connection,
                            saved.getId()
                    );

            assertNotNull(found);

            assertEquals(
                    saved.getId(),
                    found.getId()
            );

            assertEquals(
                    "INV-002",
                    found.getInvoiceNumber()
            );

            assertEquals(
                    5000,
                    found.getTotal()
            );

        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void shouldReturnNullForNonExistingSale() {

        Sale sale =
                repository.findById(999999L);

        assertNull(sale);
    }

    @Test
    void shouldFindAllSales() {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            repository.save(
                    connection,
                    new Sale("INV-003")
            );

            repository.save(
                    connection,
                    new Sale("INV-004")
            );
        } catch (Exception e) {
            fail(e);
        }

        List<Sale> sales =
                repository.findAll();

        assertEquals(
                2,
                sales.size()
        );
    }

    @Test
    void shouldMapPaymentAndSaleStatus() {

        Sale sale =
                new Sale("INV-005");

        sale.setPaymentStatus(
                PaymentStatus.PARTIALLY_PAID
        );

        sale.setSaleStatus(
                SaleStatus.CANCELLED
        );

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            repository.save(
                    connection,
                    sale
            );

            Sale found =
                    repository.findById(
                            connection,
                            sale.getId()
                    );

            assertNotNull(found);

            assertEquals(
                    PaymentStatus.PARTIALLY_PAID,
                    found.getPaymentStatus()
            );

            assertEquals(
                    SaleStatus.CANCELLED,
                    found.getSaleStatus()
            );

        } catch (Exception e) {
            fail(e);
        }
    }
}