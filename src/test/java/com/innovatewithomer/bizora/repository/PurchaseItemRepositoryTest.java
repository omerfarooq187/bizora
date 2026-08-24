package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.PurchaseItem;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseItemRepositoryTest {

    private PurchaseItemRepository repository;

    private Connection keepAliveConnection;

    @BeforeEach
    void setUp() throws Exception {

        DatabaseManager.setJdbcUrl(
                "jdbc:sqlite:file:bizora_purchase_item_test_"
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
                CREATE TABLE products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    sku TEXT UNIQUE,
                    selling_price REAL NOT NULL DEFAULT 0,
                    purchase_price REAL NOT NULL DEFAULT 0,
                    stock_quantity REAL NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL
                )
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

            statement.execute("""
                CREATE TABLE purchase_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    purchase_id INTEGER NOT NULL,
                    product_id INTEGER NOT NULL,
                    quantity REAL NOT NULL,
                    unit_price REAL NOT NULL,
                    discount REAL NOT NULL DEFAULT 0,
                    subtotal REAL NOT NULL DEFAULT 0,

                    FOREIGN KEY (purchase_id)
                        REFERENCES purchases(id),

                    FOREIGN KEY (product_id)
                        REFERENCES products(id)
                )
                """);
        }

        repository =
                new PurchaseItemRepository();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    @Test
    void shouldSavePurchaseItem() throws Exception {

        long purchaseId =
                createPurchase("PUR-001");

        long productId =
                createProduct(
                        "Keyboard",
                        "KEY-001"
                );

        PurchaseItem item =
                new PurchaseItem(
                        productId,
                        5,
                        3500,
                        100
                );

        item.setPurchaseId(
                purchaseId
        );

        repository.save(
                keepAliveConnection,
                item
        );

        assertNotNull(
                item.getId()
        );

        assertTrue(
                item.getId() > 0
        );
    }

    @Test
    void shouldFindPurchaseItemsByPurchaseId()
            throws Exception {

        long purchaseId =
                createPurchase("PUR-002");

        long productId =
                createProduct(
                        "Mouse",
                        "MOU-001"
                );

        PurchaseItem item =
                new PurchaseItem(
                        productId,
                        4,
                        1500,
                        100
                );

        item.setPurchaseId(
                purchaseId
        );

        repository.save(
                keepAliveConnection,
                item
        );

        List<PurchaseItem> items =
                repository.findByPurchaseId(
                        keepAliveConnection,
                        purchaseId
                );

        assertEquals(
                1,
                items.size()
        );

        PurchaseItem saved =
                items.get(0);

        assertEquals(
                item.getId(),
                saved.getId()
        );

        assertEquals(
                purchaseId,
                saved.getPurchaseId()
        );

        assertEquals(
                productId,
                saved.getProductId()
        );

        assertEquals(
                4,
                saved.getQuantity()
        );

        assertEquals(
                1500,
                saved.getUnitPrice()
        );

        assertEquals(
                100,
                saved.getDiscount()
        );

        assertEquals(
                5900,
                saved.getSubtotal()
        );
    }

    @Test
    void shouldReturnEmptyListWhenPurchaseHasNoItems()
            throws Exception {

        long purchaseId =
                createPurchase("PUR-003");

        List<PurchaseItem> items =
                repository.findByPurchaseId(
                        keepAliveConnection,
                        purchaseId
                );

        assertNotNull(items);

        assertTrue(
                items.isEmpty()
        );
    }

    @Test
    void shouldFindMultipleItemsForSamePurchase()
            throws Exception {

        long purchaseId =
                createPurchase("PUR-004");

        long keyboardId =
                createProduct(
                        "Keyboard",
                        "KEY-004"
                );

        long mouseId =
                createProduct(
                        "Mouse",
                        "MOU-004"
                );

        PurchaseItem keyboard =
                new PurchaseItem(
                        keyboardId,
                        2,
                        3500,
                        0
                );

        keyboard.setPurchaseId(
                purchaseId
        );

        PurchaseItem mouse =
                new PurchaseItem(
                        mouseId,
                        3,
                        1500,
                        0
                );

        mouse.setPurchaseId(
                purchaseId
        );

        repository.save(
                keepAliveConnection,
                keyboard
        );

        repository.save(
                keepAliveConnection,
                mouse
        );

        List<PurchaseItem> items =
                repository.findByPurchaseId(
                        keepAliveConnection,
                        purchaseId
                );

        assertEquals(
                2,
                items.size()
        );

        assertEquals(
                keyboardId,
                items.get(0).getProductId()
        );

        assertEquals(
                mouseId,
                items.get(1).getProductId()
        );
    }

    @Test
    void shouldNotReturnItemsFromAnotherPurchase()
            throws Exception {

        long firstPurchaseId =
                createPurchase("PUR-005");

        long secondPurchaseId =
                createPurchase("PUR-006");

        long productId =
                createProduct(
                        "Monitor",
                        "MON-005"
                );

        PurchaseItem firstItem =
                new PurchaseItem(
                        productId,
                        2,
                        25000,
                        0
                );

        firstItem.setPurchaseId(
                firstPurchaseId
        );

        PurchaseItem secondItem =
                new PurchaseItem(
                        productId,
                        5,
                        25000,
                        0
                );

        secondItem.setPurchaseId(
                secondPurchaseId
        );

        repository.save(
                keepAliveConnection,
                firstItem
        );

        repository.save(
                keepAliveConnection,
                secondItem
        );

        List<PurchaseItem> firstItems =
                repository.findByPurchaseId(
                        keepAliveConnection,
                        firstPurchaseId
                );

        List<PurchaseItem> secondItems =
                repository.findByPurchaseId(
                        keepAliveConnection,
                        secondPurchaseId
                );

        assertEquals(
                1,
                firstItems.size()
        );

        assertEquals(
                2,
                firstItems.get(0).getQuantity()
        );

        assertEquals(
                1,
                secondItems.size()
        );

        assertEquals(
                5,
                secondItems.get(0).getQuantity()
        );
    }

    @Test
    void shouldReturnItemsInIdOrder()
            throws Exception {

        long purchaseId =
                createPurchase("PUR-007");

        long product1 =
                createProduct(
                        "Product A",
                        "PROD-A"
                );

        long product2 =
                createProduct(
                        "Product B",
                        "PROD-B"
                );

        PurchaseItem first =
                new PurchaseItem(
                        product1,
                        1,
                        100,
                        0
                );

        first.setPurchaseId(
                purchaseId
        );

        PurchaseItem second =
                new PurchaseItem(
                        product2,
                        2,
                        200,
                        0
                );

        second.setPurchaseId(
                purchaseId
        );

        repository.save(
                keepAliveConnection,
                first
        );

        repository.save(
                keepAliveConnection,
                second
        );

        List<PurchaseItem> items =
                repository.findByPurchaseId(
                        keepAliveConnection,
                        purchaseId
                );

        assertEquals(
                2,
                items.size()
        );

        assertEquals(
                first.getId(),
                items.get(0).getId()
        );

        assertEquals(
                second.getId(),
                items.get(1).getId()
        );
    }

    @Test
    void shouldWorkThroughPublicFindByPurchaseId()
            throws Exception {

        long purchaseId =
                createPurchase("PUR-008");

        long productId =
                createProduct(
                        "Printer",
                        "PRI-008"
                );

        PurchaseItem item =
                new PurchaseItem(
                        productId,
                        3,
                        15000,
                        500
                );

        item.setPurchaseId(
                purchaseId
        );

        repository.save(
                keepAliveConnection,
                item
        );

        List<PurchaseItem> items =
                repository.findByPurchaseId(
                        purchaseId
                );

        assertEquals(
                1,
                items.size()
        );

        assertEquals(
                productId,
                items.get(0).getProductId()
        );
    }

    private long createPurchase(
            String invoiceNumber
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
            VALUES (?, 0, 0, 0, 0, 'UNPAID', 'COMPLETED', datetime('now'))
            """;

        try (var statement =
                     keepAliveConnection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(
                    1,
                    invoiceNumber
            );

            statement.executeUpdate();

            try (var keys =
                         statement.getGeneratedKeys()) {

                assertTrue(keys.next());

                return keys.getLong(1);
            }
        }
    }

    private long createProduct(
            String name,
            String sku
    ) throws Exception {

        String sql = """
            INSERT INTO products (
                name,
                sku,
                selling_price,
                purchase_price,
                stock_quantity,
                created_at
            )
            VALUES (?, ?, 1000, 800, 0, datetime('now'))
            """;

        try (var statement =
                     keepAliveConnection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(1, name);
            statement.setString(2, sku);

            statement.executeUpdate();

            try (var keys =
                         statement.getGeneratedKeys()) {

                assertTrue(keys.next());

                return keys.getLong(1);
            }
        }
    }
}
