package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.dashboard.DashboardSummary;
import com.innovatewithomer.bizora.model.dashboard.LowStockProduct;
import com.innovatewithomer.bizora.model.dashboard.RecentSale;
import com.innovatewithomer.bizora.service.DashboardService;
import com.innovatewithomer.bizora.util.CurrencyFormatter;
import com.innovatewithomer.bizora.util.RefreshableView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController implements RefreshableView {

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

    @FXML
    private GridPane metricsGrid;


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
    private TableColumn<RecentSale, String> customerColumn;

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


    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a"
            );


    @FXML
    private void initialize() {

        configureRecentSalesTable();

        configureLowStockTable();

        configureResponsiveMetrics();

        loadDashboard();
    }

    private void configureResponsiveMetrics() {
        metricsGrid.widthProperty().addListener(
                (observable, oldWidth, newWidth) ->
                        updateMetricLayout(newWidth.doubleValue())
        );
        javafx.application.Platform.runLater(
                () -> updateMetricLayout(metricsGrid.getWidth())
        );
    }

    private void updateMetricLayout(double width) {
        int columns = width < 640 ? 1 : width < 1080 ? 2 : 4;

        metricsGrid.getColumnConstraints().clear();
        for (int index = 0; index < columns; index++) {
            ColumnConstraints constraint = new ColumnConstraints();
            constraint.setPercentWidth(100.0 / columns);
            constraint.setHgrow(Priority.ALWAYS);
            constraint.setFillWidth(true);
            metricsGrid.getColumnConstraints().add(constraint);
        }

        List<Node> cards = List.copyOf(metricsGrid.getChildren());
        for (int index = 0; index < cards.size(); index++) {
            Node card = cards.get(index);
            GridPane.setColumnIndex(card, index % columns);
            GridPane.setRowIndex(card, index / columns);
            GridPane.setHgrow(card, Priority.ALWAYS);
            if (card instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
            }
        }
    }


    // =========================================
    // INITIALIZATION
    // =========================================

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

        if (customerColumn != null) {
            customerColumn.setCellValueFactory(
                    new PropertyValueFactory<>(
                            "customerName"
                    )
            );
        }

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
        try {
            loadSummary();
            loadRecentSales();
            loadLowStockProducts();
        } catch (RuntimeException e) {
            recentSales.clear();
            lowStockProducts.clear();
            showError(
                    "Dashboard data could not be loaded.\n\n"
                            + e.getMessage()
            );
        }
    }

    @Override
    public void refreshView() {
        loadDashboard();
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

        if (todayPurchasesLabel != null) {
            todayPurchasesLabel.setText(
                    formatCurrency(
                            summary.getTodayPurchases()
                    )
            );
        }

        if (todayExpensesLabel != null) {
            todayExpensesLabel.setText(
                    formatCurrency(
                            summary.getTodayExpenses()
                    )
            );
        }

        if (todayProfitLabel != null) {
            todayProfitLabel.setText(
                    formatCurrency(
                            summary.getTodayProfit()
                    )
            );
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

        return CurrencyFormatter.format(amount);
    }


    // =========================================
    // QUICK ACTIONS
    // =========================================

    @FXML
    private void handleNewSale() {

        if (MainController.getInstance() != null) {
            MainController.getInstance().showSales();
        }
    }


    @FXML
    private void handleAddProduct() {

        try {
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(
                            getClass().getResource(
                                    "/com/innovatewithomer/bizora/fxml/add-product-dialog.fxml"
                            )
                    );

            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Add Product");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadDashboard();

        } catch (java.io.IOException e) {
            showError(
                    "The product form could not be opened.\n\n"
                            + e.getMessage()
            );
        }
    }


    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Dashboard");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleStockAdjustment() {

        if (MainController.getInstance() != null) {
            MainController.getInstance().showInventory();
        }
    }

    @FXML
    private void handleReceiveStock() {

        if (MainController.getInstance() != null) {
            MainController.getInstance().showPurchases();
        }
    }


    @FXML
    private void handleAddExpense() {

        if (MainController.getInstance() != null) {
            MainController.getInstance().showExpenses();
        }
    }


    @FXML
    private void handleViewSales() {

        if (MainController.getInstance() != null) {
            MainController.getInstance().showSales();
        }
    }
}
