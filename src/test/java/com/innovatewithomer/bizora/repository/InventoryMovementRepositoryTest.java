package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.InventoryMovement;
import com.innovatewithomer.bizora.model.InventoryMovementType;
import com.innovatewithomer.bizora.model.Product;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryMovementRepositoryTest {

    private InventoryMovementRepository repository;
    private ProductRepository productRepository;
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

        repository =
                new InventoryMovementRepository();

        productRepository =
                new ProductRepository();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    @Test
    void shouldSaveMovement() {

        Product product =
                productRepository.save(
                        new Product(
                                "Laptop",
                                "LAP-001",
                                150000,
                                120000,
                                5
                        )
                );

        InventoryMovement movement =
                new InventoryMovement(
                        product.getId(),
                        InventoryMovementType.STOCK_IN,
                        10,
                        "PURCHASE",
                        null,
                        "Initial stock"
                );

        repository.save(movement);

        assertNotNull(movement.getId());

        assertEquals(
                product.getId(),
                movement.getProductId()
        );

        assertEquals(
                InventoryMovementType.STOCK_IN,
                movement.getMovementType()
        );

        assertEquals(
                10,
                movement.getQuantity()
        );
    }

    @Test
    void shouldFindMovementsByProductId() {

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

        repository.save(
                new InventoryMovement(
                        product.getId(),
                        InventoryMovementType.STOCK_IN,
                        20,
                        "PURCHASE",
                        null,
                        "Stock received"
                )
        );

        repository.save(
                new InventoryMovement(
                        product.getId(),
                        InventoryMovementType.STOCK_OUT,
                        5,
                        "MANUAL",
                        null,
                        "Damaged items"
                )
        );

        List<InventoryMovement> movements =
                repository.findByProductId(
                        product.getId()
                );

        assertEquals(
                2,
                movements.size()
        );
    }

    @Test
    void shouldRejectMovementForNonExistingProduct() {

        InventoryMovement movement =
                new InventoryMovement(
                        999999L,
                        InventoryMovementType.STOCK_IN,
                        10,
                        "PURCHASE",
                        null,
                        "Invalid product"
                );

        assertThrows(
                RuntimeException.class,
                () -> repository.save(movement)
        );
    }
}