package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.Product;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductRepositoryTest {

    private ProductRepository repository;
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
        }

        repository = new ProductRepository();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    @Test
    void shouldSaveProduct() {

        Product product = new Product(
                "Laptop",
                "LAP-001",
                150000,
                120000,
                5
        );

        Product saved =
                repository.save(product);

        assertNotNull(saved.getId());

        assertEquals(
                "Laptop",
                saved.getName()
        );

        assertEquals(
                "LAP-001",
                saved.getSku()
        );
    }

    @Test
    void shouldFindProductById() {

        Product product = new Product(
                "Keyboard",
                "KEY-001",
                5000,
                3500,
                10
        );

        Product saved =
                repository.save(product);

        Product found =
                repository.findById(saved.getId());

        assertNotNull(found);

        assertEquals(
                "Keyboard",
                found.getName()
        );

        assertEquals(
                "KEY-001",
                found.getSku()
        );
    }

    @Test
    void shouldFindAllProducts() {

        repository.save(
                new Product(
                        "Mouse",
                        "MOU-001",
                        2500,
                        1500,
                        20
                )
        );

        repository.save(
                new Product(
                        "Monitor",
                        "MON-001",
                        30000,
                        25000,
                        7
                )
        );

        List<Product> products =
                repository.findAll();

        assertEquals(
                2,
                products.size()
        );
    }

    @Test
    void shouldUpdateProduct() {

        Product product = repository.save(
                new Product(
                        "Old Name",
                        "OLD-001",
                        1000,
                        700,
                        5
                )
        );

        product.setName("New Name");
        product.setSellingPrice(1200);
        product.setStockQuantity(10);

        repository.update(product);

        Product updated =
                repository.findById(product.getId());

        assertNotNull(updated);

        assertEquals(
                "New Name",
                updated.getName()
        );

        assertEquals(
                1200,
                updated.getSellingPrice()
        );

        assertEquals(
                10,
                updated.getStockQuantity()
        );
    }

    @Test
    void shouldDeleteProduct() {

        Product product = repository.save(
                new Product(
                        "Delete Me",
                        "DEL-001",
                        1000,
                        500,
                        2
                )
        );

        repository.delete(product.getId());

        Product deleted =
                repository.findById(product.getId());

        assertNull(deleted);
    }
}