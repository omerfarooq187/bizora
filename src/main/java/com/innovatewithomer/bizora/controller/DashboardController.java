package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.dashboard.DashboardSummary;
import com.innovatewithomer.bizora.model.dashboard.LowStockProduct;
import com.innovatewithomer.bizora.model.dashboard.RecentSale;
import com.innovatewithomer.bizora.service.DashboardService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    private final DashboardService dashboardService =
            AppContext.dashboardService();

    private final ObservableList<RecentSale> recentSales =
            FXCollections.observableArrayList();

    private final ObservableList<LowStockProduct> lowStockProducts =
            FXCollections.observableArrayList();


    // =========================================
    // SUMMARY
    // =========================================

    @FXML
    private Label todaySalesLabel;

    @FXML
    private Label todayPurchasesLabel;

    @FXML
    private Label todayExpensesLabel;

    @FXML
    private Label todayProfitLabel;

    @FXML
    private Label totalProductsLabel;

    @FXML
    private Label lowStockLabel;

    @FXML
    private Label todayTransactionsLabel;


    // =========================================
    // RECENT SALES TABLE
    // =========================================

    @FXML
    private TableView<RecentSale> recentSalesTable;

    @FXML
    private TableColumn<RecentSale, String> invoiceColumn;

    @FXML
    private TableColumn<RecentSale, LocalDateTime> dateColumn;

    @FXML
    private TableColumn<RecentSale, Double> amountColumn;

    @FXML
    private TableColumn<RecentSale, String> paymentStatusColumn;


    // =========================================
    // LOW STOCK TABLE
    // =========================================

    @FXML
    private TableView<LowStockProduct> lowStockTable;

    @FXML
    private TableColumn<LowStockProduct, String> lowStockNameColumn;

    @FXML
    private TableColumn<LowStockProduct, String> lowStockSkuColumn;

    @FXML
    private TableColumn<LowStockProduct, Double> lowStockQuantityColumn;


    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getNumberInstance(
                    Locale.US
            );

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a"
            );


    @FXML
    private void initialize() {

        configureCurrencyFormat();

        configureRecentSalesTable();

        configureLowStockTable();

        loadDashboard();
    }


    // =========================================
    // INITIALIZATION
    // =========================================

    private void configureCurrencyFormat() {

        CURRENCY_FORMAT.setMinimumFractionDigits(2);
        CURRENCY_FORMAT.setMaximumFractionDigits(2);
    }


    private void configureRecentSalesTable() {

        invoiceColumn.setCellValueFactory(
                new PropertyValueFactory<>(
                        "invoiceNumber"
                )
        );

        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>(
                        "createdAt"
                )
        );

        amountColumn.setCellValueFactory(
                new PropertyValueFactory<>(
                        "total"
                )
        );

        paymentStatusColumn.setCellValueFactory(
                new PropertyValueFactory<>(
                        "paymentStatus"
                )
        );

        recentSalesTable.setItems(
                recentSales
        );
    }


    private void configureLowStockTable() {

        if (lowStockTable == null) {
            return;
        }

        lowStockNameColumn.setCellValueFactory(
                new PropertyValueFactory<>(
                        "name"
                )
        );

        lowStockSkuColumn.setCellValueFactory(
                new PropertyValueFactory<>(
                        "sku"
                )
        );

        lowStockQuantityColumn.setCellValueFactory(
                new PropertyValueFactory<>(
                        "stockQuantity"
                )
        );

        lowStockTable.setItems(
                lowStockProducts
        );
    }


    // =========================================
    // LOAD DASHBOARD
    // =========================================

    private void loadDashboard() {

        loadSummary();

        loadRecentSales();

        loadLowStockProducts();
    }


    private void loadSummary() {

        DashboardSummary summary =
                dashboardService.getSummary();

        todaySalesLabel.setText(
                formatCurrency(
                        summary.getTodayRevenue()
                )
        );

        todayTransactionsLabel.setText(
                String.valueOf(
                        summary.getTodaySalesCount()
                )
        );

        totalProductsLabel.setText(
                String.valueOf(
                        summary.getTotalProductCount()
                )
        );

        lowStockLabel.setText(
                String.valueOf(
                        summary.getLowStockCount()
                )
        );


        /*
         * These metrics are not yet provided
         * by DashboardSummary.
         *
         * Keep them unavailable instead of
         * displaying misleading values.
         */

        if (todayPurchasesLabel != null) {
            todayPurchasesLabel.setText("—");
        }

        if (todayExpensesLabel != null) {
            todayExpensesLabel.setText("—");
        }

        if (todayProfitLabel != null) {
            todayProfitLabel.setText("—");
        }
    }


    private void loadRecentSales() {

        List<RecentSale> sales =
                dashboardService.getRecentSales(10);

        recentSales.setAll(sales);
    }


    private void loadLowStockProducts() {

        List<LowStockProduct> products =
                dashboardService.getLowStockProducts(5);

        lowStockProducts.setAll(products);
    }


    // =========================================
    // FORMATTING
    // =========================================

    private String formatCurrency(
            double amount
    ) {

        return "Rs. " +
                CURRENCY_FORMAT.format(amount);
    }


    // =========================================
    // QUICK ACTIONS
    // =========================================

    @FXML
    private void handleNewSale() {

        /*
         * Sales UI will be implemented later.
         *
         * We intentionally do not create
         * fake navigation here yet.
         */
    }


    @FXML
    private void handleAddProduct() {

        /*
         * ProductController currently owns
         * the Add Product dialog.
         *
         * Dashboard navigation can be wired
         * here after the MainController/ViewManager
         * navigation is finalized.
         */
    }


    @FXML
    private void handleStockAdjustment() {

        /*
         * Inventory UI will be implemented later.
         */
    }


    @FXML
    private void handleAddExpense() {

        /*
         * Expense UI will be implemented later.
         */
    }


    @FXML
    private void handleViewSales() {

        /*
         * Sales UI/navigation will be implemented later.
         */
    }
}