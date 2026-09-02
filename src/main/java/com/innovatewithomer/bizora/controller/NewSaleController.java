package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.config.AppSettingsStore;
import com.innovatewithomer.bizora.model.Customer;
import com.innovatewithomer.bizora.model.Payment;
import com.innovatewithomer.bizora.model.PaymentMethod;
import com.innovatewithomer.bizora.model.Product;
import com.innovatewithomer.bizora.model.Sale;
import com.innovatewithomer.bizora.model.SaleItem;
import com.innovatewithomer.bizora.service.CustomerService;
import com.innovatewithomer.bizora.service.PaymentService;
import com.innovatewithomer.bizora.service.SaleService;
import com.innovatewithomer.bizora.util.CurrencyFormatter;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Controller for the New Sale / POS screen.
 *
 * Responsibilities:
 *
 * - Product searching
 * - Product selection
 * - Cart management
 * - Sale total calculation
 * - Payment collection
 * - Creating the sale
 * - Creating the payment
 *
 * Navigation between New Sale and Sales History
 * belongs to SalesController.
 */
public class NewSaleController {


    // =========================================================
    // SERVICES
    // =========================================================

    private final SaleService saleService =
            AppContext.saleService();

    private final PaymentService paymentService =
            AppContext.paymentService();

    private final CustomerService customerService =
            AppContext.customerService();


    // =========================================================
    // DATA
    // =========================================================

    private final ObservableList<Product> products =
            FXCollections.observableArrayList();

    private final ObservableList<Product> filteredProducts =
            FXCollections.observableArrayList();

    private final ObservableList<SaleItem> cart =
            FXCollections.observableArrayList();

    private final ObservableList<Customer> customers =
            FXCollections.observableArrayList();

    private final Map<Long, Product> productMap =
            new HashMap<>();


    // =========================================================
    // PRODUCT TABLE
    // =========================================================

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, String> productSkuColumn;

    @FXML
    private TableColumn<Product, Double> productStockColumn;

    @FXML
    private TableColumn<Product, Double> productPriceColumn;


    // =========================================================
    // CART TABLE
    // =========================================================

    @FXML
    private TableView<SaleItem> cartTable;

    @FXML
    private TableColumn<SaleItem, String> cartProductColumn;

    @FXML
    private TableColumn<SaleItem, Double> cartQuantityColumn;

    @FXML
    private TableColumn<SaleItem, Double> cartPriceColumn;

    @FXML
    private TableColumn<SaleItem, Double> cartDiscountColumn;

    @FXML
    private TableColumn<SaleItem, Double> cartSubtotalColumn;

    @FXML
    private TableColumn<SaleItem, Void> cartActionColumn;


    // =========================================================
    // SEARCH
    // =========================================================

    @FXML
    private TextField searchField;

    @FXML
    private TextField quantityField;

    @FXML
    private Label productCountLabel;


    // =========================================================
    // SALE SUMMARY
    // =========================================================

    @FXML
    private ComboBox<Customer> customerComboBox;

    @FXML
    private Label invoiceLabel;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Label remainingLabel;

    @FXML
    private TextField discountField;

    @FXML
    private TextField taxField;


    // =========================================================
    // PAYMENT
    // =========================================================

    @FXML
    private ComboBox<PaymentMethod> paymentMethodComboBox;

    @FXML
    private TextField paymentAmountField;


    // =========================================================
    // FORMATTING
    // =========================================================

    private static final DateTimeFormatter
            INVOICE_DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyyMMdd-HHmmss-SSS"
            );


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    private void initialize() {

        configureProductTable();

        configureCartTable();

        configureCustomerComboBox();

        configurePaymentMethods();

        configureListeners();

        loadProducts();

        loadCustomers();

        taxField.setText(
                String.format(
                        Locale.US,
                        "%.2f",
                        AppSettingsStore.load().taxRate()
                )
        );

        generateInvoiceNumber();

        updateSummary();
    }


    // =========================================================
    // CURRENCY
    // =========================================================

    // =========================================================
    // PRODUCT TABLE
    // =========================================================

    private void configureProductTable() {

        productNameColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                cell.getValue()
                                        .getName()
                        )
        );


        productSkuColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                cell.getValue()
                                        .getSku()
                        )
        );


        productStockColumn.setCellValueFactory(
                cell ->
                        new SimpleDoubleProperty(
                                cell.getValue()
                                        .getStockQuantity()
                        ).asObject()
        );


        productPriceColumn.setCellValueFactory(
                cell ->
                        new SimpleDoubleProperty(
                                cell.getValue()
                                        .getSellingPrice()
                        ).asObject()
        );


        productTable.setItems(
                filteredProducts
        );
    }


    // =========================================================
    // CART TABLE
    // =========================================================

    private void configureCartTable() {

        cartProductColumn.setCellValueFactory(
                cell -> {

                    Product product =
                            productMap.get(
                                    cell.getValue()
                                            .getProductId()
                            );

                    String name =
                            product == null
                                    ? "Unknown Product"
                                    : product.getName();

                    return new SimpleStringProperty(
                            name
                    );
                }
        );


        cartQuantityColumn.setCellValueFactory(
                cell ->
                        new SimpleDoubleProperty(
                                cell.getValue()
                                        .getQuantity()
                        ).asObject()
        );


        cartPriceColumn.setCellValueFactory(
                cell ->
                        new SimpleDoubleProperty(
                                cell.getValue()
                                        .getUnitPrice()
                        ).asObject()
        );


        cartDiscountColumn.setCellValueFactory(
                cell ->
                        new SimpleDoubleProperty(
                                cell.getValue()
                                        .getDiscount()
                        ).asObject()
        );


        cartSubtotalColumn.setCellValueFactory(
                cell ->
                        new SimpleDoubleProperty(
                                cell.getValue()
                                        .getSubtotal()
                        ).asObject()
        );


        configureCartActionColumn();

        cartTable.setItems(
                cart
        );
    }


    // =========================================================
    // CART ACTION
    // =========================================================

    private void configureCartActionColumn() {

        cartActionColumn.setCellFactory(
                column ->
                        new TableCell<>() {

                            private final Button button =
                                    new Button("Remove");


                            {
                                button.setOnAction(
                                        event -> {

                                            SaleItem item =
                                                    getTableView()
                                                            .getItems()
                                                            .get(
                                                                    getIndex()
                                                            );

                                            cart.remove(
                                                    item
                                            );

                                            updateSummary();
                                        }
                                );
                            }


                            @Override
                            protected void updateItem(
                                    Void item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty) {

                                    setGraphic(
                                            null
                                    );

                                } else {

                                    setGraphic(
                                            button
                                    );
                                }
                            }
                        }
        );
    }


    // =========================================================
    // CUSTOMER
    // =========================================================

    private void configureCustomerComboBox() {

        if (customerComboBox == null) {
            return;
        }

        customerComboBox.setItems(customers);

        javafx.util.StringConverter<Customer> converter =
                new javafx.util.StringConverter<>() {

                    @Override
                    public String toString(Customer customer) {
                        if (customer == null) {
                            return "Walk-in Customer";
                        }
                        String phone = customer.getPhone();
                        return customer.getName()
                                + (phone != null && !phone.isBlank() ? " (" + phone + ")" : "");
                    }

                    @Override
                    public Customer fromString(String string) {
                        return null;
                    }
                };

        customerComboBox.setConverter(converter);

        customerComboBox.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(converter.toString(customer));
                }
            }
        });

        customerComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);
                if (empty || customer == null) {
                    setText("Walk-in Customer");
                } else {
                    setText(converter.toString(customer));
                }
            }
        });
    }

    private void loadCustomers() {

        try {
            List<Customer> customerList =
                    customerService.getAllCustomers();
            customers.setAll(customerList);
        } catch (Exception e) {
            customers.clear();
            showWarning(
                    "Customers could not be loaded. You can still create a walk-in sale.\n\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // PAYMENT METHODS
    // =========================================================

    private void configurePaymentMethods() {

        paymentMethodComboBox.setItems(
                FXCollections.observableArrayList(
                        PaymentMethod.values()
                )
        );

        paymentMethodComboBox.setValue(
                PaymentMethod.CASH
        );
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void configureListeners() {

        discountField.textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updateSummary()
                );


        taxField.textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updateSummary()
                );


        paymentAmountField.textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updateRemaining()
                );
    }


    // =========================================================
    // PRODUCTS
    // =========================================================

    private void loadProducts() {

        List<Product> productList =
                AppContext.productService()
                        .getAllProducts();


        products.setAll(
                productList
        );


        productMap.clear();


        for (Product product :
                productList) {

            productMap.put(
                    product.getId(),
                    product
            );
        }


        filteredProducts.setAll(
                productList
        );


        updateProductCount();
    }


    // =========================================================
    // SEARCH
    // =========================================================

    @FXML
    private void handleSearch() {

        String query =
                searchField.getText();


        if (query == null ||
                query.isBlank()) {

            filteredProducts.setAll(
                    products
            );

            updateProductCount();

            return;
        }


        String search =
                query.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        List<Product> result =
                products.stream()
                        .filter(
                                product -> {

                                    String name =
                                            product.getName()
                                                    == null
                                                    ? ""
                                                    : product.getName()
                                                    .toLowerCase(
                                                            Locale.ROOT
                                                    );


                                    String sku =
                                            product.getSku()
                                                    == null
                                                    ? ""
                                                    : product.getSku()
                                                    .toLowerCase(
                                                            Locale.ROOT
                                                    );


                                    return name.contains(
                                            search
                                    )
                                            ||
                                            sku.contains(
                                                    search
                                            );
                                }
                        )
                        .toList();


        filteredProducts.setAll(
                result
        );

        updateProductCount();
    }


    @FXML
    private void handleClearSearch() {

        searchField.clear();

        filteredProducts.setAll(
                products
        );

        updateProductCount();
    }


    private void updateProductCount() {

        int count =
                filteredProducts.size();


        productCountLabel.setText(
                count
                        + (
                        count == 1
                                ? " product"
                                : " products"
                )
        );
    }


    // =========================================================
    // ADD TO CART
    // =========================================================

    @FXML
    private void handleAddToCart() {

        Product product =
                productTable
                        .getSelectionModel()
                        .getSelectedItem();


        if (product == null) {

            showWarning(
                    "Please select a product."
            );

            return;
        }


        addSelectedProductToCart(
                product
        );
    }


    @FXML
    private void handleProductDoubleClick(
            javafx.scene.input.MouseEvent event
    ) {

        if (event.getClickCount() != 2) {

            return;
        }


        Product product =
                productTable
                        .getSelectionModel()
                        .getSelectedItem();


        if (product != null) {

            addSelectedProductToCart(
                    product
            );
        }
    }


    private void addSelectedProductToCart(
            Product product
    ) {

        double quantity;


        try {

            quantity =
                    Double.parseDouble(
                            quantityField
                                    .getText()
                    );

        } catch (NumberFormatException e) {

            showWarning(
                    "Please enter a valid quantity."
            );

            return;
        }


        if (quantity <= 0) {

            showWarning(
                    "Quantity must be greater than zero."
            );

            return;
        }


        if (quantity >
                product.getStockQuantity()) {

            showWarning(
                    "Insufficient stock.\n"
                            + "Available: "
                            + product.getStockQuantity()
            );

            return;
        }


        for (SaleItem existing :
                cart) {

            if (existing.getProductId()
                    .equals(
                            product.getId()
                    )) {

                double newQuantity =
                        existing.getQuantity()
                                + quantity;


                if (newQuantity >
                        product.getStockQuantity()) {

                    showWarning(
                            "Insufficient stock.\n"
                                    + "Available: "
                                    + product.getStockQuantity()
                    );

                    return;
                }


                existing.setQuantity(
                        newQuantity
                );


                cartTable.refresh();

                updateSummary();

                return;
            }
        }


        SaleItem item =
                new SaleItem(
                        product.getId(),
                        quantity,
                        product.getSellingPrice(),
                        0
                );


        cart.add(
                item
        );


        quantityField.setText(
                "1"
        );


        updateSummary();
    }


    // =========================================================
    // SUMMARY
    // =========================================================

    private void updateSummary() {

        double subtotal =
                calculateSubtotal();


        double discount =
                parseAmount(
                        discountField.getText()
                );


        double tax = calculateTax(subtotal);


        double total =
                subtotal
                        - discount
                        + tax;


        subtotalLabel.setText(
                formatCurrency(
                        subtotal
                )
        );


        totalLabel.setText(
                formatCurrency(
                        Math.max(
                                total,
                                0
                        )
                )
        );


        updateRemaining();
    }


    private void updateRemaining() {

        double total =
                calculateTotal();


        double payment =
                parseAmount(
                        paymentAmountField.getText()
                );


        double remaining =
                Math.max(
                        total - payment,
                        0
                );


        remainingLabel.setText(
                "Remaining: "
                        + formatCurrency(
                        remaining
                )
        );
    }


    private double calculateSubtotal() {

        return cart.stream()
                .mapToDouble(
                        SaleItem::getSubtotal
                )
                .sum();
    }


    private double calculateTotal() {

        double subtotal =
                calculateSubtotal();


        double discount =
                parseAmount(
                        discountField.getText()
                );


        double tax = calculateTax(subtotal);


        return Math.max(
                subtotal
                        - discount
                        + tax,
                0
        );
    }


    // =========================================================
    // COMPLETE SALE
    // =========================================================

    @FXML
    private void handleCompleteSale() {

        if (cart.isEmpty()) {

            showWarning(
                    "Cart is empty."
            );

            return;
        }

        if (!isValidNumber(discountField.getText())) {
            showWarning("Please enter a valid discount amount.");
            return;
        }

        if (!isValidNumber(taxField.getText())) {
            showWarning("Please enter a valid tax rate.");
            return;
        }

        if (!isValidNumber(paymentAmountField.getText())) {
            showWarning("Please enter a valid payment amount.");
            return;
        }


        double subtotal =
                calculateSubtotal();


        double discount =
                parseAmount(
                        discountField.getText()
                );


        double taxRate = parseAmount(taxField.getText());
        double tax = calculateTax(subtotal);


        double paymentAmount =
                parseAmount(
                        paymentAmountField
                                .getText()
                );


        if (discount < 0) {

            showWarning(
                    "Discount cannot be negative."
            );

            return;
        }


        if (taxRate < 0 || taxRate > 100) {

            showWarning(
                    "Tax rate must be between 0 and 100."
            );

            return;
        }


        if (discount > subtotal) {

            showWarning(
                    "Discount cannot exceed subtotal."
            );

            return;
        }


        double total =
                subtotal
                        - discount
                        + tax;


        if (total < 0) {

            total = 0;
        }


        if (paymentAmount < 0) {

            showWarning(
                    "Payment amount cannot be negative."
            );

            return;
        }


        if (paymentAmount > total) {

            showWarning(
                    "Payment cannot exceed sale total."
            );

            return;
        }


        try {

            Sale sale =
                    new Sale(
                            generateInvoiceNumber()
                    );


            Customer selectedCustomer =
                    customerComboBox != null
                            ? customerComboBox.getValue()
                            : null;

            sale.setCustomerId(
                    selectedCustomer != null
                            ? selectedCustomer.getId()
                            : null
            );


            sale.setDiscount(
                    discount
            );


            sale.setTax(
                    tax
            );


            for (SaleItem cartItem :
                    cart) {

                SaleItem item =
                        new SaleItem(
                                cartItem.getProductId(),
                                cartItem.getQuantity(),
                                cartItem.getUnitPrice(),
                                cartItem.getDiscount()
                        );


                sale.addItem(
                        item
                );
            }


            sale.calculateTotals();


            Sale savedSale =
                    saleService.createSale(
                            sale
                    );


            if (paymentAmount > 0) {

                Payment payment =
                        new Payment(
                                savedSale.getId(),
                                paymentAmount,
                                paymentMethodComboBox
                                        .getValue()
                        );


                paymentService.addPayment(
                        payment
                );
            }


            showSuccess(
                    "Sale completed successfully.\n\n"
                            + "Invoice: "
                            + savedSale
                            .getInvoiceNumber()
                            + "\n"
                            + "Total: "
                            + formatCurrency(
                            savedSale.getTotal()
                    )
                            + "\n"
                            + "Paid: "
                            + formatCurrency(
                            paymentAmount
                    )
            );


            clearSale();


        } catch (IllegalArgumentException e) {

            showWarning(
                    e.getMessage()
            );

        } catch (Exception e) {
            showError(
                    "Failed to complete sale.\n\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // CLEAR CART
    // =========================================================

    @FXML
    private void handleClearCart() {

        if (cart.isEmpty()) {

            return;
        }


        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        alert.setTitle(
                "Clear Cart"
        );


        alert.setHeaderText(
                "Clear current sale?"
        );


        alert.setContentText(
                "All items currently in the cart "
                        + "will be removed."
        );


        alert.showAndWait()
                .ifPresent(
                        response -> {

                            if (response ==
                                    javafx.scene.control.ButtonType.OK) {

                                clearSale();
                            }
                        }
                );
    }


    private void clearSale() {

        cart.clear();

        cartTable.refresh();


        discountField.setText(
                "0"
        );


        taxField.setText(String.format(
                Locale.US,
                "%.2f",
                AppSettingsStore.load().taxRate()
        ));


        paymentAmountField.setText(
                "0"
        );


        quantityField.setText(
                "1"
        );


        paymentMethodComboBox.setValue(
                PaymentMethod.CASH
        );


        if (customerComboBox != null) {
            customerComboBox.setValue(null);
        }


        generateInvoiceNumber();

        updateSummary();
    }


    // =========================================================
    // INVOICE
    // =========================================================

    private String generateInvoiceNumber() {

        String invoice =
                AppSettingsStore.load().invoicePrefix()
                        + LocalDateTime.now()
                        .format(
                                INVOICE_DATE_FORMAT
                        );


        invoiceLabel.setText(
                "Invoice: " + invoice
        );


        return invoice;
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private double parseAmount(
            String value
    ) {

        if (value == null ||
                value.isBlank()) {

            return 0;
        }


        try {

            return Double.parseDouble(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            return 0;
        }
    }

    private double calculateTax(double subtotal) {
        double rate = parseAmount(taxField.getText());
        return subtotal * rate / 100.0;
    }

    private boolean isValidNumber(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            double parsed = Double.parseDouble(value.trim());
            return Double.isFinite(parsed);
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private String formatCurrency(
            double amount
    ) {

        return CurrencyFormatter.format(amount);
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
                "Sale Completed"
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
                "Sales Error"
        );


        alert.setHeaderText(
                "Unable to complete sale"
        );


        alert.setContentText(
                message
        );


        alert.showAndWait();
    }
}
