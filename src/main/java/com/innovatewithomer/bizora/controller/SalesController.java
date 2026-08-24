package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.Payment;
import com.innovatewithomer.bizora.model.PaymentMethod;
import com.innovatewithomer.bizora.model.Product;
import com.innovatewithomer.bizora.model.Sale;
import com.innovatewithomer.bizora.model.SaleItem;
import com.innovatewithomer.bizora.service.PaymentService;
import com.innovatewithomer.bizora.service.SaleService;

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

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Controller for the Sales / POS screen.
 *
 * Responsibilities:
 *
 * - Product searching
 * - Cart management
 * - Sale total calculation
 * - Payment collection
 * - Calling SaleService
 * - Calling PaymentService
 *
 * Business rules remain inside the services.
 */
public class SalesController {


    // =========================================================
    // SERVICES
    // =========================================================

    private final SaleService saleService =
            AppContext.saleService();

    private final PaymentService paymentService =
            AppContext.paymentService();


    // =========================================================
    // DATA
    // =========================================================

    private final ObservableList<Product> products =
            FXCollections.observableArrayList();

    private final ObservableList<Product> filteredProducts =
            FXCollections.observableArrayList();

    private final ObservableList<SaleItem> cart =
            FXCollections.observableArrayList();


    /*
     * Product lookup.
     *
     * SaleItem stores only productId.
     * This map lets the UI display the product name.
     */
    private final java.util.Map<Long, Product> productMap =
            new java.util.HashMap<>();


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

    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getNumberInstance(
                    Locale.US
            );

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

        configureCurrency();

        configureProductTable();

        configureCartTable();

        configurePaymentMethods();

        configureListeners();

        loadProducts();

        generateInvoiceNumber();

        updateSummary();
    }


    // =========================================================
    // CONFIGURATION
    // =========================================================

    private void configureCurrency() {

        CURRENCY_FORMAT.setMinimumFractionDigits(2);

        CURRENCY_FORMAT.setMaximumFractionDigits(2);
    }


    private void configureProductTable() {

        productNameColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                cell.getValue().getName()
                        )
        );

        productSkuColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                cell.getValue().getSku()
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

        cartTable.setItems(cart);
    }


    private void configureCartActionColumn() {

        cartActionColumn.setCellFactory(
                column -> new TableCell<>() {

                    private final Button button =
                            new Button("Remove");

                    {
                        button.setOnAction(event -> {

                            SaleItem item =
                                    getTableView()
                                            .getItems()
                                            .get(
                                                    getIndex()
                                            );

                            cart.remove(item);

                            updateSummary();
                        });
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

                            setGraphic(null);

                        } else {

                            setGraphic(button);
                        }
                    }
                }
        );
    }


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

        for (Product product : productList) {

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
                        .filter(product ->

                                product.getName()
                                        .toLowerCase(
                                                Locale.ROOT
                                        )
                                        .contains(search)

                                        ||

                                        (
                                                product.getSku() != null
                                                        &&
                                                        product.getSku()
                                                                .toLowerCase(
                                                                        Locale.ROOT
                                                                )
                                                                .contains(search)
                                        )
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
                count + (
                        count == 1
                                ? " product"
                                : " products"
                )
        );
    }


    // =========================================================
    // ADD PRODUCT
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


        /*
         * If the product is already in the cart,
         * increase its quantity instead of adding
         * a duplicate line.
         */
        for (SaleItem existing :
                cart) {

            if (existing.getProductId()
                    .equals(product.getId())) {

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

        cart.add(item);

        quantityField.setText("1");

        updateSummary();
    }


    // =========================================================
    // SUMMARY
    // =========================================================

    private void updateSummary() {

        double subtotal =
                cart.stream()
                        .mapToDouble(
                                SaleItem::getSubtotal
                        )
                        .sum();

        double discount =
                parseAmount(
                        discountField.getText()
                );

        double tax =
                parseAmount(
                        taxField.getText()
                );

        double total =
                subtotal
                        - discount
                        + tax;


        if (discount > subtotal) {

            total = tax;
        }


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
                        paymentAmountField
                                .getText()
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

        double tax =
                parseAmount(
                        taxField.getText()
                );

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


        double subtotal =
                calculateSubtotal();

        double discount =
                parseAmount(
                        discountField.getText()
                );

        double tax =
                parseAmount(
                        taxField.getText()
                );

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


        if (tax < 0) {

            showWarning(
                    "Tax cannot be negative."
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

            /*
             * Build Sale object.
             */
            Sale sale =
                    new Sale(
                            generateInvoiceNumber()
                    );


            /*
             * Walk-in sale for now.
             */
            sale.setCustomerId(
                    null
            );


            sale.setDiscount(
                    discount
            );

            sale.setTax(
                    tax
            );


            /*
             * Copy cart items.
             */
            for (SaleItem cartItem :
                    cart) {

                SaleItem item =
                        new SaleItem(
                                cartItem.getProductId(),
                                cartItem.getQuantity(),
                                cartItem.getUnitPrice(),
                                cartItem.getDiscount()
                        );

                sale.addItem(item);
            }


            /*
             * Ensure final totals are calculated.
             */
            sale.calculateTotals();


            /*
             * SaleService handles:
             *
             * - stock validation
             * - sale insertion
             * - sale item insertion
             * - stock reduction
             * - inventory movements
             *
             * inside one transaction.
             */
            Sale savedSale =
                    saleService.createSale(
                            sale
                    );


            /*
             * If customer pays something,
             * create the payment after sale creation.
             */
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

            e.printStackTrace();

            showError(
                    "Failed to complete sale.\n\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // CLEAR SALE
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
                .ifPresent(response -> {

                    if (response ==
                            javafx.scene.control.ButtonType.OK) {

                        clearSale();
                    }
                });
    }


    private void clearSale() {

        cart.clear();

        cartTable.refresh();

        discountField.setText(
                "0"
        );

        taxField.setText(
                "0"
        );

        paymentAmountField.setText(
                "0"
        );

        quantityField.setText(
                "1"
        );

        paymentMethodComboBox.setValue(
                PaymentMethod.CASH
        );

        generateInvoiceNumber();

        updateSummary();
    }


    // =========================================================
    // INVOICE
    // =========================================================

    private String generateInvoiceNumber() {

        String invoice =
                "INV-"
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


    private String formatCurrency(
            double amount
    ) {

        return "Rs. "
                + CURRENCY_FORMAT.format(
                amount
        );
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