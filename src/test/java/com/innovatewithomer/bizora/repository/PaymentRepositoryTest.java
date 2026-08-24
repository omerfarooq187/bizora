package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.Payment;
import com.innovatewithomer.bizora.model.PaymentMethod;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRepositoryTest {

    private PaymentRepository repository;

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
                    created_at TEXT NOT NULL
                )
                """);

            // -----------------------------------------
            // PAYMENTS
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

        repository =
                new PaymentRepository();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    // -------------------------------------------------
    // Helper
    // -------------------------------------------------

    private Long createSale(
            Connection connection,
            String invoiceNumber
    ) throws Exception {

        String sql = """
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
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (
                var statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setString(
                    1,
                    invoiceNumber
            );

            statement.setDouble(
                    2,
                    10000
            );

            statement.setDouble(
                    3,
                    0
            );

            statement.setDouble(
                    4,
                    0
            );

            statement.setDouble(
                    5,
                    10000
            );

            statement.setString(
                    6,
                    "UNPAID"
            );

            statement.setString(
                    7,
                    "COMPLETED"
            );

            statement.setString(
                    8,
                    java.time.LocalDateTime.now()
                            .toString()
            );

            statement.executeUpdate();

            try (var keys =
                         statement.getGeneratedKeys()) {

                assertTrue(keys.next());

                return keys.getLong(1);
            }
        }
    }

    // -------------------------------------------------
    // Save
    // -------------------------------------------------

    @Test
    void shouldSavePayment() throws Exception {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            Long saleId =
                    createSale(
                            connection,
                            "INV-PAY-001"
                    );

            Payment payment =
                    new Payment(
                            saleId,
                            5000,
                            PaymentMethod.CASH
                    );

            Payment saved =
                    repository.save(
                            connection,
                            payment
                    );

            assertNotNull(
                    saved.getId()
            );

            assertEquals(
                    saleId,
                    saved.getSaleId()
            );

            assertEquals(
                    5000,
                    saved.getAmount()
            );

            assertEquals(
                    PaymentMethod.CASH,
                    saved.getPaymentMethod()
            );
        }
    }

    // -------------------------------------------------
    // Find by ID
    // -------------------------------------------------

    @Test
    void shouldFindPaymentById() throws Exception {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            Long saleId =
                    createSale(
                            connection,
                            "INV-PAY-002"
                    );

            Payment payment =
                    new Payment(
                            saleId,
                            7500,
                            PaymentMethod.BANK_TRANSFER
                    );

            payment.setReference(
                    "BANK-REF-123"
            );

            Payment saved =
                    repository.save(
                            connection,
                            payment
                    );

            Payment found =
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
                    saleId,
                    found.getSaleId()
            );

            assertEquals(
                    7500,
                    found.getAmount()
            );

            assertEquals(
                    PaymentMethod.BANK_TRANSFER,
                    found.getPaymentMethod()
            );

            assertEquals(
                    "BANK-REF-123",
                    found.getReference()
            );
        }
    }

    // -------------------------------------------------
    // Non-existing payment
    // -------------------------------------------------

    @Test
    void shouldReturnNullForNonExistingPayment() {

        Payment payment =
                repository.findById(
                        999999L
                );

        assertNull(payment);
    }

    // -------------------------------------------------
    // Find by sale ID
    // -------------------------------------------------

    @Test
    void shouldFindPaymentsBySaleId() throws Exception {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            Long saleId =
                    createSale(
                            connection,
                            "INV-PAY-003"
                    );

            Payment cash =
                    new Payment(
                            saleId,
                            3000,
                            PaymentMethod.CASH
                    );

            Payment easypaisa =
                    new Payment(
                            saleId,
                            2000,
                            PaymentMethod.EASYPAISA
                    );

            repository.save(
                    connection,
                    cash
            );

            repository.save(
                    connection,
                    easypaisa
            );
        }

        List<Payment> payments =
                repository.findBySaleId(
                        1L
                );

        assertEquals(
                2,
                payments.size()
        );

        assertEquals(
                3000,
                payments.get(0).getAmount()
        );

        assertEquals(
                PaymentMethod.CASH,
                payments.get(0).getPaymentMethod()
        );

        assertEquals(
                2000,
                payments.get(1).getAmount()
        );

        assertEquals(
                PaymentMethod.EASYPAISA,
                payments.get(1).getPaymentMethod()
        );
    }

    // -------------------------------------------------
    // Payment method mapping
    // -------------------------------------------------

    @Test
    void shouldMapPaymentMethod() throws Exception {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            Long saleId =
                    createSale(
                            connection,
                            "INV-PAY-004"
                    );

            Payment payment =
                    new Payment(
                            saleId,
                            1000,
                            PaymentMethod.JAZZCASH
                    );

            repository.save(
                    connection,
                    payment
            );

            Payment found =
                    repository.findById(
                            connection,
                            payment.getId()
                    );

            assertNotNull(found);

            assertEquals(
                    PaymentMethod.JAZZCASH,
                    found.getPaymentMethod()
            );
        }
    }

    // -------------------------------------------------
    // Reference can be null
    // -------------------------------------------------

    @Test
    void shouldAllowNullReference() throws Exception {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            Long saleId =
                    createSale(
                            connection,
                            "INV-PAY-005"
                    );

            Payment payment =
                    new Payment(
                            saleId,
                            1500,
                            PaymentMethod.CASH
                    );

            assertNull(
                    payment.getReference()
            );

            Payment saved =
                    repository.save(
                            connection,
                            payment
                    );

            Payment found =
                    repository.findById(
                            connection,
                            saved.getId()
                    );

            assertNotNull(found);

            assertNull(
                    found.getReference()
            );
        }
    }

    // -------------------------------------------------
    // Multiple sales are isolated
    // -------------------------------------------------

    @Test
    void shouldOnlyReturnPaymentsForRequestedSale()
            throws Exception {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            Long firstSaleId =
                    createSale(
                            connection,
                            "INV-PAY-006"
                    );

            Long secondSaleId =
                    createSale(
                            connection,
                            "INV-PAY-007"
                    );

            repository.save(
                    connection,
                    new Payment(
                            firstSaleId,
                            1000,
                            PaymentMethod.CASH
                    )
            );

            repository.save(
                    connection,
                    new Payment(
                            secondSaleId,
                            2000,
                            PaymentMethod.CARD
                    )
            );

            List<Payment> payments =
                    repository.findBySaleId(
                            connection,
                            firstSaleId
                    );

            assertEquals(
                    1,
                    payments.size()
            );

            assertEquals(
                    firstSaleId,
                    payments.get(0).getSaleId()
            );

            assertEquals(
                    1000,
                    payments.get(0).getAmount()
            );
        }
    }
}