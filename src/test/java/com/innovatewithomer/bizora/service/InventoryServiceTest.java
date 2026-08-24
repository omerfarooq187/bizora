package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.InventoryMovement;
import com.innovatewithomer.bizora.model.InventoryMovementType;
import com.innovatewithomer.bizora.model.Product;
import com.innovatewithomer.bizora.repository.InventoryMovementRepository;
import com.innovatewithomer.bizora.repository.ProductRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {

    private ProductRepository productRepository;
    private InventoryMovementRepository movementRepository;
    private InventoryService inventoryService;

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

        productRepository =
                new ProductRepository();

        movementRepository =
                new InventoryMovementRepository();

        inventoryService =
                new InventoryService(
                        productRepository,
                        movementRepository
                );
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    private Product createProduct(
            double stock
    ) {

        return productRepository.save(
                new Product(
                        "Test Product",
                        "TEST-" + System.nanoTime(),
                        1000,
                        500,
                        stock
                )
        );
    }

    @Test
    void shouldStockIn() {

        Product product =
                createProduct(10);

        inventoryService.stockIn(
                product.getId(),
                5,
                "PURCHASE",
                null,
                "Stock received"
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

        List<InventoryMovement> movements =
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
                5,
                movement.getQuantity()
        );
    }

    @Test
    void shouldStockOut() {

        Product product =
                createProduct(10);

        inventoryService.stockOut(
                product.getId(),
                4,
                "SALE",
                null,
                "Customer sale"
        );

        Product updated =
                productRepository.findById(
                        product.getId()
                );

        assertNotNull(updated);

        assertEquals(
                6,
                updated.getStockQuantity()
        );

        List<InventoryMovement> movements =
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
    }

    @Test
    void shouldRejectStockOutWhenInsufficientStock() {

        Product product =
                createProduct(5);

        assertThrows(
                RuntimeException.class,
                () -> inventoryService.stockOut(
                        product.getId(),
                        10,
                        "SALE",
                        null,
                        "Too much stock"
                )
        );

        Product unchanged =
                productRepository.findById(
                        product.getId()
                );

        assertNotNull(unchanged);

        assertEquals(
                5,
                unchanged.getStockQuantity()
        );

        List<InventoryMovement> movements =
                movementRepository.findByProductId(
                        product.getId()
                );

        assertEquals(
                0,
                movements.size()
        );
    }

    @Test
    void shouldRejectZeroQuantity() {

        Product product =
                createProduct(10);

        assertThrows(
                RuntimeException.class,
                () -> inventoryService.stockIn(
                        product.getId(),
                        0,
                        "PURCHASE",
                        null,
                        "Invalid quantity"
                )
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {

        Product product =
                createProduct(10);

        assertThrows(
                RuntimeException.class,
                () -> inventoryService.stockIn(
                        product.getId(),
                        -5,
                        "PURCHASE",
                        null,
                        "Invalid quantity"
                )
        );
    }

    @Test
    void shouldRejectUnknownProduct() {

        assertThrows(
                RuntimeException.class,
                () -> inventoryService.stockIn(
                        999999L,
                        5,
                        "PURCHASE",
                        null,
                        "Unknown product"
                )
        );
    }

    @Test
    void shouldAdjustStockIn() {

        Product product =
                createProduct(10);

        inventoryService.adjustStock(
                product.getId(),
                15,
                "Physical stock count"
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

        List<InventoryMovement> movements =
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
                InventoryMovementType.ADJUSTMENT_IN,
                movement.getMovementType()
        );

        assertEquals(
                5,
                movement.getQuantity()
        );
    }

    @Test
    void shouldAdjustStockOut() {

        Product product =
                createProduct(10);

        inventoryService.adjustStock(
                product.getId(),
                7,
                "Physical stock count"
        );

        Product updated =
                productRepository.findById(
                        product.getId()
                );

        assertNotNull(updated);

        assertEquals(
                7,
                updated.getStockQuantity()
        );

        List<InventoryMovement> movements =
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
                InventoryMovementType.ADJUSTMENT_OUT,
                movement.getMovementType()
        );

        assertEquals(
                3,
                movement.getQuantity()
        );
    }

    @Test
    void shouldNotCreateMovementWhenNoAdjustmentIsNeeded() {

        Product product =
                createProduct(10);

        inventoryService.adjustStock(
                product.getId(),
                10,
                "Physical stock count"
        );

        Product updated =
                productRepository.findById(
                        product.getId()
                );

        assertNotNull(updated);

        assertEquals(
                10,
                updated.getStockQuantity()
        );

        List<InventoryMovement> movements =
                movementRepository.findByProductId(
                        product.getId()
                );

        assertEquals(
                0,
                movements.size()
        );
    }

    @Test
    void shouldRejectNegativeActualStock() {

        Product product =
                createProduct(10);

        assertThrows(
                RuntimeException.class,
                () -> inventoryService.adjustStock(
                        product.getId(),
                        -1,
                        "Invalid physical count"
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

        List<InventoryMovement> movements =
                movementRepository.findByProductId(
                        product.getId()
                );

        assertEquals(
                0,
                movements.size()
        );
    }

    @Test
    void shouldRollbackStockWhenMovementFails() {

        Product product =
                createProduct(10);

        InventoryService service = getService();

        assertThrows(
                RuntimeException.class,
                () -> service.stockOut(
                        product.getId(),
                        5,
                        "SALE",
                        null,
                        "Test rollback"
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

    private InventoryService getService() {
        InventoryMovementRepository failingRepository =
                new InventoryMovementRepository() {

                    @Override
                    public void save(
                            Connection connection,
                            InventoryMovement movement
                    ) throws SQLException {

                        throw new SQLException(
                                "Simulated database failure"
                        );
                    }
                };

        InventoryService service =
                new InventoryService(
                        productRepository,
                        failingRepository
                );
        return service;
    }

    @Test
    void shouldRollbackAdjustmentWhenMovementFails() {

        Product product = createProduct(10);

        InventoryService service = getInventoryService();

        assertThrows(
                RuntimeException.class,
                () -> service.adjustStock(
                        product.getId(),
                        20,
                        "Test adjustment rollback"
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

    private InventoryService getInventoryService() {
        InventoryMovementRepository failingRepository =
                new InventoryMovementRepository() {

                    @Override
                    public void save(
                            Connection connection,
                            InventoryMovement movement
                    ) throws SQLException {

                        throw new SQLException(
                                "Simulated movement failure"
                        );
                    }
                };

        InventoryService service =
                new InventoryService(
                        productRepository,
                        failingRepository
                );
        return service;
    }
}