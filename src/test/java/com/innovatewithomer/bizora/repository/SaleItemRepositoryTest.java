package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.SaleItem;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaleItemRepositoryTest {

    private SaleItemRepository repository;
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

            statement.execute("""
                CREATE TABLE sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
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

            statement.execute("""
                CREATE TABLE sale_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER NOT NULL,
                    product_id INTEGER NOT NULL,
                    quantity REAL NOT NULL,
                    unit_price REAL NOT NULL,
                    cost_price REAL NOT NULL DEFAULT 0,
                    discount REAL NOT NULL DEFAULT 0,
                    subtotal REAL NOT NULL DEFAULT 0,

                    FOREIGN KEY (sale_id)
                        REFERENCES sales(id)
                )
                """);
        }

        repository =
                new SaleItemRepository();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    @Test
    void shouldSaveSaleItem() throws Exception {

        Long saleId =
                createSale();

        SaleItem item =
                new SaleItem(
                        10L,
                        2,
                        5000,
                        500
                );

        item.setSaleId(saleId);

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            repository.save(
                    connection,
                    item
            );
        }

        assertNotNull(
                item.getId()
        );

        assertEquals(
                10L,
                item.getProductId()
        );

        assertEquals(
                2,
                item.getQuantity()
        );

        assertEquals(
                9500,
                item.getSubtotal()
        );
    }

    @Test
    void shouldFindItemsBySaleId() throws Exception {

        Long saleId =
                createSale();

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            SaleItem first =
                    new SaleItem(
                            10L,
                            2,
                            5000,
                            0
                    );

            first.setSaleId(saleId);

            SaleItem second =
                    new SaleItem(
                            20L,
                            1,
                            3000,
                            0
                    );

            second.setSaleId(saleId);

            repository.save(
                    connection,
                    first
            );

            repository.save(
                    connection,
                    second
            );
        }

        List<SaleItem> items =
                repository.findBySaleId(
                        saleId
                );

        assertEquals(
                2,
                items.size()
        );
    }

    @Test
    void shouldReturnEmptyListForSaleWithoutItems()
            throws Exception {

        Long saleId =
                createSale();

        List<SaleItem> items =
                repository.findBySaleId(
                        saleId
                );

        assertTrue(
                items.isEmpty()
        );
    }

    private Long createSale()
            throws Exception {

        try (Connection connection =
                     DatabaseManager.getConnection();

             Statement statement =
                     connection.createStatement()) {

            statement.executeUpdate("""
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
                VALUES (
                    'TEST-INV',
                    10000,
                    0,
                    0,
                    10000,
                    'PAID',
                    'COMPLETED',
                    '2026-08-21T00:00:00'
                )
                """,
                    Statement.RETURN_GENERATED_KEYS
            );

            try (var keys =
                         statement.getGeneratedKeys()) {

                assertTrue(keys.next());

                return keys.getLong(1);
            }
        }
    }
}
