package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.report.ExpenseReport;
import com.innovatewithomer.bizora.model.report.FinancialSummary;
import com.innovatewithomer.bizora.model.SalesReport;
import com.innovatewithomer.bizora.service.ExpenseReportService;
import com.innovatewithomer.bizora.service.FinancialSummaryService;
import com.innovatewithomer.bizora.service.ReportPdfService;
import com.innovatewithomer.bizora.service.SalesReportService;
import com.innovatewithomer.bizora.util.CurrencyFormatter;
import com.innovatewithomer.bizora.util.RefreshableView;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.concurrent.Task;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ReportsController implements RefreshableView {

    private static final ExecutorService REPORT_EXECUTOR = Executors.newSingleThreadExecutor(
            new ReportThreadFactory()
    );

    private final SalesReportService     salesReportService     = AppContext.salesReportService();
    private final ExpenseReportService   expenseReportService   = AppContext.expenseReportService();
    private final FinancialSummaryService financialSummaryService = AppContext.financialSummaryService();
    private final ReportPdfService reportPdfService = new ReportPdfService();

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
    @FXML private Button generateButton;
    @FXML private Button generateReportButton;
    @FXML private ProgressIndicator reportProgressIndicator;

    private long reportRequestVersion;
    private Task<ReportSnapshot> activeReportTask;
    private int metricColumnCount;

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
        // Let JavaFX paint the screen before starting database aggregation.
        javafx.application.Platform.runLater(this::handleGenerateReport);
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
        if (columns == metricColumnCount) return;
        metricColumnCount = columns;

        List<ColumnConstraints> constraints = new ArrayList<>(columns);
        for (int index = 0; index < columns; index++) {
            ColumnConstraints constraint = new ColumnConstraints();
            constraint.setPercentWidth(100.0 / columns);
            constraint.setHgrow(Priority.ALWAYS);
            constraint.setFillWidth(true);
            constraints.add(constraint);
        }
        metricsGrid.getColumnConstraints().setAll(constraints);

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

        long requestVersion = ++reportRequestVersion;
        if (activeReportTask != null) activeReportTask.cancel();

        setReportBusy(true);
        showStatus("Loading report…", true);
        Task<ReportSnapshot> task = new Task<>() {
            @Override protected ReportSnapshot call() {
                SalesReport sales = salesReportService.getSalesReport(from, to);
                ExpenseReport expenses = expenseReportService.getExpenseReport(from, to);
                FinancialSummary financial = financialSummaryService.getFinancialSummary(from, to);
                return new ReportSnapshot(from, to, sales, expenses, financial);
            }
        };
        activeReportTask = task;
        task.setOnSucceeded(event -> {
            if (requestVersion != reportRequestVersion) return;
            setReportBusy(false);
            applyReport(task.getValue());
        });
        task.setOnFailed(event -> {
            if (requestVersion != reportRequestVersion) return;
            setReportBusy(false);
            clearReport();
            showStatus("Unable to generate the report: " + rootMessage(task.getException()), false);
        });
        task.setOnCancelled(event -> {
            if (requestVersion == reportRequestVersion) setReportBusy(false);
        });
        REPORT_EXECUTOR.execute(task);
    }

    private void applyReport(ReportSnapshot report) {
        SalesReport sales = report.sales();
        ExpenseReport expenses = report.expenses();
        FinancialSummary fin = report.financial();

        salesRevenueLabel.setText(CurrencyFormatter.format(sales.getTotalRevenue()));
        salesCountLabel.setText(sales.getTotalSales() + " transactions");
        purchaseCostLabel.setText(CurrencyFormatter.format(fin.getTotalCostOfGoodsSold()));
        purchaseCountLabel.setText("Cost of " + sales.getTotalSales() + " completed sales");
        expensesTotalLabel.setText(CurrencyFormatter.format(expenses.getTotalExpenses()));
        expenseCountLabel.setText(expenses.getExpenseCount() + " entries");
        netProfitLabel.setText(CurrencyFormatter.format(fin.getNetProfit()));
        grossProfitLabel.setText("Gross: " + CurrencyFormatter.format(fin.getGrossProfit()));
        colorProfitLabel(netProfitLabel, fin.getNetProfit());

        Map<String, Double> catMap = expenses.getExpensesByCategory();
        List<Map.Entry<String, Double>> entries = new ArrayList<>(
                catMap != null ? catMap.entrySet() : Collections.emptyList()
        );
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        ObservableList<Map.Entry<String, Double>> catItems = FXCollections.observableArrayList(entries);
        categoryTable.setItems(catItems);
        String expenseNote = expenses.getExpenseCount() == 0
                ? " No expenses are recorded in this period."
                : " " + expenses.getExpenseCount() + " expense entries included.";
        showStatus(
                "Generated for " + report.from() + " to " + report.to() + "." + expenseNote,
                true
        );
    }

    @FXML
    private void handleExportPdf() {
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();
        if (from == null || to == null || from.isAfter(to)) {
            showStatus("Select a valid date range before exporting.", false);
            return;
        }

        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Bizora PDF Report");
            chooser.setInitialFileName("Bizora-report-" + from + "-to-" + to + ".pdf");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF document", "*.pdf"));
            java.io.File selected = chooser.showSaveDialog(reportStatusLabel.getScene().getWindow());
            if (selected == null) {
                showStatus("PDF export cancelled.", false);
                return;
            }

            setReportBusy(true);
            showStatus("Creating PDF report…", true);
            Task<java.nio.file.Path> task = new Task<>() {
                @Override protected java.nio.file.Path call() {
                    SalesReport sales = salesReportService.getSalesReport(from, to);
                    ExpenseReport expenses = expenseReportService.getExpenseReport(from, to);
                    FinancialSummary financial = financialSummaryService.getFinancialSummary(from, to);
                    return reportPdfService.export(
                            selected.toPath(), from, to, sales, expenses, financial);
                }
            };
            task.setOnSucceeded(event -> {
                setReportBusy(false);
                showStatus("PDF report saved: " + task.getValue(), true);
            });
            task.setOnFailed(event -> {
                setReportBusy(false);
                showStatus("Unable to export PDF: " + rootMessage(task.getException()), false);
            });
            Thread worker = new Thread(task, "bizora-pdf-report");
            worker.setDaemon(true);
            worker.start();
        } catch (Exception exception) {
            setReportBusy(false);
            String detail = rootMessage(exception);
            showStatus("Unable to export PDF: " + detail, false);
        }
    }

    @FXML
    private void handleToday() {
        LocalDate today = LocalDate.now();
        fromDate.setValue(today);
        toDate.setValue(today);
        handleGenerateReport();
    }

    @FXML
    private void handleThisWeek() {
        LocalDate today = LocalDate.now();
        fromDate.setValue(today.with(DayOfWeek.MONDAY));
        toDate.setValue(today.with(DayOfWeek.SUNDAY));
        handleGenerateReport();
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

    @Override
    public void refreshView() {
        handleGenerateReport();
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? "Please try again." : current.getMessage();
    }

    private void setReportBusy(boolean busy) {
        reportProgressIndicator.setVisible(busy);
        reportProgressIndicator.setManaged(busy);
        generateButton.setDisable(busy);
        generateReportButton.setDisable(busy);
    }

    private record ReportSnapshot(
            LocalDate from,
            LocalDate to,
            SalesReport sales,
            ExpenseReport expenses,
            FinancialSummary financial
    ) { }

    private static final class ReportThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "bizora-report-loader");
            thread.setDaemon(true);
            return thread;
        }
    }
}
