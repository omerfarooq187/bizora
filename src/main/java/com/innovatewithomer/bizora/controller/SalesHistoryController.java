package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.config.AppSettings;
import com.innovatewithomer.bizora.config.AppSettingsStore;
import com.innovatewithomer.bizora.model.Payment;
import com.innovatewithomer.bizora.model.PaymentMethod;
import com.innovatewithomer.bizora.model.PaymentStatus;
import com.innovatewithomer.bizora.model.Sale;
import com.innovatewithomer.bizora.model.SaleStatus;
import com.innovatewithomer.bizora.service.SaleService;
import com.innovatewithomer.bizora.service.PaymentService;
import com.innovatewithomer.bizora.service.ReceiptPrinterService;
import com.innovatewithomer.bizora.util.RefreshableView;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ProgressIndicator;
import javafx.concurrent.Task;

import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;


import com.innovatewithomer.bizora.model.Customer;
import com.innovatewithomer.bizora.service.CustomerService;

import java.util.HashMap;
import java.util.Map;

public class SalesHistoryController implements RefreshableView {


    // =========================================================
    // SERVICE
    // =========================================================

    private final SaleService saleService =
            AppContext.saleService();

    private final CustomerService customerService =
            AppContext.customerService();

    private final PaymentService paymentService =
            AppContext.paymentService();

    private final ReceiptPrinterService receiptPrinterService =
            new ReceiptPrinterService();


    // =========================================================
    // DATA
    // =========================================================

    private final ObservableList<Sale> sales =
            FXCollections.observableArrayList();

    private final ObservableList<Sale> filteredSales =
            FXCollections.observableArrayList();

    private final Map<Long, String> customerNameMap =
            new HashMap<>();


    // =========================================================
    // TABLE
    // =========================================================

    @FXML
    private TableView<Sale> salesTable;


    @FXML
    private TableColumn<Sale, String> invoiceColumn;


    @FXML
    private TableColumn<Sale, String> customerColumn;


    @FXML
    private TableColumn<Sale, String> dateColumn;


    @FXML
    private TableColumn<Sale, Double> subtotalColumn;


    @FXML
    private TableColumn<Sale, Double> discountColumn;


    @FXML
    private TableColumn<Sale, Double> taxColumn;


    @FXML
    private TableColumn<Sale, Double> totalColumn;


    @FXML
    private TableColumn<Sale, String> paymentStatusColumn;


    @FXML
    private TableColumn<Sale, String> saleStatusColumn;


    // =========================================================
    // FILTERS
    // =========================================================

    @FXML
    private TextField searchField;


    @FXML
    private ComboBox<String> paymentStatusComboBox;


    @FXML
    private ComboBox<String> saleStatusComboBox;


    @FXML
    private Label salesCountLabel;

    @FXML
    private ProgressIndicator receiptProgressIndicator;

    @FXML
    private Label receiptStatusLabel;


    // =========================================================
    // FORMATTING
    // =========================================================

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a"
            );


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    private void initialize() {

        configureTable();

        configureFilters();

        loadSales();
    }


    // =========================================================
    // TABLE CONFIGURATION
    // =========================================================

    private void configureTable() {

        invoiceColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                safeString(
                                        cell.getValue()
                                                .getInvoiceNumber()
                                )
                        )
        );


        customerColumn.setCellValueFactory(
                cell -> {

                    Sale sale =
                            cell.getValue();


                    String customer;


                    if (sale.getCustomerId() == null) {

                        customer = "Walk-in";

                    } else {

                        String name = customerNameMap.get(sale.getCustomerId());
                        customer = (name != null && !name.isBlank())
                                ? name
                                : "Customer #" + sale.getCustomerId();
                    }


                    return new SimpleStringProperty(
                            customer
                    );
                }
        );


        dateColumn.setCellValueFactory(
                cell -> {

                    Sale sale =
                            cell.getValue();


                    if (sale.getCreatedAt() == null) {

                        return new SimpleStringProperty(
                                ""
                        );
                    }


                    return new SimpleStringProperty(
                            sale.getCreatedAt()
                                    .format(
                                            DATE_FORMATTER
                                    )
                    );
                }
        );


        subtotalColumn.setCellValueFactory(
                cell ->
                        new SimpleDoubleProperty(
                                cell.getValue()
                                        .getSubtotal()
                        ).asObject()
        );


        discountColumn.setCellValueFactory(
                cell ->
                        new SimpleDoubleProperty(
                                cell.getValue()
                                        .getDiscount()
                        ).asObject()
        );


        taxColumn.setCellValueFactory(
                cell ->
                        new SimpleDoubleProperty(
                                cell.getValue()
                                        .getTax()
                        ).asObject()
        );


        totalColumn.setCellValueFactory(
                cell ->
                        new SimpleDoubleProperty(
                                cell.getValue()
                                        .getTotal()
                        ).asObject()
        );


        paymentStatusColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                formatPaymentStatus(
                                        cell.getValue()
                                                .getPaymentStatus()
                                )
                        )
        );


        saleStatusColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                formatSaleStatus(
                                        cell.getValue()
                                                .getSaleStatus()
                                )
                        )
        );


        salesTable.setItems(
                filteredSales
        );
    }


    // =========================================================
    // FILTER CONFIGURATION
    // =========================================================

    private void configureFilters() {

        paymentStatusComboBox.setItems(
                FXCollections.observableArrayList(
                        "All",
                        "UNPAID",
                        "PARTIALLY_PAID",
                        "PAID"
                )
        );


        paymentStatusComboBox.setValue(
                "All"
        );


        saleStatusComboBox.setItems(
                FXCollections.observableArrayList(
                        "All",
                        "COMPLETED",
                        "CANCELLED"
                )
        );


        saleStatusComboBox.setValue(
                "All"
        );


        paymentStatusComboBox.valueProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                applyFilters()
                );


        saleStatusComboBox.valueProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                applyFilters()
                );
    }


    // =========================================================
    // LOAD SALES
    // =========================================================

    private void loadSales() {

        try {

            customerNameMap.clear();
            try {
                for (Customer customer : customerService.getAllCustomers()) {
                    if (customer.getId() != null) {
                        customerNameMap.put(customer.getId(), customer.getName());
                    }
                }
            } catch (Exception ignored) {
            }

            List<Sale> result =
                    saleService.getAllSales();


            sales.setAll(
                    result
            );


            applyFilters();

        } catch (Exception e) {
            showError(
                    "Failed to load sales.\n\n"
                            + e.getMessage()
            );
        }
    }

    @Override
    public void refreshView() {
        loadSales();
    }


    // =========================================================
    // SEARCH
    // =========================================================

    @FXML
    private void handleSearch() {

        applyFilters();
    }


    @FXML
    private void handleFilterChanged() {

        applyFilters();
    }


    private void applyFilters() {

        String search =
                searchField.getText();


        String paymentStatus =
                paymentStatusComboBox.getValue();


        String saleStatus =
                saleStatusComboBox.getValue();


        String normalizedSearch =
                search == null
                        ? ""
                        : search.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        List<Sale> result =
                sales.stream()
                        .filter(
                                sale -> {

                                    if (normalizedSearch
                                            .isEmpty()) {

                                        return true;
                                    }


                                    String invoice =
                                            sale.getInvoiceNumber();


                                    if (invoice == null) {

                                        return false;
                                    }


                                    return invoice
                                            .toLowerCase(
                                                    Locale.ROOT
                                            )
                                            .contains(
                                                    normalizedSearch
                                            );
                                }
                        )
                        .filter(
                                sale -> {

                                    if (paymentStatus == null
                                            ||
                                            paymentStatus.equals(
                                                    "All"
                                            )) {

                                        return true;
                                    }


                                    PaymentStatus status =
                                            sale.getPaymentStatus();


                                    return status != null
                                            &&
                                            status.name()
                                                    .equals(
                                                            paymentStatus
                                                    );
                                }
                        )
                        .filter(
                                sale -> {

                                    if (saleStatus == null
                                            ||
                                            saleStatus.equals(
                                                    "All"
                                            )) {

                                        return true;
                                    }


                                    SaleStatus status =
                                            sale.getSaleStatus();


                                    return status != null
                                            &&
                                            status.name()
                                                    .equals(
                                                            saleStatus
                                                    );
                                }
                        )
                        .toList();


        filteredSales.setAll(
                result
        );


        updateSalesCount();
    }


    // =========================================================
    // CLEAR FILTERS
    // =========================================================

    @FXML
    private void handleClearFilters() {

        searchField.clear();


        paymentStatusComboBox.setValue(
                "All"
        );


        saleStatusComboBox.setValue(
                "All"
        );


        applyFilters();
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @FXML
    private void handleRefresh() {

        loadSales();
    }


    // =========================================================
    // VIEW DETAILS
    // =========================================================

    @FXML
    private void handleViewDetails() {

        Sale selectedSale =
                salesTable
                        .getSelectionModel()
                        .getSelectedItem();


        if (selectedSale == null) {

            showWarning(
                    "Please select a sale."
            );

            return;
        }


        try {

            Sale sale =
                    saleService.getSale(
                            selectedSale.getId()
                    );


            if (sale == null) {

                showError(
                        "The selected sale could not be found."
                );

                return;
            }


            showSaleDetails(
                    sale
            );

        } catch (Exception e) {
            showError(
                    "Failed to load sale details.\n\n"
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void handlePrintReceipt() {
        Sale selected = salesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a sale to print.");
            return;
        }
        if (selected.getSaleStatus() == SaleStatus.CANCELLED) {
            showWarning("Cancelled sales cannot be printed as active receipts.");
            return;
        }

        AppSettings settings = AppSettingsStore.load();
        try {
            receiptPrinterService.validateConfiguration(settings);
        } catch (IllegalArgumentException exception) {
            showWarning(exception.getMessage());
            return;
        }

        setReceiptBusy(true, "Printing " + safeString(selected.getInvoiceNumber()) + "…");
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                Sale sale = saleService.getSale(selected.getId());
                Map<Long, String> productNames = new HashMap<>();
                AppContext.productService().getAllProducts().forEach(
                        product -> productNames.put(product.getId(), product.getName()));
                List<Payment> payments = paymentService.getPaymentsForSale(sale.getId());
                double paid = payments.stream().mapToDouble(Payment::getAmount).sum();
                PaymentMethod method = payments.isEmpty()
                        ? null : payments.get(payments.size() - 1).getPaymentMethod();
                String customer = sale.getCustomerId() == null
                        ? "Walk-in Customer"
                        : customerNameMap.getOrDefault(sale.getCustomerId(), "Customer #" + sale.getCustomerId());
                receiptPrinterService.printReceipt(
                        settings, sale, productNames, customer, paid, method);
                return null;
            }
        };
        task.setOnSucceeded(event -> setReceiptBusy(false, "Receipt sent successfully."));
        task.setOnFailed(event -> {
            setReceiptBusy(false, "Receipt printing failed.");
            showError("Receipt could not be printed.\n\n" + rootMessage(task.getException()));
        });
        Thread worker = new Thread(task, "bizora-reprint-receipt");
        worker.setDaemon(true);
        worker.start();
    }

    private void setReceiptBusy(boolean busy, String status) {
        receiptProgressIndicator.setVisible(busy);
        receiptProgressIndicator.setManaged(busy);
        receiptStatusLabel.setText(status == null ? "" : status);
    }

    private String rootMessage(Throwable throwable) {
        if (throwable == null) return "Unknown printer error.";
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? "Unknown printer error." : current.getMessage();
    }


    private void showSaleDetails(
            Sale sale
    ) {

        StringBuilder message =
                new StringBuilder();


        message.append(
                        "Invoice: "
                )
                .append(
                        safeString(
                                sale.getInvoiceNumber()
                        )
                )
                .append(
                        "\n\n"
                );


        if (sale.getCreatedAt() != null) {

            message.append(
                            "Date: "
                    )
                    .append(
                            sale.getCreatedAt()
                                    .format(
                                            DATE_FORMATTER
                                    )
                    )
                    .append(
                            "\n"
                    );
        }


        message.append(
                        "Subtotal: Rs. "
                )
                .append(
                        formatAmount(
                                sale.getSubtotal()
                        )
                )
                .append(
                        "\n"
                );


        message.append(
                        "Discount: Rs. "
                )
                .append(
                        formatAmount(
                                sale.getDiscount()
                        )
                )
                .append(
                        "\n"
                );


        message.append(
                        "Tax: Rs. "
                )
                .append(
                        formatAmount(
                                sale.getTax()
                        )
                )
                .append(
                        "\n"
                );


        message.append(
                        "Total: Rs. "
                )
                .append(
                        formatAmount(
                                sale.getTotal()
                        )
                )
                .append(
                        "\n\n"
                );


        message.append(
                        "Payment Status: "
                )
                .append(
                        formatPaymentStatus(
                                sale.getPaymentStatus()
                        )
                )
                .append(
                        "\n"
                );


        message.append(
                        "Sale Status: "
                )
                .append(
                        formatSaleStatus(
                                sale.getSaleStatus()
                        )
                )
                .append(
                        "\n\n"
                );


        message.append(
                        "Items: "
                )
                .append(
                        sale.getItems() == null
                                ? 0
                                : sale.getItems().size()
                );


        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );


        alert.setTitle(
                "Sale Details"
        );


        alert.setHeaderText(
                safeString(
                        sale.getInvoiceNumber()
                )
        );


        alert.setContentText(
                message.toString()
        );


        alert.showAndWait();
    }


    // =========================================================
    // COUNT
    // =========================================================

    private void updateSalesCount() {

        int count =
                filteredSales.size();


        salesCountLabel.setText(
                count
                        + (
                        count == 1
                                ? " sale"
                                : " sales"
                )
        );
    }


    // =========================================================
    // STATUS FORMATTING
    // =========================================================

    private String formatPaymentStatus(
            PaymentStatus status
    ) {

        if (status == null) {

            return "Unknown";
        }


        return switch (status) {

            case UNPAID ->
                    "Unpaid";

            case PARTIALLY_PAID ->
                    "Partially Paid";

            case PAID ->
                    "Paid";
        };
    }


    private String formatSaleStatus(
            SaleStatus status
    ) {

        if (status == null) {

            return "Unknown";
        }


        return switch (status) {

            case COMPLETED ->
                    "Completed";

            case CANCELLED ->
                    "Cancelled";
        };
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private String safeString(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }


    private String formatAmount(
            double amount
    ) {

        return String.format(
                Locale.US,
                "%,.2f",
                amount
        );
    }


    // =========================================================
    // CANCEL SALE
    // =========================================================

    @FXML
    private void handleCancelSale() {

        Sale selectedSale =
                salesTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedSale == null) {
            showWarning("Please select a sale to cancel.");
            return;
        }

        if (selectedSale.getSaleStatus() == SaleStatus.CANCELLED) {
            showWarning("This sale is already cancelled.");
            return;
        }

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle("Cancel Sale");
        alert.setHeaderText("Cancel Invoice " + safeString(selectedSale.getInvoiceNumber()) + "?");
        alert.setContentText("This will cancel the sale and restore all items back into product inventory. This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    saleService.cancelSale(selectedSale.getId());
                    showSuccess("Sale " + safeString(selectedSale.getInvoiceNumber()) + " was cancelled successfully.\nStock has been restored.");
                    loadSales();
                } catch (Exception e) {
                    showError("Failed to cancel sale.\n\n" + e.getMessage());
                }
            }
        });
    }


    // =========================================================
    // ALERTS
    // =========================================================

    private void showWarning(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );


        alert.setTitle(
                "Sales"
        );


        alert.setHeaderText(
                null
        );


        alert.setContentText(
                message
        );


        alert.showAndWait();
    }


    private void showSuccess(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "Sales"
        );

        alert.setHeaderText(
                "Success"
        );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }


    private void showError(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );


        alert.setTitle(
                "Sales"
        );


        alert.setHeaderText(
                "Error"
        );


        alert.setContentText(
                message
        );


        alert.showAndWait();
    }
}
