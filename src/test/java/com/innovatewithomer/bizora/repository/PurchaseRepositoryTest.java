package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.PaymentStatus;
import com.innovatewithomer.bizora.model.Purchase;
import com.innovatewithomer.bizora.model.PurchaseStatus;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseRepositoryTest {

    private PurchaseRepository purchaseRepository;

    private Connection keepAliveConnection;

    @BeforeEach
    void setUp() throws Exception {

        DatabaseManager.setJdbcUrl(
                "jdbc:sqlite:file:bizora_purchase_repository_test_"
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
        }

        purchaseRepository =
                new PurchaseRepository();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    @Test
    void shouldSavePurchaseWithoutSupplier() throws Exception {

        Purchase purchase =
                new Purchase("PUR-001");

        purchase.setSubtotal(10000);
        purchase.setDiscount(500);
        purchase.setTax(1000);
        purchase.setTotal(10500);

        purchaseRepository.save(
                keepAliveConnection,
                purchase
        );

        assertNotNull(
                purchase.getId()
        );

        assertTrue(
                purchase.getId() > 0
        );
    }

    @Test
    void shouldSavePurchaseWithSupplier() throws Exception {

        long supplierId;

        try (Statement statement =
                     keepAliveConnection.createStatement()) {

            statement.execute("""
                INSERT INTO suppliers
                (
                    name,
                    phone,
                    email,
                    address,
                    created_at
                )
                VALUES
                (
                    'ABC Suppliers',
                    '03001234567',
                    'abc@example.com',
                    'Islamabad',
                    '2026-08-20T12:00:00'
                )
                """);

            try (var resultSet =
                         statement.executeQuery(
                                 "SELECT last_insert_rowid()"
                         )) {

                assertTrue(resultSet.next());

                supplierId =
                        resultSet.getLong(1);
            }
        }

        Purchase purchase =
                new Purchase("PUR-002");

        purchase.setSupplierId(
                supplierId
        );

        purchase.setSubtotal(20000);
        purchase.setDiscount(1000);
        purchase.setTax(1900);
        purchase.setTotal(20900);

        purchaseRepository.save(
                keepAliveConnection,
                purchase
        );

        assertNotNull(
                purchase.getId()
        );

        Purchase saved =
                purchaseRepository.findById(
                        keepAliveConnection,
                        purchase.getId()
                );

        assertNotNull(saved);

        assertEquals(
                supplierId,
                saved.getSupplierId()
        );
    }

    @Test
    void shouldFindPurchaseById() throws Exception {

        Purchase purchase =
                createPurchase(
                        "PUR-003",
                        null,
                        15000,
                        500,
                        1000,
                        15500
                );

        purchaseRepository.save(
                keepAliveConnection,
                purchase
        );

        Purchase found =
                purchaseRepository.findById(
                        keepAliveConnection,
                        purchase.getId()
                );

        assertNotNull(found);

        assertEquals(
                purchase.getId(),
                found.getId()
        );

        assertEquals(
                "PUR-003",
                found.getInvoiceNumber()
        );
    }

    @Test
    void shouldReturnNullWhenPurchaseDoesNotExist()
            throws Exception {

        Purchase found =
                purchaseRepository.findById(
                        keepAliveConnection,
                        999999L
                );

        assertNull(found);
    }

    @Test
    void shouldPersistAllMonetaryFields()
            throws Exception {

        Purchase purchase =
                createPurchase(
                        "PUR-004",
                        null,
                        50000,
                        2500,
                        4750,
                        52250
                );

        purchaseRepository.save(
                keepAliveConnection,
                purchase
        );

        Purchase saved =
                purchaseRepository.findById(
                        keepAliveConnection,
                        purchase.getId()
                );

        assertNotNull(saved);

        assertEquals(
                50000,
                saved.getSubtotal()
        );

        assertEquals(
                2500,
                saved.getDiscount()
        );

        assertEquals(
                4750,
                saved.getTax()
        );

        assertEquals(
                52250,
                saved.getTotal()
        );
    }

    @Test
    void shouldPersistPaymentStatus()
            throws Exception {

        Purchase purchase =
                createPurchase(
                        "PUR-005",
                        null,
                        10000,
                        0,
                        0,
                        10000
                );

        purchase.setPaymentStatus(
                PaymentStatus.PAID
        );

        purchaseRepository.save(
                keepAliveConnection,
                purchase
        );

        Purchase saved =
                purchaseRepository.findById(
                        keepAliveConnection,
                        purchase.getId()
                );

        assertNotNull(saved);

        assertEquals(
                PaymentStatus.PAID,
                saved.getPaymentStatus()
        );
    }

    @Test
    void shouldPersistPartiallyPaidStatus()
            throws Exception {

        Purchase purchase =
                createPurchase(
                        "PUR-006",
                        null,
                        10000,
                        0,
                        0,
                        10000
                );

        purchase.setPaymentStatus(
                PaymentStatus.PARTIALLY_PAID
        );

        purchaseRepository.save(
                keepAliveConnection,
                purchase
        );

        Purchase saved =
                purchaseRepository.findById(
                        keepAliveConnection,
                        purchase.getId()
                );

        assertNotNull(saved);

        assertEquals(
                PaymentStatus.PARTIALLY_PAID,
                saved.getPaymentStatus()
        );
    }

    @Test
    void shouldPersistPurchaseStatus()
            throws Exception {

        Purchase purchase =
                createPurchase(
                        "PUR-007",
                        null,
                        10000,
                        0,
                        0,
                        10000
                );

        purchase.setPurchaseStatus(
                PurchaseStatus.CANCELLED
        );

        purchaseRepository.save(
                keepAliveConnection,
                purchase
        );

        Purchase saved =
                purchaseRepository.findById(
                        keepAliveConnection,
                        purchase.getId()
                );

        assertNotNull(saved);

        assertEquals(
                PurchaseStatus.CANCELLED,
                saved.getPurchaseStatus()
        );
    }

    @Test
    void shouldPersistCreatedAt()
            throws Exception {

        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        8,
                        20,
                        15,
                        30,
                        45
                );

        Purchase purchase =
                createPurchase(
                        "PUR-008",
                        null,
                        10000,
                        0,
                        0,
                        10000
                );

        purchase.setCreatedAt(
                createdAt
        );

        purchaseRepository.save(
                keepAliveConnection,
                purchase
        );

        Purchase saved =
                purchaseRepository.findById(
                        keepAliveConnection,
                        purchase.getId()
                );

        assertNotNull(saved);

        assertEquals(
                createdAt,
                saved.getCreatedAt()
        );
    }

    @Test
    void shouldPreserveNullSupplier()
            throws Exception {

        Purchase purchase =
                createPurchase(
                        "PUR-009",
                        null,
                        10000,
                        0,
                        0,
                        10000
                );

        assertNull(
                purchase.getSupplierId()
        );

        purchaseRepository.save(
                keepAliveConnection,
                purchase
        );

        Purchase saved =
                purchaseRepository.findById(
                        keepAliveConnection,
                        purchase.getId()
                );

        assertNotNull(saved);

        assertNull(
                saved.getSupplierId()
        );
    }

    @Test
    void shouldFindAllPurchases()
            throws Exception {

        Purchase first =
                createPurchase(
                        "PUR-010",
                        null,
                        10000,
                        0,
                        0,
                        10000
                );

        Purchase second =
                createPurchase(
                        "PUR-011",
                        null,
                        20000,
                        1000,
                        500,
                        19500
                );

        Purchase third =
                createPurchase(
                        "PUR-012",
                        null,
                        30000,
                        2000,
                        1000,
                        29000
                );

        purchaseRepository.save(
                keepAliveConnection,
                first
        );

        purchaseRepository.save(
                keepAliveConnection,
                second
        );

        purchaseRepository.save(
                keepAliveConnection,
                third
        );

        List<Purchase> purchases =
                purchaseRepository.findAll();

        assertEquals(
                3,
                purchases.size()
        );

        assertEquals(
                "PUR-012",
                purchases.get(0).getInvoiceNumber()
        );

        assertEquals(
                "PUR-011",
                purchases.get(1).getInvoiceNumber()
        );

        assertEquals(
                "PUR-010",
                purchases.get(2).getInvoiceNumber()
        );
    }

    @Test
    void shouldRejectDuplicateInvoiceNumber()
            throws Exception {

        Purchase first =
                createPurchase(
                        "PUR-DUP-001",
                        null,
                        10000,
                        0,
                        0,
                        10000
                );

        purchaseRepository.save(
                keepAliveConnection,
                first
        );

        Purchase duplicate =
                createPurchase(
                        "PUR-DUP-001",
                        null,
                        20000,
                        0,
                        0,
                        20000
                );

        assertThrows(
                java.sql.SQLException.class,
                () -> purchaseRepository.save(
                        keepAliveConnection,
                        duplicate
                )
        );
    }

    @Test
    void shouldRestoreDefaultPaymentStatus()
            throws Exception {

        Purchase purchase =
                new Purchase("PUR-013");

        purchase.setSubtotal(10000);
        purchase.setTotal(10000);

        assertEquals(
                PaymentStatus.UNPAID,
                purchase.getPaymentStatus()
        );

        purchaseRepository.save(
                keepAliveConnection,
                purchase
        );

        Purchase saved =
                purchaseRepository.findById(
                        keepAliveConnection,
                        purchase.getId()
                );

        assertNotNull(saved);

        assertEquals(
                PaymentStatus.UNPAID,
                saved.getPaymentStatus()
        );
    }

    @Test
    void shouldRestoreDefaultPurchaseStatus()
            throws Exception {

        Purchase purchase =
                new Purchase("PUR-014");

        purchase.setSubtotal(10000);
        purchase.setTotal(10000);

        assertEquals(
                PurchaseStatus.COMPLETED,
                purchase.getPurchaseStatus()
        );

        purchaseRepository.save(
                keepAliveConnection,
                purchase
        );

        Purchase saved =
                purchaseRepository.findById(
                        keepAliveConnection,
                        purchase.getId()
                );

        assertNotNull(saved);

        assertEquals(
                PurchaseStatus.COMPLETED,
                saved.getPurchaseStatus()
        );
    }

    private Purchase createPurchase(
            String invoiceNumber,
            Long supplierId,
            double subtotal,
            double discount,
            double tax,
            double total
    ) {

        Purchase purchase =
                new Purchase(invoiceNumber);

        purchase.setSupplierId(
                supplierId
        );

        purchase.setSubtotal(
                subtotal
        );

        purchase.setDiscount(
                discount
        );

        purchase.setTax(
                tax
        );

        purchase.setTotal(
                total
        );

        return purchase;
    }
}

