package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.report.ExpenseReport;
import com.innovatewithomer.bizora.model.report.FinancialSummary;
import com.innovatewithomer.bizora.model.SalesReport;
import com.innovatewithomer.bizora.service.ExpenseReportService;
import com.innovatewithomer.bizora.service.FinancialSummaryService;
import com.innovatewithomer.bizora.service.SalesReportService;
import com.innovatewithomer.bizora.util.CurrencyFormatter;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsController {

    private final SalesReportService     salesReportService     = AppContext.salesReportService();
    private final ExpenseReportService   expenseReportService   = AppContext.expenseReportService();
    private final FinancialSummaryService financialSummaryService = AppContext.financialSummaryService();

    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;

    // Metric labels
    @FXML private Label salesRevenueLabel;
    @FXML private Label salesCountLabel;
    @FXML private Label purchaseCostLabel;
    @FXML private Label purchaseCountLabel;
    @FXML private Label expensesTotalLabel;
    @FXML private Label expenseCountLabel;
    @FXML private Label netProfitLabel;
    @FXML private Label grossProfitLabel;
    @FXML private Label reportStatusLabel;
    @FXML private GridPane metricsGrid;

    // Category breakdown table
    @FXML private TableView<Map.Entry<String, Double>> categoryTable;
    @FXML private TableColumn<Map.Entry<String, Double>, String> catNameColumn;
    @FXML private TableColumn<Map.Entry<String, Double>, Double> catAmountColumn;
    @FXML private TableColumn<Map.Entry<String, Double>, String> catShareColumn;

    @FXML
    private void initialize() {
        // Default: this calendar month
        YearMonth now = YearMonth.now();
        fromDate.setValue(now.atDay(1));
        toDate.setValue(now.atEndOfMonth());

        configureCategoryTable();
        configureResponsiveMetrics();
        handleGenerateReport();
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

    private void configureCategoryTable() {
        catNameColumn.setCellValueFactory(
                entry -> new SimpleStringProperty(
                        entry.getValue() != null && entry.getValue().getKey() != null
                                ? entry.getValue().getKey()
                                : ""
                )
        );
        catAmountColumn.setCellValueFactory(
                entry -> new SimpleDoubleProperty(
                        entry.getValue() != null && entry.getValue().getValue() != null
                                ? entry.getValue().getValue()
                                : 0.0
                ).asObject()
        );
        catAmountColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : CurrencyFormatter.format(v));
            }
        });
        catShareColumn.setCellValueFactory(entry -> {
            if (entry.getValue() == null || entry.getValue().getValue() == null) {
                return new SimpleStringProperty("");
            }
            double total = categoryTable.getItems() != null
                    ? categoryTable.getItems().stream()
                        .filter(java.util.Objects::nonNull)
                        .mapToDouble(e -> e.getValue() != null ? e.getValue() : 0.0).sum()
                    : 0.0;
            double share = total == 0 ? 0 : (entry.getValue().getValue() / total) * 100.0;
            return new SimpleStringProperty(String.format(Locale.US, "%.1f%%", share));
        });
    }

    @FXML
    private void handleGenerateReport() {
        LocalDate from = fromDate.getValue();
        LocalDate to   = toDate.getValue();

        if (from == null || to == null || from.isAfter(to)) {
            showStatus("Select a valid date range.", false);
            return;
        }

        try {
            SalesReport sales = salesReportService.getSalesReport(from, to);
            ExpenseReport expenses = expenseReportService.getExpenseReport(from, to);
            FinancialSummary fin = financialSummaryService.getFinancialSummary(from, to);

            salesRevenueLabel.setText(CurrencyFormatter.format(sales.getTotalRevenue()));
            salesCountLabel.setText(sales.getTotalSales() + " transactions");
            purchaseCostLabel.setText(CurrencyFormatter.format(fin.getTotalCostOfGoodsSold()));
            purchaseCountLabel.setText("Cost of " + sales.getTotalSales() + " completed sales");
            expensesTotalLabel.setText(CurrencyFormatter.format(expenses.getTotalExpenses()));
            expenseCountLabel.setText(expenses.getExpenseCount() + " entries");
            netProfitLabel.setText(CurrencyFormatter.format(fin.getNetProfit()));
            grossProfitLabel.setText("Gross: " + CurrencyFormatter.format(fin.getGrossProfit()));
            colorProfitLabel(netProfitLabel, fin.getNetProfit());

            // Category breakdown
            Map<String, Double> catMap = expenses.getExpensesByCategory();
            List<Map.Entry<String, Double>> entries = new ArrayList<>(catMap != null ? catMap.entrySet() : Collections.emptyList());
            entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            ObservableList<Map.Entry<String, Double>> catItems = FXCollections.observableArrayList(entries);
            categoryTable.setItems(catItems);
            String expenseNote = expenses.getExpenseCount() == 0
                    ? " No expenses are recorded in this period."
                    : " " + expenses.getExpenseCount() + " expense entries included.";
            showStatus(
                    "Generated for " + from + " to " + to + "." + expenseNote,
                    true
            );

        } catch (Exception e) {
            clearReport();
            String detail = e.getMessage() == null || e.getMessage().isBlank()
                    ? "Please try again."
                    : e.getMessage();
            showStatus("Unable to generate the report: " + detail, false);
        }
    }

    @FXML
    private void handleThisMonth() {
        YearMonth now = YearMonth.now();
        fromDate.setValue(now.atDay(1));
        toDate.setValue(now.atEndOfMonth());
        handleGenerateReport();
    }

    @FXML
    private void handleLast30Days() {
        toDate.setValue(LocalDate.now());
        fromDate.setValue(LocalDate.now().minusDays(29));
        handleGenerateReport();
    }

    @FXML
    private void handleThisYear() {
        int year = LocalDate.now().getYear();
        fromDate.setValue(LocalDate.of(year, 1, 1));
        toDate.setValue(LocalDate.of(year, 12, 31));
        handleGenerateReport();
    }

    private void colorProfitLabel(Label label, double value) {
        label.setStyle(value >= 0
                ? "-fx-font-size:24px; -fx-font-weight:bold; -fx-text-fill:#10b981;"
                : "-fx-font-size:24px; -fx-font-weight:bold; -fx-text-fill:#ef4444;");
    }

    private void clearReport() {
        String zero = CurrencyFormatter.format(0);
        salesRevenueLabel.setText(zero);
        salesCountLabel.setText("0 transactions");
        purchaseCostLabel.setText(zero);
        purchaseCountLabel.setText("Cost of 0 completed sales");
        expensesTotalLabel.setText(zero);
        expenseCountLabel.setText("0 entries");
        netProfitLabel.setText(zero);
        grossProfitLabel.setText("Gross: " + zero);
        categoryTable.getItems().clear();
    }

    private void showStatus(String message, boolean success) {
        reportStatusLabel.setText(message);
        reportStatusLabel.setStyle(success
                ? "-fx-text-fill:#10b981; -fx-font-weight:bold;"
                : "-fx-text-fill:#ef4444; -fx-font-weight:bold;");
    }
}
