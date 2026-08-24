package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.Product;
import com.innovatewithomer.bizora.repository.FakeProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {

        productService =
                new ProductService(
                        new FakeProductRepository()
                );
    }

    @Test
    void shouldCreateValidProduct() {

        Product product = new Product(
                "Laptop",
                "LAP-001",
                150000,
                120000,
                5
        );

        Product saved =
                productService.createProduct(product);

        assertNotNull(saved.getId());

        assertEquals(
                "Laptop",
                saved.getName()
        );
    }

    @Test
    void shouldRejectBlankProductName() {

        Product product = new Product(
                "",
                "SKU-001",
                1000,
                500,
                5
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(product)
        );
    }

    @Test
    void shouldRejectNegativeSellingPrice() {

        Product product = new Product(
                "Laptop",
                "LAP-001",
                -100,
                500,
                5
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(product)
        );
    }

    @Test
    void shouldRejectNegativePurchasePrice() {

        Product product = new Product(
                "Laptop",
                "LAP-001",
                1000,
                -500,
                5
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(product)
        );
    }

    @Test
    void shouldRejectNegativeStock() {

        Product product = new Product(
                "Laptop",
                "LAP-001",
                1000,
                500,
                -5
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(product)
        );
    }

    @Test
    void shouldRejectNullProduct() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(null)
        );
    }

    @Test
    void shouldFindProduct() {

        Product product = new Product(
                "Mouse",
                "MOU-001",
                2500,
                1500,
                10
        );

        Product saved =
                productService.createProduct(product);

        Product found =
                productService.getProduct(
                        saved.getId()
                );

        assertEquals(
                "Mouse",
                found.getName()
        );
    }

    @Test
    void shouldRejectMissingProduct() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.getProduct(999L)
        );
    }
}