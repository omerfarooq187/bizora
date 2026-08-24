package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.Product;
import com.innovatewithomer.bizora.service.InventoryService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdjustStockController {

    private final InventoryService inventoryService =
            AppContext.inventoryService();

    @FXML
    private Label productNameLabel;

    @FXML
    private Label currentStockLabel;

    @FXML
    private TextField actualStockField;

    @FXML
    private TextArea noteField;

    @FXML
    private Label errorLabel;

    private Product product;

    @FXML
    private void initialize() {

        errorLabel.setText("");
    }

    public void setProduct(Product product) {

        this.product = product;

        productNameLabel.setText(
                product.getName()
        );

        currentStockLabel.setText(
                formatQuantity(
                        product.getStockQuantity()
                )
        );

        actualStockField.setText(
                formatQuantity(
                        product.getStockQuantity()
                )
        );

        actualStockField.selectAll();
    }

    @FXML
    private void handleSave() {

        errorLabel.setText("");

        if (product == null) {

            showError(
                    "No product selected."
            );

            return;
        }

        String actualStockText =
                actualStockField.getText().trim();

        if (actualStockText.isEmpty()) {

            showError(
                    "Please enter the actual stock quantity."
            );

            return;
        }

        double actualStock;

        try {

            actualStock =
                    Double.parseDouble(
                            actualStockText
                    );

        } catch (NumberFormatException e) {

            showError(
                    "Stock quantity must be a valid number."
            );

            return;
        }

        if (actualStock < 0) {

            showError(
                    "Stock quantity cannot be negative."
            );

            return;
        }

        String note =
                noteField.getText().trim();

        try {

            inventoryService.adjustStock(
                    product.getId(),
                    actualStock,
                    note.isEmpty()
                            ? null
                            : note
            );

            close();

        } catch (IllegalArgumentException e) {

            showError(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            showError(
                    "Failed to adjust stock."
            );
        }
    }

    @FXML
    private void handleCancel() {

        close();
    }

    private void close() {

        Stage stage =
                (Stage) actualStockField
                        .getScene()
                        .getWindow();

        stage.close();
    }

    private void showError(
            String message
    ) {

        errorLabel.setText(
                message == null
                        ? "Something went wrong."
                        : message
        );
    }

    private String formatQuantity(
            double quantity
    ) {

        if (quantity == Math.floor(quantity)) {

            return String.valueOf(
                    (long) quantity
            );
        }

        return String.valueOf(quantity);
    }
}
