package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.Product;
import com.innovatewithomer.bizora.service.ProductService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddProductController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField skuField;

    @FXML
    private TextField purchasePriceField;

    @FXML
    private TextField sellingPriceField;

    @FXML
    private TextField stockField;

    private final ProductService productService = AppContext.productService();

    private Product product;


    @FXML
    private void handleSave() {

        try {

            String name =
                    nameField.getText().trim();

            String sku =
                    skuField.getText().trim();

            double purchasePrice =
                    Double.parseDouble(
                            purchasePriceField.getText().trim()
                    );

            double sellingPrice =
                    Double.parseDouble(
                            sellingPriceField.getText().trim()
                    );

            double stock =
                    Double.parseDouble(
                            stockField.getText().trim()
                    );

            if (product == null) {

                Product newProduct =
                        new Product(
                                name,
                                sku,
                                sellingPrice,
                                purchasePrice,
                                stock
                        );

                productService.createProduct(newProduct);

            } else {

                product.setName(name);
                product.setSku(sku);
                product.setPurchasePrice(purchasePrice);
                product.setSellingPrice(sellingPrice);
                product.setStockQuantity(stock);

                productService.updateProduct(product);
            }

            closeDialog();

        } catch (NumberFormatException e) {

            showError(
                    "Invalid number",
                    "Please enter valid numbers for prices and stock."
            );

        } catch (IllegalArgumentException e) {

            showError(
                    "Invalid product",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleCancel() {

        closeDialog();
    }

    public void setProduct(Product product) {

        this.product = product;

        nameField.setText(product.getName());
        skuField.setText(product.getSku());

        purchasePriceField.setText(
                String.valueOf(
                        product.getPurchasePrice()
                )
        );

        sellingPriceField.setText(
                String.valueOf(
                        product.getSellingPrice()
                )
        );

        stockField.setText(
                String.valueOf(
                        product.getStockQuantity()
                )
        );
    }

    private void closeDialog() {

        Stage stage =
                (Stage) nameField
                        .getScene()
                        .getWindow();

        stage.close();
    }

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}