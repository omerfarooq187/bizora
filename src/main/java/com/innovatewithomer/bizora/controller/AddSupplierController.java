package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.Supplier;
import com.innovatewithomer.bizora.service.SupplierService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddSupplierController {

    private final SupplierService supplierService = AppContext.supplierService();

    @FXML private Label    dialogTitleLabel;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea  addressField;
    @FXML private Label     errorLabel;

    private Supplier existingSupplier;

    public void setSupplier(Supplier supplier) {
        this.existingSupplier = supplier;
        dialogTitleLabel.setText("Edit Supplier");
        nameField.setText(supplier.getName());
        phoneField.setText(supplier.getPhone()   != null ? supplier.getPhone()   : "");
        emailField.setText(supplier.getEmail()   != null ? supplier.getEmail()   : "");
        addressField.setText(supplier.getAddress() != null ? supplier.getAddress() : "");
    }

    @FXML
    private void handleSave() {
        String name    = nameField.getText().trim();
        String phone   = phoneField.getText().trim();
        String email   = emailField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty()) {
            showError("Supplier name is required.");
            return;
        }

        try {
            if (existingSupplier == null) {
                Supplier s = new Supplier(
                        name,
                        phone.isEmpty()   ? null : phone,
                        email.isEmpty()   ? null : email,
                        address.isEmpty() ? null : address
                );
                supplierService.createSupplier(s);
            } else {
                existingSupplier.setName(name);
                existingSupplier.setPhone(phone.isEmpty()   ? null : phone);
                existingSupplier.setEmail(email.isEmpty()   ? null : email);
                existingSupplier.setAddress(address.isEmpty() ? null : address);
                supplierService.updateSupplier(existingSupplier);
            }
            closeStage();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    private void closeStage() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
