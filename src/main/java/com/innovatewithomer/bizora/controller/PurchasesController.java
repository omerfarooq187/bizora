package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.PaymentStatus;
import com.innovatewithomer.bizora.model.Purchase;
import com.innovatewithomer.bizora.model.Supplier;
import com.innovatewithomer.bizora.service.PurchaseService;
import com.innovatewithomer.bizora.service.SupplierService;
import com.innovatewithomer.bizora.util.CurrencyFormatter;
import com.innovatewithomer.bizora.util.RefreshableView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchasesController implements RefreshableView {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy  h:mm a");

    private final PurchaseService purchaseService = AppContext.purchaseService();
    private final SupplierService supplierService = AppContext.supplierService();
    private final ObservableList<PurchaseRow> purchases = FXCollections.observableArrayList();
    private FilteredList<PurchaseRow> filteredPurchases;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> paymentFilter;
    @FXML private TableView<PurchaseRow> purchasesTable;
    @FXML private TableColumn<PurchaseRow, String> invoiceColumn;
    @FXML private TableColumn<PurchaseRow, String> dateColumn;
    @FXML private TableColumn<PurchaseRow, String> supplierColumn;
    @FXML private TableColumn<PurchaseRow, String> itemsColumn;
    @FXML private TableColumn<PurchaseRow, String> totalColumn;
    @FXML private TableColumn<PurchaseRow, String> paymentColumn;
    @FXML private Label totalPurchasesLabel;
    @FXML private Label purchaseCountLabel;
    @FXML private Label unpaidCountLabel;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        configureTable();
        paymentFilter.setItems(FXCollections.observableArrayList("All Payments", "Paid", "Unpaid"));
        paymentFilter.setValue("All Payments");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        paymentFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        loadPurchases();
    }

    private void configureTable() {
        invoiceColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().invoiceNumber()));
        dateColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().date()));
        supplierColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().supplier()));
        itemsColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().items()));
        totalColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().total()));
        paymentColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().payment()));
        paymentColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : value);
                getStyleClass().removeAll("status-paid", "status-unpaid");
                if (!empty && value != null) {
                    getStyleClass().add("Paid".equals(value) ? "status-paid" : "status-unpaid");
                }
            }
        });
    }

    private void loadPurchases() {
        Map<Long, String> supplierNames = new HashMap<>();
        for (Supplier supplier : supplierService.getAllSuppliers()) {
            supplierNames.put(supplier.getId(), supplier.getName());
        }
        Map<Long, Integer> itemCounts = purchaseService.getPurchaseItemCounts();

        List<PurchaseRow> rows = purchaseService.getAllPurchases().stream()
                .map(purchase -> {
                    int itemCount = itemCounts.getOrDefault(purchase.getId(), 0);
                    String supplier = purchase.getSupplierId() == null
                            ? "Walk-in / No supplier"
                            : supplierNames.getOrDefault(purchase.getSupplierId(), "Unknown supplier");
                    return new PurchaseRow(
                            purchase.getInvoiceNumber(),
                            purchase.getCreatedAt() == null ? "" : purchase.getCreatedAt().format(DATE_FORMAT),
                            supplier,
                            itemCount + (itemCount == 1 ? " item" : " items"),
                            CurrencyFormatter.format(purchase.getTotal()),
                            purchase.getPaymentStatus() == PaymentStatus.PAID ? "Paid" : "Unpaid",
                            purchase.getTotal(),
                            purchase.getPaymentStatus()
                    );
                })
                .toList();

        purchases.setAll(rows);
        if (filteredPurchases == null) {
            filteredPurchases = new FilteredList<>(purchases, row -> true);
            purchasesTable.setItems(filteredPurchases);
        }
        applyFilter();
    }

    @Override
    public void refreshView() {
        loadPurchases();
    }

    private void applyFilter() {
        if (filteredPurchases == null) return;
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String payment = paymentFilter.getValue();
        filteredPurchases.setPredicate(row -> {
            boolean matchesText = query.isBlank()
                    || row.invoiceNumber().toLowerCase().contains(query)
                    || row.supplier().toLowerCase().contains(query);
            boolean matchesPayment = payment == null || "All Payments".equals(payment)
                    || payment.equals(row.payment());
            return matchesText && matchesPayment;
        });
        updateSummary();
    }

    private void updateSummary() {
        double total = filteredPurchases.stream().mapToDouble(PurchaseRow::rawTotal).sum();
        long unpaid = filteredPurchases.stream()
                .filter(row -> row.paymentStatus() != PaymentStatus.PAID)
                .count();
        totalPurchasesLabel.setText(CurrencyFormatter.format(total));
        purchaseCountLabel.setText(String.valueOf(filteredPurchases.size()));
        unpaidCountLabel.setText(String.valueOf(unpaid));
        statusLabel.setText(filteredPurchases.size() + " purchase(s) shown.");
    }

    @FXML
    private void handleReceiveStock() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/innovatewithomer/bizora/fxml/add-purchase-dialog.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Receive Stock");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setMinWidth(760);
            stage.setMinHeight(600);
            stage.showAndWait();
            loadPurchases();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to open stock receiving dialog.", exception);
        }
    }

    public record PurchaseRow(
            String invoiceNumber,
            String date,
            String supplier,
            String items,
            String total,
            String payment,
            double rawTotal,
            PaymentStatus paymentStatus
    ) { }
}
