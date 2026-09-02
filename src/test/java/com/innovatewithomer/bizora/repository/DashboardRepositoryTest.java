package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.dashboard.DashboardSummary;
import com.innovatewithomer.bizora.model.dashboard.LowStockProduct;
import com.innovatewithomer.bizora.model.dashboard.RecentSale;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DashboardRepositoryTest {

    private DashboardRepository repository;
    private Connection keepAliveConnection;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.setJdbcUrl(
                "jdbc:sqlite:file:bizora_test_"
                        + System.nanoTime()
                        + "?mode=memory&cache=shared"
        );

        keepAliveConnection = DatabaseManager.getConnection();

        try (Statement statement = keepAliveConnection.createStatement()) {
            statement.execute("""
                CREATE TABLE customers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    phone TEXT,
                    email TEXT,
                    address TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

            statement.execute("""
                CREATE TABLE products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    sku TEXT UNIQUE,
                    selling_price REAL NOT NULL DEFAULT 0,
                    purchase_price REAL NOT NULL DEFAULT 0,
                    stock_quantity REAL NOT NULL DEFAULT 0,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

            statement.execute("""
                CREATE TABLE sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER,
                    invoice_number TEXT UNIQUE NOT NULL,
                    subtotal REAL NOT NULL DEFAULT 0,
                    discount REAL NOT NULL DEFAULT 0,
                    tax REAL NOT NULL DEFAULT 0,
                    total REAL NOT NULL DEFAULT 0,
                    payment_status TEXT NOT NULL DEFAULT 'UNPAID',
                    sale_status TEXT NOT NULL DEFAULT 'COMPLETED',
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

            statement.execute("""
                CREATE TABLE purchases (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    supplier_id INTEGER,
                    invoice_number TEXT UNIQUE NOT NULL,
                    subtotal REAL NOT NULL DEFAULT 0,
                    discount REAL NOT NULL DEFAULT 0,
                    tax REAL NOT NULL DEFAULT 0,
                    total REAL NOT NULL DEFAULT 0,
                    payment_status TEXT NOT NULL DEFAULT 'UNPAID',
                    purchase_status TEXT NOT NULL DEFAULT 'COMPLETED',
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

            statement.execute("""
                CREATE TABLE sale_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER NOT NULL,
                    quantity REAL NOT NULL,
                    cost_price REAL NOT NULL DEFAULT 0
                );
            """);

            statement.execute("""
                CREATE TABLE expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category TEXT NOT NULL,
                    description TEXT,
                    amount REAL NOT NULL,
                    expense_date DATE NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);
        }

        repository = new DashboardRepository();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (keepAliveConnection != null && !keepAliveConnection.isClosed()) {
            keepAliveConnection.close();
        }
        DatabaseManager.resetJdbcUrl();
    }

    @Test
    void shouldReturnEmptySummaryWhenDatabaseIsEmpty() {
        DashboardSummary summary = repository.getSummary();

        assertNotNull(summary);
        assertEquals(0.0, summary.getTodayRevenue());
        assertEquals(0, summary.getTodaySalesCount());
        assertEquals(0, summary.getTotalProductCount());
        assertEquals(0, summary.getLowStockCount());
        assertEquals(0.0, summary.getTodayPurchases());
        assertEquals(0.0, summary.getTodayExpenses());
        assertEquals(0.0, summary.getTodayProfit());
    }

    @Test
    void shouldComputeSummaryWithSalesPurchasesAndExpenses() throws Exception {
        try (Statement statement = keepAliveConnection.createStatement()) {
            statement.execute("""
                INSERT INTO sales (invoice_number, total, sale_status, created_at)
                VALUES ('INV-001', 1000.0, 'COMPLETED', datetime('now'));
            """);
            statement.execute("""
                INSERT INTO purchases (invoice_number, total, purchase_status, created_at)
                VALUES ('PUR-001', 400.0, 'COMPLETED', datetime('now'));
            """);
            statement.execute("""
                INSERT INTO sale_items (sale_id, quantity, cost_price)
                VALUES (1, 1, 400.0);
            """);
            statement.execute("""
                INSERT INTO expenses (category, amount, expense_date)
                VALUES ('Rent', 100.0, date('now'));
            """);
            statement.execute("""
                INSERT INTO products (name, sku, selling_price, stock_quantity)
                VALUES ('Product A', 'SKU-A', 50.0, 3.0);
            """);
            statement.execute("""
                INSERT INTO products (name, sku, selling_price, stock_quantity)
                VALUES ('Product B', 'SKU-B', 100.0, 20.0);
            """);
        }

        DashboardSummary summary = repository.getSummary();

        assertEquals(1000.0, summary.getTodayRevenue());
        assertEquals(1, summary.getTodaySalesCount());
        assertEquals(2, summary.getTotalProductCount());
        assertEquals(1, summary.getLowStockCount());
        assertEquals(400.0, summary.getTodayPurchases());
        assertEquals(100.0, summary.getTodayExpenses());
        assertEquals(500.0, summary.getTodayProfit());
    }

    @Test
    void shouldFindRecentSalesWithCustomerName() throws Exception {
        try (Statement statement = keepAliveConnection.createStatement()) {
            statement.execute("""
                INSERT INTO customers (id, name) VALUES (1, 'Alice Smith');
            """);
            statement.execute("""
                INSERT INTO sales (customer_id, invoice_number, total, payment_status, sale_status, created_at)
                VALUES (1, 'INV-100', 250.0, 'PAID', 'COMPLETED', '2026-08-24 10:00:00');
            """);
            statement.execute("""
                INSERT INTO sales (customer_id, invoice_number, total, payment_status, sale_status, created_at)
                VALUES (null, 'INV-101', 150.0, 'PAID', 'COMPLETED', '2026-08-24 11:00:00');
            """);
        }

        List<RecentSale> recentSales = repository.findRecentSales(5);

        assertEquals(2, recentSales.size());
        assertEquals("INV-101", recentSales.get(0).getInvoiceNumber());
        assertEquals("Walk-in", recentSales.get(0).getCustomerName());

        assertEquals("INV-100", recentSales.get(1).getInvoiceNumber());
        assertEquals("Alice Smith", recentSales.get(1).getCustomerName());
    }

    @Test
    void shouldFindLowStockProducts() throws Exception {
        try (Statement statement = keepAliveConnection.createStatement()) {
            statement.execute("""
                INSERT INTO products (name, sku, selling_price, stock_quantity)
                VALUES ('Low Stock Item', 'SKU-LOW', 10.0, 2.0);
            """);
            statement.execute("""
                INSERT INTO products (name, sku, selling_price, stock_quantity)
                VALUES ('Healthy Stock Item', 'SKU-GOOD', 20.0, 50.0);
            """);
        }

        List<LowStockProduct> lowStock = repository.findLowStockProducts(10);

        assertEquals(1, lowStock.size());
        assertEquals("Low Stock Item", lowStock.get(0).getName());
        assertEquals(2.0, lowStock.get(0).getStockQuantity());
    }
}
