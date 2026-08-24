package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.PaymentStatus;
import com.innovatewithomer.bizora.model.Sale;
import com.innovatewithomer.bizora.model.SaleStatus;
import com.innovatewithomer.bizora.service.SaleService;

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

import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class SalesHistoryController {


    // =========================================================
    // SERVICE
    // =========================================================

    private final SaleService saleService =
            AppContext.saleService();


    // =========================================================
    // DATA
    // =========================================================

    private final ObservableList<Sale> sales =
            FXCollections.observableArrayList();

    private final ObservableList<Sale> filteredSales =
            FXCollections.observableArrayList();


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


    // =========================================================
    // FORMATTING
    // =========================================================

    private static final DateTimeFormatter
            DATE_FORMATTER =
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
                                cell.getValue()
                                        .getInvoiceNumber()
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

                        customer =
                                "Customer #"
                                        + sale.getCustomerId();
                    }

                    return new SimpleStringProperty(
                            customer
                    );
                }
        );


        dateColumn.setCellValueFactory(
                cell -> {

                    if (cell.getValue()
                            .getCreatedAt() == null) {

                        return new SimpleStringProperty(
                                ""
                        );
                    }

                    return new SimpleStringProperty(
                            cell.getValue()
                                    .getCreatedAt()
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
    }


    // =========================================================
    // LOAD SALES
    // =========================================================

    private void loadSales() {

        try {

            List<Sale> result =
                    saleService.getAllSales();

            sales.setAll(
                    result
            );

            applyFilters();

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Failed to load sales.\n\n"
                            + e.getMessage()
            );
        }
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
                paymentStatusComboBox
                        .getValue();

        String saleStatus =
                saleStatusComboBox
                        .getValue();


        String normalizedSearch =
                search == null
                        ? ""
                        : search.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        List<Sale> result =
                sales.stream()
                        .filter(sale -> {

                            if (normalizedSearch
                                    .isEmpty()) {

                                return true;
                            }

                            String invoice =
                                    sale.getInvoiceNumber();

                            return invoice != null
                                    &&
                                    invoice.toLowerCase(
                                            Locale.ROOT
                                    ).contains(
                                            normalizedSearch
                                    );
                        })
                        .filter(sale -> {

                            if (paymentStatus == null
                                    ||
                                    paymentStatus.equals(
                                            "All"
                                    )) {

                                return true;
                            }

                            return sale.getPaymentStatus()
                                    .name()
                                    .equals(
                                            paymentStatus
                                    );
                        })
                        .filter(sale -> {

                            if (saleStatus == null
                                    ||
                                    saleStatus.equals(
                                            "All"
                                    )) {

                                return true;
                            }

                            return sale.getSaleStatus()
                                    .name()
                                    .equals(
                                            saleStatus
                                    );
                        })
                        .collect(
                                Collectors.toList()
                        );


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

        /*
         * For now we simply demonstrate that
         * the correct sale has been selected.
         *
         * We will replace this with the
         * Sale Details dialog in the next step.
         */
        Sale sale =
                saleService.getSale(
                        selectedSale.getId()
                );

        showSaleDetails(
                sale
        );
    }


    private void showSaleDetails(
            Sale sale
    ) {

        StringBuilder message =
                new StringBuilder();

        message.append(
                "Invoice: "
        ).append(
                sale.getInvoiceNumber()
        ).append(
                "\n\n"
        );

        message.append(
                "Date: "
        ).append(
                sale.getCreatedAt()
                        .format(
                                DATE_FORMATTER
                        )
        ).append(
                "\n"
        );

        message.append(
                "Subtotal: Rs. "
        ).append(
                sale.getSubtotal()
        ).append(
                "\n"
        );

        message.append(
                "Discount: Rs. "
        ).append(
                sale.getDiscount()
        ).append(
                "\n"
        );

        message.append(
                "Tax: Rs. "
        ).append(
                sale.getTax()
        ).append(
                "\n"
        );

        message.append(
                "Total: Rs. "
        ).append(
                sale.getTotal()
        ).append(
                "\n\n"
        );

        message.append(
                "Items: "
        ).append(
                sale.getItems().size()
        );


        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "Sale Details"
        );

        alert.setHeaderText(
                sale.getInvoiceNumber()
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