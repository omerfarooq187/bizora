package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.*;
import com.innovatewithomer.bizora.repository.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class SaleServiceTest {

    private SaleService saleService;

    private SaleRepository saleRepository;
    private SaleItemRepository saleItemRepository;
    private ProductRepository productRepository;
    private InventoryMovementRepository movementRepository;
    private PaymentRepositoryPort paymentRepository;

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
            // CUSTOMERS
            // -----------------------------------------

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

            // -----------------------------------------
            // PRODUCTS
            // -----------------------------------------

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
                    created_at TEXT NOT NULL,

                    FOREIGN KEY (customer_id)
                        REFERENCES customers(id)
                )
                """);

            // -----------------------------------------
            // SALE ITEMS
            // -----------------------------------------

            statement.execute("""
                CREATE TABLE sale_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER NOT NULL,
                    product_id INTEGER NOT NULL,
                    quantity REAL NOT NULL,
                    unit_price REAL NOT NULL,
                    discount REAL NOT NULL DEFAULT 0,
                    subtotal REAL NOT NULL DEFAULT 0,

                    FOREIGN KEY (sale_id)
                        REFERENCES sales(id),

                    FOREIGN KEY (product_id)
                        REFERENCES products(id)
                )
                """);

            // -----------------------------------------
            // INVENTORY MOVEMENTS
            // -----------------------------------------

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

            // -----------------------------------------
            // PAYMENTS
            // V7
            // -----------------------------------------

            statement.execute("""
                CREATE TABLE payments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,

                    sale_id INTEGER NOT NULL,

                    amount REAL NOT NULL,

                    payment_method TEXT NOT NULL,

                    reference TEXT,

                    created_at TEXT NOT NULL,

                    FOREIGN KEY (sale_id)
                        REFERENCES sales(id)
                )
                """);
        }

        saleRepository =
                new SaleRepository();

        saleItemRepository =
                new SaleItemRepository();

        productRepository =
                new ProductRepository();

        movementRepository =
                new InventoryMovementRepository();

        paymentRepository =
                new PaymentRepository();

        saleService =
                new SaleService(
                        saleRepository,
                        saleItemRepository,
                        productRepository,
                        movementRepository,
                        paymentRepository
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
    void shouldCreateSaleAndReduceStock() {

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

        Sale sale =
                new Sale("INV-001");

        sale.addItem(
                new SaleItem(
                        product.getId(),
                        2,
                        150000,
                        0
                )
        );

        saleService.createSale(sale);

        assertNotNull(
                sale.getId()
        );

        Product updated =
                productRepository.findById(
                        product.getId()
                );

        assertNotNull(updated);

        assertEquals(
                8,
                updated.getStockQuantity()
        );
    }

    @Test
    void shouldCreateSaleItem() {

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

        Sale sale =
                new Sale("INV-002");

        sale.addItem(
                new SaleItem(
                        product.getId(),
                        3,
                        5000,
                        0
                )
        );

        saleService.createSale(sale);

        Sale saved =
                saleService.getSale(
                        sale.getId()
                );

        assertEquals(
                1,
                saved.getItems().size()
        );

        SaleItem item =
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
                15000,
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

        Sale sale =
                new Sale("INV-003");

        sale.addItem(
                new SaleItem(
                        product.getId(),
                        4,
                        2500,
                        0
                )
        );

        saleService.createSale(sale);

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
                InventoryMovementType.STOCK_OUT,
                movement.getMovementType()
        );

        assertEquals(
                4,
                movement.getQuantity()
        );

        assertEquals(
                "SALE",
                movement.getReferenceType()
        );

        assertEquals(
                sale.getId(),
                movement.getReferenceId()
        );
    }

    @Test
    void shouldRollbackEntireSaleWhenStockIsInsufficient() {

        Product product =
                productRepository.save(
                        new Product(
                                "Monitor",
                                "MON-001",
                                30000,
                                25000,
                                2
                        )
                );

        Sale sale =
                new Sale("INV-004");

        sale.addItem(
                new SaleItem(
                        product.getId(),
                        5,
                        30000,
                        0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> saleService.createSale(sale)
        );

        assertNull(
                saleRepository.findById(
                        sale.getId() == null
                                ? -1L
                                : sale.getId()
                )
        );

        Product unchanged =
                productRepository.findById(
                        product.getId()
                );

        assertEquals(
                2,
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
    void shouldRejectDuplicateProductsInSale() {

        Product product =
                productRepository.save(
                        new Product(
                                "Laptop",
                                "LAP-002",
                                150000,
                                120000,
                                10
                        )
                );

        Sale sale =
                new Sale("INV-DUP-001");

        sale.addItem(
                new SaleItem(
                        product.getId(),
                        2,
                        150000,
                        0
                )
        );

        sale.addItem(
                new SaleItem(
                        product.getId(),
                        3,
                        150000,
                        0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> saleService.createSale(sale)
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
    void shouldCancelSaleAndRestoreStock() {

        Product product =
                productRepository.save(
                        new Product(
                                "Laptop",
                                "LAP-CANCEL-001",
                                150000,
                                120000,
                                10
                        )
                );

        Sale sale =
                new Sale("INV-CANCEL-001");

        sale.addItem(
                new SaleItem(
                        product.getId(),
                        3,
                        150000,
                        0
                )
        );

        saleService.createSale(sale);

        Product afterSale =
                productRepository.findById(
                        product.getId()
                );

        assertEquals(
                7,
                afterSale.getStockQuantity()
        );

        saleService.cancelSale(
                sale.getId()
        );

        Product restored =
                productRepository.findById(
                        product.getId()
                );

        assertEquals(
                10,
                restored.getStockQuantity()
        );

        Sale cancelled =
                saleRepository.findById(
                        sale.getId()
                );

        assertEquals(
                SaleStatus.CANCELLED,
                cancelled.getSaleStatus()
        );
    }

    @Test
    void shouldCreateReturnMovementWhenSaleIsCancelled() {

        Product product =
                productRepository.save(
                        new Product(
                                "Mouse",
                                "MOU-CANCEL-001",
                                2500,
                                1500,
                                20
                        )
                );

        Sale sale =
                new Sale("INV-CANCEL-002");

        sale.addItem(
                new SaleItem(
                        product.getId(),
                        4,
                        2500,
                        0
                )
        );

        saleService.createSale(sale);

        saleService.cancelSale(
                sale.getId()
        );

        var movements =
                movementRepository.findByProductId(
                        product.getId()
                );

        assertEquals(
                2,
                movements.size()
        );

        InventoryMovement returnMovement =
                movements.stream()
                        .filter(movement ->
                                movement.getMovementType()
                                        == InventoryMovementType.RETURN_IN
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                InventoryMovementType.RETURN_IN,
                returnMovement.getMovementType()
        );

        assertEquals(
                4,
                returnMovement.getQuantity()
        );

        assertEquals(
                "SALE",
                returnMovement.getReferenceType()
        );

        assertEquals(
                sale.getId(),
                returnMovement.getReferenceId()
        );
    }

    @Test
    void shouldRejectAlreadyCancelledSale() {

        Product product =
                productRepository.save(
                        new Product(
                                "Keyboard",
                                "KEY-CANCEL-001",
                                5000,
                                3500,
                                10
                        )
                );

        Sale sale =
                new Sale("INV-CANCEL-003");

        sale.addItem(
                new SaleItem(
                        product.getId(),
                        2,
                        5000,
                        0
                )
        );

        saleService.createSale(sale);

        saleService.cancelSale(
                sale.getId()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> saleService.cancelSale(
                        sale.getId()
                )
        );

        Product productAfter =
                productRepository.findById(
                        product.getId()
                );

        assertEquals(
                10,
                productAfter.getStockQuantity()
        );
    }

    @Test
    void shouldNotAllowCancellationWhenSaleHasPayments() throws SQLException {

        Product product =
                productRepository.save(
                        new Product(
                                "Laptop",
                                "LAP-PAY-CANCEL-001",
                                150000,
                                120000,
                                10
                        )
                );

        Sale sale =
                new Sale("INV-PAY-CANCEL-001");

        sale.addItem(
                new SaleItem(
                        product.getId(),
                        2,
                        150000,
                        0
                )
        );

        saleService.createSale(sale);

        /*
         * Create an actual payment.
         */
        Payment payment =
                new Payment(
                        sale.getId(),
                        300000,
                        PaymentMethod.CASH
                );

        paymentRepository.save(
                keepAliveConnection,
                payment
        );

        assertNotNull(
                payment.getId()
        );

        /*
         * Cancellation must be rejected.
         */
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> saleService.cancelSale(
                                sale.getId()
                        )
                );

        assertEquals(
                "Cannot cancel a sale that has payments: "
                        + sale.getId(),
                exception.getMessage()
        );

        /*
         * Stock must remain unchanged.
         */
        Product unchanged =
                productRepository.findById(
                        product.getId()
                );

        assertEquals(
                8,
                unchanged.getStockQuantity()
        );

        /*
         * Sale must remain active.
         */
        Sale unchangedSale =
                saleRepository.findById(
                        sale.getId()
                );

        assertNotEquals(
                SaleStatus.CANCELLED,
                unchangedSale.getSaleStatus()
        );

        /*
         * No RETURN_IN movement should be created.
         */
        var movements =
                movementRepository.findByProductId(
                        product.getId()
                );

        assertEquals(
                1,
                movements.size()
        );

        assertEquals(
                InventoryMovementType.STOCK_OUT,
                movements.get(0).getMovementType()
        );
    }
}