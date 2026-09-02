package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.infrastructure.migration.MigrationRunner;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ReportsViewTest {

    private static Connection keepAliveConnection;

    @BeforeAll
    static void initJavaFX() throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase();
        boolean hasGraphicalEnvironment = os.contains("win")
                || os.contains("mac")
                || System.getenv("DISPLAY") != null
                || System.getenv("WAYLAND_DISPLAY") != null;
        assumeTrue(
                hasGraphicalEnvironment,
                "JavaFX view tests require a graphical environment."
        );
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            // Platform already started
            latch.countDown();
        } catch (UnsupportedOperationException e) {
            assumeTrue(
                    false,
                    "JavaFX could not connect to the configured graphical display."
            );
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        DatabaseManager.setJdbcUrl(
                "jdbc:sqlite:file:fxml_test_" + System.nanoTime()
                        + "?mode=memory&cache=shared"
        );
        keepAliveConnection = DatabaseManager.getConnection();
        new MigrationRunner().run(keepAliveConnection);
    }

    @AfterAll
    static void closeDatabase() throws Exception {
        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }
        DatabaseManager.resetJdbcUrl();
    }

    @Test
    void testLoadReportsView() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/innovatewithomer/bizora/fxml/reports-view.fxml")
        );
        Object root = loader.load();
        assertNotNull(root);
        ReportsController controller = loader.getController();
        assertNotNull(controller);
    }

    @Test
    void generateButtonsShouldRefreshCostOfGoodsAndExpenses() throws Exception {
        LocalDate today = LocalDate.now();

        try (Statement statement = keepAliveConnection.createStatement()) {
            statement.executeUpdate("DELETE FROM sale_items WHERE sale_id = 900001");
            statement.executeUpdate("DELETE FROM sales WHERE id = 900001");
            statement.executeUpdate("DELETE FROM expenses WHERE id = 900001");
            statement.executeUpdate("DELETE FROM products WHERE id = 900001");
            statement.executeUpdate("""
                    INSERT INTO products
                        (id, name, sku, selling_price, purchase_price, stock_quantity, created_at)
                    VALUES
                        (900001, 'Report fixture', 'REPORT-FIXTURE', 200, 120, 1, '%s')
                    """.formatted(today.atStartOfDay()));
            statement.executeUpdate("""
                    INSERT INTO sales
                        (id, invoice_number, subtotal, discount, tax, total,
                         payment_status, sale_status, created_at)
                    VALUES
                        (900001, 'REPORT-FIXTURE', 200, 0, 0, 200,
                         'PAID', 'COMPLETED', '%s')
                    """.formatted(today.atStartOfDay()));
            statement.executeUpdate("""
                    INSERT INTO sale_items
                        (sale_id, product_id, quantity, unit_price, cost_price, discount, subtotal)
                    VALUES
                        (900001, 900001, 1, 200, 120, 0, 200)
                    """);
            statement.executeUpdate("""
                    INSERT INTO expenses
                        (id, category, description, amount, expense_date, created_at)
                    VALUES
                        (900001, 'Testing', 'Report fixture', 25, '%s', '%s')
                    """.formatted(today, today.atStartOfDay()));
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/innovatewithomer/bizora/fxml/reports-view.fxml")
            );
            loader.load();

            Label costLabel = (Label) loader.getNamespace().get("purchaseCostLabel");
            Label expenseLabel = (Label) loader.getNamespace().get("expensesTotalLabel");
            Label statusLabel = (Label) loader.getNamespace().get("reportStatusLabel");
            Button generateButton = (Button) loader.getNamespace().get("generateButton");
            Button generateReportButton = (Button) loader.getNamespace().get("generateReportButton");

            assertTrue(costLabel.getText().contains("120.00"));
            assertTrue(expenseLabel.getText().contains("25.00"));

            costLabel.setText("stale");
            generateButton.fire();
            assertTrue(costLabel.getText().contains("120.00"));

            expenseLabel.setText("stale");
            generateReportButton.fire();
            assertTrue(expenseLabel.getText().contains("25.00"));
            assertTrue(statusLabel.getText().startsWith("Generated for "));
        } finally {
            try (Statement statement = keepAliveConnection.createStatement()) {
                statement.executeUpdate("DELETE FROM sale_items WHERE sale_id = 900001");
                statement.executeUpdate("DELETE FROM sales WHERE id = 900001");
                statement.executeUpdate("DELETE FROM expenses WHERE id = 900001");
                statement.executeUpdate("DELETE FROM products WHERE id = 900001");
            }
        }
    }

    @Test
    void mainLayoutShouldCollapseAndExpandSidebarAtBreakpoints() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/innovatewithomer/bizora/fxml/main-view.fxml")
        );
        BorderPane root = loader.load();
        VBox sidebar = (VBox) loader.getNamespace().get("sidebar");
        Button dashboardButton = (Button) loader.getNamespace().get("dashboardButton");

        root.resize(1000, 700);
        assertEquals(82, sidebar.getPrefWidth());
        assertEquals(ContentDisplay.GRAPHIC_ONLY, dashboardButton.getContentDisplay());

        root.resize(1400, 800);
        assertEquals(238, sidebar.getPrefWidth());
        assertEquals(ContentDisplay.LEFT, dashboardButton.getContentDisplay());
    }

    @Test
    void reportMetricsShouldReflowAtResponsiveBreakpoints() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/innovatewithomer/bizora/fxml/reports-view.fxml")
        );
        loader.load();
        GridPane metricsGrid = (GridPane) loader.getNamespace().get("metricsGrid");

        metricsGrid.resize(900, 400);
        assertEquals(2, metricsGrid.getColumnConstraints().size());
        assertEquals(1, GridPane.getColumnIndex(metricsGrid.getChildren().get(1)));
        assertEquals(1, GridPane.getRowIndex(metricsGrid.getChildren().get(2)));

        metricsGrid.resize(500, 700);
        assertEquals(1, metricsGrid.getColumnConstraints().size());
        assertEquals(0, GridPane.getColumnIndex(metricsGrid.getChildren().get(3)));
        assertEquals(3, GridPane.getRowIndex(metricsGrid.getChildren().get(3)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "add-customer-dialog.fxml",
            "add-expense-dialog.fxml",
            "add-product-dialog.fxml",
            "add-supplier-dialog.fxml",
            "adjust-stock-view.fxml",
            "customers-view.fxml",
            "dashboard-view.fxml",
            "expenses-view.fxml",
            "inventory-view.fxml",
            "main-view.fxml",
            "new-sale-view.fxml",
            "products-view.fxml",
            "reports-view.fxml",
            "sales-history-view.fxml",
            "sales-view.fxml",
            "settings-view.fxml",
            "suppliers-view.fxml"
    })
    void testLoadAllFxmlViews(String fxmlFile) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/innovatewithomer/bizora/fxml/" + fxmlFile)
        );
        Object root = loader.load();
        assertNotNull(root, "Failed to load " + fxmlFile);
    }
}
