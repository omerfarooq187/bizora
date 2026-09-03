package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.*;
import com.innovatewithomer.bizora.util.CurrencyFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddPurchaseController {

    private final ObservableList<PurchaseLine> lines = FXCollections.observableArrayList();

    @FXML private ComboBox<Supplier> supplierCombo;
    @FXML private TextField invoiceField;
    @FXML private ComboBox<PaymentStatus> paymentCombo;
    @FXML private ComboBox<Product> productCombo;
    @FXML private TextField quantityField;
    @FXML private TextField unitCostField;
    @FXML private TextField itemDiscountField;
    @FXML private TextField purchaseDiscountField;
    @FXML private TextField taxField;
    @FXML private TableView<PurchaseLine> itemsTable;
    @FXML private TableColumn<PurchaseLine, String> productColumn;
    @FXML private TableColumn<PurchaseLine, String> quantityColumn;
    @FXML private TableColumn<PurchaseLine, String> costColumn;
    @FXML private TableColumn<PurchaseLine, String> discountColumn;
    @FXML private TableColumn<PurchaseLine, String> subtotalColumn;
    @FXML private TableColumn<PurchaseLine, Void> removeColumn;
    @FXML private Label subtotalLabel;
    @FXML private Label totalLabel;
    @FXML private Label errorLabel;

    @FXML
    private void initialize() {
        supplierCombo.setItems(FXCollections.observableArrayList(AppContext.supplierService().getAllSuppliers()));
        supplierCombo.setConverter(namedConverter(Supplier::getName));

        productCombo.setItems(FXCollections.observableArrayList(AppContext.productService().getAllProducts()));
        productCombo.setConverter(namedConverter(product -> product.getName() + "  ·  " + product.getSku()));
        productCombo.valueProperty().addListener((obs, oldProduct, product) -> {
            if (product != null) unitCostField.setText(decimal(product.getPurchasePrice()));
        });

        paymentCombo.setItems(FXCollections.observableArrayList(PaymentStatus.PAID, PaymentStatus.UNPAID));
        paymentCombo.setValue(PaymentStatus.PAID);
        paymentCombo.setConverter(new StringConverter<>() {
            @Override public String toString(PaymentStatus value) {
                return value == null ? "" : value == PaymentStatus.PAID ? "Paid" : "Unpaid";
            }
            @Override public PaymentStatus fromString(String value) { return null; }
        });

        invoiceField.setText("PUR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")));
        quantityField.setText("1");
        itemDiscountField.setText("0");
        purchaseDiscountField.setText("0");
        taxField.setText("0");

        configureTable();
        itemsTable.setItems(lines);
        lines.addListener((javafx.collections.ListChangeListener<PurchaseLine>) change -> updateTotals());
        purchaseDiscountField.textProperty().addListener((obs, oldValue, newValue) -> updateTotals());
        taxField.textProperty().addListener((obs, oldValue, newValue) -> updateTotals());
        updateTotals();
    }

    private void configureTable() {
        productColumn.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().product().getName()));
        quantityColumn.setCellValueFactory(row -> new SimpleStringProperty(decimal(row.getValue().item().getQuantity())));
        costColumn.setCellValueFactory(row -> new SimpleStringProperty(CurrencyFormatter.format(row.getValue().item().getUnitPrice())));
        discountColumn.setCellValueFactory(row -> new SimpleStringProperty(CurrencyFormatter.format(row.getValue().item().getDiscount())));
        subtotalColumn.setCellValueFactory(row -> new SimpleStringProperty(CurrencyFormatter.format(row.getValue().item().getSubtotal())));
        removeColumn.setCellFactory(column -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");
            {
                removeButton.getStyleClass().add("danger-button");
                removeButton.setOnAction(event -> {
                    PurchaseLine line = getTableRow() == null ? null : getTableRow().getItem();
                    if (line != null) lines.remove(line);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });
    }

    @FXML
    private void handleAddItem() {
        clearError();
        try {
            Product product = productCombo.getValue();
            if (product == null) throw new IllegalArgumentException("Choose a product first.");
            if (lines.stream().anyMatch(line -> line.product().getId().equals(product.getId()))) {
                throw new IllegalArgumentException("This product is already in the purchase.");
            }

            double quantity = positive(quantityField.getText(), "Quantity");
            double unitCost = nonNegative(unitCostField.getText(), "Unit cost");
            double discount = nonNegative(itemDiscountField.getText(), "Item discount");
            if (discount > quantity * unitCost) {
                throw new IllegalArgumentException("Item discount cannot exceed its value.");
            }
            PurchaseItem item = new PurchaseItem(product.getId(), quantity, unitCost, discount);
            lines.add(new PurchaseLine(product, item));
            productCombo.setValue(null);
            quantityField.setText("1");
            unitCostField.clear();
            itemDiscountField.setText("0");
        } catch (RuntimeException exception) {
            showError(rootMessage(exception));
        }
    }

    @FXML
    private void handleSave() {
        clearError();
        try {
            if (invoiceField.getText() == null || invoiceField.getText().isBlank()) {
                throw new IllegalArgumentException("Invoice number is required.");
            }
            if (lines.isEmpty()) throw new IllegalArgumentException("Add at least one product.");

            Purchase purchase = new Purchase(invoiceField.getText().trim());
            Supplier supplier = supplierCombo.getValue();
            purchase.setSupplierId(supplier == null ? null : supplier.getId());
            purchase.setPaymentStatus(paymentCombo.getValue());
            purchase.setDiscount(nonNegative(purchaseDiscountField.getText(), "Purchase discount"));
            purchase.setTax(nonNegative(taxField.getText(), "Tax"));
            lines.forEach(line -> purchase.addItem(line.item()));
            AppContext.purchaseService().createPurchase(purchase);
            close();
        } catch (RuntimeException exception) {
            showError(rootMessage(exception));
        }
    }

    @FXML private void handleCancel() { close(); }

    private void updateTotals() {
        double subtotal = lines.stream().mapToDouble(line -> line.item().getSubtotal()).sum();
        double discount = parseOrZero(purchaseDiscountField == null ? null : purchaseDiscountField.getText());
        double tax = parseOrZero(taxField == null ? null : taxField.getText());
        subtotalLabel.setText(CurrencyFormatter.format(subtotal));
        totalLabel.setText(CurrencyFormatter.format(Math.max(0, subtotal - discount + tax)));
    }

    private double positive(String text, String name) {
        double value = number(text, name);
        if (value <= 0) throw new IllegalArgumentException(name + " must be greater than zero.");
        return value;
    }

    private double nonNegative(String text, String name) {
        double value = number(text, name);
        if (value < 0) throw new IllegalArgumentException(name + " cannot be negative.");
        return value;
    }

    private double number(String text, String name) {
        try {
            double value = Double.parseDouble(text == null ? "" : text.trim());
            if (!Double.isFinite(value)) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " must be a valid number.");
        }
    }

    private double parseOrZero(String text) {
        try { return Double.parseDouble(text == null || text.isBlank() ? "0" : text.trim()); }
        catch (NumberFormatException ignored) { return 0; }
    }

    private <T> StringConverter<T> namedConverter(java.util.function.Function<T, String> label) {
        return new StringConverter<>() {
            @Override public String toString(T value) { return value == null ? "" : label.apply(value); }
            @Override public T fromString(String value) { return null; }
        };
    }

    private String decimal(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private void showError(String message) { errorLabel.setText(message); }
    private void clearError() { errorLabel.setText(""); }
    private void close() { ((Stage) invoiceField.getScene().getWindow()).close(); }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? "Could not save this purchase." : current.getMessage();
    }

    public record PurchaseLine(Product product, PurchaseItem item) { }
}
