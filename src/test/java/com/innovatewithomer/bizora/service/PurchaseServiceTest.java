package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.*;
import com.innovatewithomer.bizora.repository.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseServiceTest {

    private PurchaseService purchaseService;

    private PurchaseRepository purchaseRepository;
    private PurchaseItemRepository purchaseItemRepository;
    private ProductRepository productRepository;
    private InventoryMovementRepository movementRepository;
    private SupplierRepository supplierRepository;


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
    CREATE TABLE suppliers (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        phone TEXT,
        email TEXT,
        address TEXT,
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
        created_at TEXT NOT NULL,

        FOREIGN KEY (supplier_id)
            REFERENCES suppliers(id)
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

            statement.execute("""
                CREATE TABLE inventory_movements (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_id INTEGER NOT NULL,
                    movement_type TEXT NOT NULL,
                    quantity REAL NOT NULL,
                    reference_type TEXT,
                    reference_id INTEGER,
                    note TEXT,
                    created_at TEXT NOT NULL,

                    FOREIGN KEY (product_id)
                        REFERENCES products(id)
                )
                """);
        }

        purchaseRepository =
                new PurchaseRepository();

        purchaseItemRepository =
                new PurchaseItemRepository();

        productRepository =
                new ProductRepository();

        movementRepository =
                new InventoryMovementRepository();

        supplierRepository =
                new SupplierRepository();

        purchaseService =
                new PurchaseService(
                        purchaseRepository,
                        purchaseItemRepository,
                        productRepository,
                        movementRepository,
                        supplierRepository
                );
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    @Test
    void shouldCreatePurchaseAndIncreaseStock() {

        Product product =
                productRepository.save(
                        new Product(
                                "Laptop",
                                "LAP-001",
                                150000,
                                120000,
                                10
                        )
                );

        Purchase purchase =
                new Purchase("PUR-001");

        purchase.addItem(
                new PurchaseItem(
                        product.getId(),
                        5,
                        120000,
                        0
                )
        );

        purchaseService.createPurchase(purchase);

        assertNotNull(
                purchase.getId()
        );

        Product updated =
                productRepository.findById(
                        product.getId()
                );

        assertNotNull(updated);

        assertEquals(
                15,
                updated.getStockQuantity()
        );
    }

    @Test
    void shouldCreatePurchaseItem() {

        Product product =
                productRepository.save(
                        new Product(
                                "Keyboard",
                                "KEY-001",
                                5000,
                                3500,
                                10
                        )
                );

        Purchase purchase =
                new Purchase("PUR-002");

        purchase.addItem(
                new PurchaseItem(
                        product.getId(),
                        3,
                        3500,
                        0
                )
        );

        purchaseService.createPurchase(purchase);

        Purchase saved =
                purchaseService.getPurchase(
                        purchase.getId()
                );

        assertNotNull(saved);

        assertEquals(
                1,
                saved.getItems().size()
        );

        PurchaseItem item =
                saved.getItems().get(0);

        assertEquals(
                product.getId(),
                item.getProductId()
        );

        assertEquals(
                3,
                item.getQuantity()
        );

        assertEquals(
                10500,
                item.getSubtotal()
        );
    }

    @Test
    void shouldCreateInventoryMovement() {

        Product product =
                productRepository.save(
                        new Product(
                                "Mouse",
                                "MOU-001",
                                2500,
                                1500,
                                20
                        )
                );

        Purchase purchase =
                new Purchase("PUR-003");

        purchase.addItem(
                new PurchaseItem(
                        product.getId(),
                        4,
                        1500,
                        0
                )
        );

        purchaseService.createPurchase(purchase);

        var movements =
                movementRepository.findByProductId(
                        product.getId()
                );

        assertEquals(
                1,
                movements.size()
        );

        InventoryMovement movement =
                movements.get(0);

        assertEquals(
                InventoryMovementType.STOCK_IN,
                movement.getMovementType()
        );

        assertEquals(
                4,
                movement.getQuantity()
        );

        assertEquals(
                "PURCHASE",
                movement.getReferenceType()
        );

        assertEquals(
                purchase.getId(),
                movement.getReferenceId()
        );
    }

    @Test
    void shouldRollbackPurchaseWhenProductDoesNotExist() {

        Purchase purchase =
                new Purchase("PUR-004");

        purchase.addItem(
                new PurchaseItem(
                        999999L,
                        10,
                        1500,
                        0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.createPurchase(
                        purchase
                )
        );

        assertNull(
                purchaseRepository.findById(
                        purchase.getId() == null
                                ? -1L
                                : purchase.getId()
                )
        );

        assertTrue(
                movementRepository
                        .findByProductId(999999L)
                        .isEmpty()
        );
    }

    @Test
    void shouldRejectDuplicateProductsInPurchase() {

        Product product =
                productRepository.save(
                        new Product(
                                "Monitor",
                                "MON-001",
                                30000,
                                25000,
                                10
                        )
                );

        Purchase purchase =
                new Purchase("PUR-DUP-001");

        purchase.addItem(
                new PurchaseItem(
                        product.getId(),
                        2,
                        25000,
                        0
                )
        );

        purchase.addItem(
                new PurchaseItem(
                        product.getId(),
                        3,
                        25000,
                        0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.createPurchase(
                        purchase
                )
        );

        Product unchanged =
                productRepository.findById(
                        product.getId()
                );

        assertEquals(
                10,
                unchanged.getStockQuantity()
        );

        assertTrue(
                movementRepository
                        .findByProductId(
                                product.getId()
                        )
                        .isEmpty()
        );
    }

    @Test
    void shouldRejectInvalidQuantity() {

        Product product =
                productRepository.save(
                        new Product(
                                "Printer",
                                "PRI-001",
                                30000,
                                25000,
                                10
                        )
                );

        Purchase purchase =
                new Purchase("PUR-005");

        purchase.addItem(
                new PurchaseItem(
                        product.getId(),
                        0,
                        25000,
                        0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.createPurchase(
                        purchase
                )
        );

        Product unchanged =
                productRepository.findById(
                        product.getId()
                );

        assertEquals(
                10,
                unchanged.getStockQuantity()
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {

        Product product =
                productRepository.save(
                        new Product(
                                "Scanner",
                                "SCN-001",
                                20000,
                                15000,
                                10
                        )
                );

        Purchase purchase =
                new Purchase("PUR-006");

        purchase.addItem(
                new PurchaseItem(
                        product.getId(),
                        -5,
                        15000,
                        0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.createPurchase(
                        purchase
                )
        );

        Product unchanged =
                productRepository.findById(
                        product.getId()
                );

        assertEquals(
                10,
                unchanged.getStockQuantity()
        );
    }

    @Test
    void shouldRejectNegativeDiscount() {

        Product product =
                productRepository.save(
                        new Product(
                                "Router",
                                "ROU-001",
                                10000,
                                7000,
                                10
                        )
                );

        Purchase purchase =
                new Purchase("PUR-007");

        purchase.setDiscount(-100);

        purchase.addItem(
                new PurchaseItem(
                        product.getId(),
                        2,
                        7000,
                        0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.createPurchase(
                        purchase
                )
        );

        Product unchanged =
                productRepository.findById(
                        product.getId()
                );

        assertEquals(
                10,
                unchanged.getStockQuantity()
        );
    }

    @Test
    void shouldRejectItemDiscountGreaterThanItemValue() {

        Product product =
                productRepository.save(
                        new Product(
                                "Hard Drive",
                                "HDD-001",
                                10000,
                                7000,
                                10
                        )
                );

        Purchase purchase =
                new Purchase("PUR-008");

        purchase.addItem(
                new PurchaseItem(
                        product.getId(),
                        2,
                        7000,
                        15000
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.createPurchase(
                        purchase
                )
        );

        Product unchanged =
                productRepository.findById(
                        product.getId()
                );

        assertEquals(
                10,
                unchanged.getStockQuantity()
        );
    }

    @Test
    void shouldCreatePurchaseWithMultipleItems() {

        Product laptop =
                productRepository.save(
                        new Product(
                                "Laptop",
                                "LAP-010",
                                150000,
                                120000,
                                10
                        )
                );

        Product mouse =
                productRepository.save(
                        new Product(
                                "Mouse",
                                "MOU-010",
                                2500,
                                1500,
                                20
                        )
                );

        Purchase purchase =
                new Purchase("PUR-009");

        purchase.addItem(
                new PurchaseItem(
                        laptop.getId(),
                        2,
                        120000,
                        0
                )
        );

        purchase.addItem(
                new PurchaseItem(
                        mouse.getId(),
                        5,
                        1500,
                        0
                )
        );

        purchaseService.createPurchase(purchase);

        Product updatedLaptop =
                productRepository.findById(
                        laptop.getId()
                );

        Product updatedMouse =
                productRepository.findById(
                        mouse.getId()
                );

        assertEquals(
                12,
                updatedLaptop.getStockQuantity()
        );

        assertEquals(
                25,
                updatedMouse.getStockQuantity()
        );

        Purchase saved =
                purchaseService.getPurchase(
                        purchase.getId()
                );

        assertEquals(
                2,
                saved.getItems().size()
        );

        assertEquals(
                247500,
                saved.getTotal()
        );
    }

    @Test
    void shouldCreatePurchaseWithSupplier() {

        Supplier supplier =
                supplierRepository.save(
                        new Supplier(
                                "ABC Suppliers",
                                "03001234567",
                                "abc@example.com",
                                "Islamabad"
                        )
                );

        Product product =
                productRepository.save(
                        new Product(
                                "Keyboard",
                                "KEY-SUP-001",
                                5000,
                                3500,
                                10
                        )
                );

        Purchase purchase =
                new Purchase("PUR-SUP-001");

        purchase.setSupplierId(
                supplier.getId()
        );

        purchase.addItem(
                new PurchaseItem(
                        product.getId(),
                        5,
                        3500,
                        0
                )
        );

        purchaseService.createPurchase(
                purchase
        );

        Purchase saved =
                purchaseService.getPurchase(
                        purchase.getId()
                );

        assertNotNull(saved);

        assertEquals(
                supplier.getId(),
                saved.getSupplierId()
        );
    }


    @Test
    void shouldRejectUnknownSupplier() {

        Product product =
                productRepository.save(
                        new Product(
                                "Keyboard",
                                "KEY-SUP-002",
                                5000,
                                3500,
                                10
                        )
                );

        Purchase purchase =
                new Purchase("PUR-BAD-SUP-001");

        purchase.setSupplierId(999999L);

        purchase.addItem(
                new PurchaseItem(
                        product.getId(),
                        5,
                        3500,
                        0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> purchaseService.createPurchase(
                        purchase
                )
        );

        Product unchanged =
                productRepository.findById(
                        product.getId()
                );

        assertNotNull(unchanged);

        assertEquals(
                10,
                unchanged.getStockQuantity()
        );
    }
}