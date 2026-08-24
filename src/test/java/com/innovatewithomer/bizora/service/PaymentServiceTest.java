package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.*;
import com.innovatewithomer.bizora.repository.PaymentRepository;
import com.innovatewithomer.bizora.repository.SaleRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentService paymentService;

    private PaymentRepository paymentRepository;
    private SaleRepository saleRepository;

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

        saleRepository =
                new SaleRepository();

        paymentRepository =
                new PaymentRepository();

        paymentService =
                new PaymentService(
                        paymentRepository,
                        saleRepository
                );
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

    private Sale createSale(
            String invoiceNumber,
            double total
    ) {

        Sale sale =
                new Sale(invoiceNumber);

        sale.setSubtotal(total);
        sale.setDiscount(0);
        sale.setTax(0);
        sale.setTotal(total);

        sale.setPaymentStatus(
                PaymentStatus.UNPAID
        );

        sale.setSaleStatus(
                SaleStatus.COMPLETED
        );

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            return saleRepository.save(
                    connection,
                    sale
            );

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------
    // First payment
    // -------------------------------------------------

    @Test
    void shouldAddFirstPayment() {

        Sale sale =
                createSale(
                        "INV-PAY-SERVICE-001",
                        10000
                );

        Payment payment =
                new Payment(
                        sale.getId(),
                        4000,
                        PaymentMethod.CASH
                );

        Payment saved =
                paymentService.addPayment(
                        payment
                );

        assertNotNull(
                saved.getId()
        );

        assertEquals(
                sale.getId(),
                saved.getSaleId()
        );

        assertEquals(
                4000,
                saved.getAmount()
        );

        Sale updated =
                saleRepository.findById(
                        sale.getId()
                );

        assertNotNull(updated);

        assertEquals(
                PaymentStatus.PARTIALLY_PAID,
                updated.getPaymentStatus()
        );
    }

    // -------------------------------------------------
    // Exact final payment
    // -------------------------------------------------

    @Test
    void shouldMarkSalePaidWhenFullyPaid() {

        Sale sale =
                createSale(
                        "INV-PAY-SERVICE-002",
                        10000
                );

        paymentService.addPayment(
                new Payment(
                        sale.getId(),
                        10000,
                        PaymentMethod.CASH
                )
        );

        Sale updated =
                saleRepository.findById(
                        sale.getId()
                );

        assertNotNull(updated);

        assertEquals(
                PaymentStatus.PAID,
                updated.getPaymentStatus()
        );
    }

    // -------------------------------------------------
    // Multiple payments
    // -------------------------------------------------

    @Test
    void shouldHandleMultiplePayments() {

        Sale sale =
                createSale(
                        "INV-PAY-SERVICE-003",
                        10000
                );

        paymentService.addPayment(
                new Payment(
                        sale.getId(),
                        3000,
                        PaymentMethod.CASH
                )
        );

        Sale afterFirst =
                saleRepository.findById(
                        sale.getId()
                );

        assertEquals(
                PaymentStatus.PARTIALLY_PAID,
                afterFirst.getPaymentStatus()
        );

        paymentService.addPayment(
                new Payment(
                        sale.getId(),
                        7000,
                        PaymentMethod.EASYPAISA
                )
        );

        Sale afterSecond =
                saleRepository.findById(
                        sale.getId()
                );

        assertEquals(
                PaymentStatus.PAID,
                afterSecond.getPaymentStatus()
        );

        List<Payment> payments =
                paymentRepository.findBySaleId(
                        sale.getId()
                );

        assertEquals(
                2,
                payments.size()
        );
    }

    // -------------------------------------------------
    // Overpayment
    // -------------------------------------------------

    @Test
    void shouldRejectOverpayment() {

        Sale sale =
                createSale(
                        "INV-PAY-SERVICE-004",
                        10000
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.addPayment(
                        new Payment(
                                sale.getId(),
                                10001,
                                PaymentMethod.CASH
                        )
                )
        );

        List<Payment> payments =
                paymentRepository.findBySaleId(
                        sale.getId()
                );

        assertTrue(
                payments.isEmpty()
        );

        Sale unchanged =
                saleRepository.findById(
                        sale.getId()
                );

        assertEquals(
                PaymentStatus.UNPAID,
                unchanged.getPaymentStatus()
        );
    }

    // -------------------------------------------------
    // Zero payment
    // -------------------------------------------------

    @Test
    void shouldRejectZeroPayment() {

        Sale sale =
                createSale(
                        "INV-PAY-SERVICE-005",
                        10000
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.addPayment(
                        new Payment(
                                sale.getId(),
                                0,
                                PaymentMethod.CASH
                        )
                )
        );

        assertTrue(
                paymentRepository
                        .findBySaleId(
                                sale.getId()
                        )
                        .isEmpty()
        );
    }

    // -------------------------------------------------
    // Negative payment
    // -------------------------------------------------

    @Test
    void shouldRejectNegativePayment() {

        Sale sale =
                createSale(
                        "INV-PAY-SERVICE-006",
                        10000
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.addPayment(
                        new Payment(
                                sale.getId(),
                                -500,
                                PaymentMethod.CASH
                        )
                )
        );

        assertTrue(
                paymentRepository
                        .findBySaleId(
                                sale.getId()
                        )
                        .isEmpty()
        );
    }

    // -------------------------------------------------
    // Cancelled sale
    // -------------------------------------------------

    @Test
    void shouldRejectPaymentForCancelledSale() throws SQLException {

        Sale sale =
                createSale(
                        "INV-PAY-SERVICE-007",
                        10000
                );

        saleRepository.updateStatus(
                getConnection(),
                sale.getId(),
                SaleStatus.CANCELLED
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.addPayment(
                        new Payment(
                                sale.getId(),
                                5000,
                                PaymentMethod.CASH
                        )
                )
        );

        assertTrue(
                paymentRepository
                        .findBySaleId(
                                sale.getId()
                        )
                        .isEmpty()
        );
    }

    // -------------------------------------------------
    // Non-existing sale
    // -------------------------------------------------

    @Test
    void shouldRejectPaymentForNonExistingSale() {

        Payment payment =
                new Payment(
                        999999L,
                        5000,
                        PaymentMethod.CASH
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.addPayment(
                        payment
                )
        );

        assertNull(
                payment.getId()
        );
    }

    // -------------------------------------------------
    // Missing payment method
    // -------------------------------------------------

    @Test
    void shouldRejectMissingPaymentMethod() {

        Sale sale =
                createSale(
                        "INV-PAY-SERVICE-008",
                        10000
                );

        Payment payment =
                new Payment();

        payment.setSaleId(
                sale.getId()
        );

        payment.setAmount(
                5000
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.addPayment(
                        payment
                )
        );
    }

    // -------------------------------------------------
    // Payment history
    // -------------------------------------------------

    @Test
    void shouldGetPaymentsForSale() {

        Sale sale =
                createSale(
                        "INV-PAY-SERVICE-009",
                        10000
                );

        paymentService.addPayment(
                new Payment(
                        sale.getId(),
                        2000,
                        PaymentMethod.CASH
                )
        );

        paymentService.addPayment(
                new Payment(
                        sale.getId(),
                        3000,
                        PaymentMethod.CARD
                )
        );

        List<Payment> payments =
                paymentService.getPaymentsForSale(
                        sale.getId()
                );

        assertEquals(
                2,
                payments.size()
        );

        assertEquals(
                2000,
                payments.get(0).getAmount()
        );

        assertEquals(
                PaymentMethod.CASH,
                payments.get(0).getPaymentMethod()
        );

        assertEquals(
                3000,
                payments.get(1).getAmount()
        );

        assertEquals(
                PaymentMethod.CARD,
                payments.get(1).getPaymentMethod()
        );
    }

    // -------------------------------------------------
    // Transaction rollback
    // -------------------------------------------------

    @Test
    void shouldRollbackPaymentWhenSaleStatusUpdateFails() {

        SaleRepository failingSaleRepository =
                new SaleRepository() {

                    @Override
                    public void updatePaymentStatus(
                            Connection connection,
                            Long saleId,
                            PaymentStatus status
                    ) throws java.sql.SQLException {

                        throw new java.sql.SQLException(
                                "Simulated failure"
                        );
                    }
                };

        PaymentService failingService =
                new PaymentService(
                        paymentRepository,
                        failingSaleRepository
                );

        Sale sale =
                createSale(
                        "INV-PAY-SERVICE-010",
                        10000
                );

        Payment payment =
                new Payment(
                        sale.getId(),
                        5000,
                        PaymentMethod.CASH
                );

        assertThrows(
                RuntimeException.class,
                () -> failingService.addPayment(
                        payment
                )
        );

        List<Payment> payments =
                paymentRepository.findBySaleId(
                        sale.getId()
                );

        assertTrue(
                payments.isEmpty()
        );
    }

    @Test
    void shouldMarkSalePartiallyPaid() {

        Sale sale =
                createSale(
                        "INV-PAY-001",
                        10000
                );

        Payment payment =
                new Payment(
                        sale.getId(),
                        4000,
                        PaymentMethod.CASH
                );

        paymentService.addPayment(payment);

        Sale updated =
                saleRepository.findById(
                        sale.getId()
                );

        assertNotNull(updated);

        assertEquals(
                PaymentStatus.PARTIALLY_PAID,
                updated.getPaymentStatus()
        );

        List<Payment> payments =
                paymentRepository.findBySaleId(
                        sale.getId()
                );

        assertEquals(
                1,
                payments.size()
        );

        assertEquals(
                4000,
                payments.get(0).getAmount()
        );
    }

    @Test
    void shouldMarkSalePaidWhenFullAmountIsPaid() {

        Sale sale =
                createSale(
                        "INV-PAY-002",
                        10000
                );

        Payment firstPayment =
                new Payment(
                        sale.getId(),
                        4000,
                        PaymentMethod.CASH
                );

        paymentService.addPayment(
                firstPayment
        );

        Payment secondPayment =
                new Payment(
                        sale.getId(),
                        6000,
                        PaymentMethod.CARD
                );

        paymentService.addPayment(
                secondPayment
        );

        Sale updated =
                saleRepository.findById(
                        sale.getId()
                );

        assertNotNull(updated);

        assertEquals(
                PaymentStatus.PAID,
                updated.getPaymentStatus()
        );

        List<Payment> payments =
                paymentRepository.findBySaleId(
                        sale.getId()
                );

        assertEquals(
                2,
                payments.size()
        );

        double totalPaid =
                payments.stream()
                        .mapToDouble(
                                Payment::getAmount
                        )
                        .sum();

        assertEquals(
                10000,
                totalPaid
        );
    }

    @Test
    void shouldUpdatePaymentStatusAfterEachPayment() {

        Sale sale =
                createSale(
                        "INV-PAY-003",
                        10000
                );

        Payment firstPayment =
                new Payment(
                        sale.getId(),
                        2500,
                        PaymentMethod.CASH
                );

        paymentService.addPayment(
                firstPayment
        );

        Sale afterFirst =
                saleRepository.findById(
                        sale.getId()
                );

        assertEquals(
                PaymentStatus.PARTIALLY_PAID,
                afterFirst.getPaymentStatus()
        );


        Payment secondPayment =
                new Payment(
                        sale.getId(),
                        2500,
                        PaymentMethod.CASH
                );

        paymentService.addPayment(
                secondPayment
        );

        Sale afterSecond =
                saleRepository.findById(
                        sale.getId()
                );

        assertEquals(
                PaymentStatus.PARTIALLY_PAID,
                afterSecond.getPaymentStatus()
        );


        Payment finalPayment =
                new Payment(
                        sale.getId(),
                        5000,
                        PaymentMethod.CARD
                );

        paymentService.addPayment(
                finalPayment
        );

        Sale afterFinal =
                saleRepository.findById(
                        sale.getId()
                );

        assertEquals(
                PaymentStatus.PAID,
                afterFinal.getPaymentStatus()
        );
    }

    @Test
    void shouldRejectPaymentExceedingSaleTotal() {

        Sale sale =
                createSale(
                        "INV-PAY-004",
                        10000
                );

        Payment payment =
                new Payment(
                        sale.getId(),
                        10001,
                        PaymentMethod.CASH
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        paymentService.addPayment(
                                payment
                        )
        );

        List<Payment> payments =
                paymentRepository.findBySaleId(
                        sale.getId()
                );

        assertTrue(
                payments.isEmpty()
        );

        Sale unchanged =
                saleRepository.findById(
                        sale.getId()
                );

        assertEquals(
                PaymentStatus.UNPAID,
                unchanged.getPaymentStatus()
        );
    }


    @Test
    void shouldRejectPaymentExceedingRemainingBalance() {

        Sale sale =
                createSale(
                        "INV-PAY-005",
                        10000
                );

        Payment firstPayment =
                new Payment(
                        sale.getId(),
                        7000,
                        PaymentMethod.CASH
                );

        paymentService.addPayment(
                firstPayment
        );

        Payment invalidPayment =
                new Payment(
                        sale.getId(),
                        4000,
                        PaymentMethod.CARD
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        paymentService.addPayment(
                                invalidPayment
                        )
        );

        List<Payment> payments =
                paymentRepository.findBySaleId(
                        sale.getId()
                );

        assertEquals(
                1,
                payments.size()
        );

        assertEquals(
                7000,
                payments.get(0).getAmount()
        );

        Sale unchanged =
                saleRepository.findById(
                        sale.getId()
                );

        assertEquals(
                PaymentStatus.PARTIALLY_PAID,
                unchanged.getPaymentStatus()
        );
    }

    @Test
    void shouldRejectPaymentWithoutSaleId() {

        Payment payment =
                new Payment();

        payment.setAmount(5000);
        payment.setPaymentMethod(
                PaymentMethod.CASH
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        paymentService.addPayment(
                                payment
                        )
        );
    }




    // -------------------------------------------------
    // Helper connection
    // -------------------------------------------------

    private Connection getConnection() {

        try {
            return DatabaseManager.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}