package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.Customer;
import com.innovatewithomer.bizora.service.CustomerService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddCustomerController {

    private final CustomerService customerService = AppContext.customerService();

    @FXML private Label    dialogTitleLabel;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea  addressField;
    @FXML private Label     errorLabel;

    private Customer existingCustomer;

    /** Pre-populate fields when editing an existing customer. */
    public void setCustomer(Customer customer) {
        this.existingCustomer = customer;
        dialogTitleLabel.setText("Edit Customer");
        nameField.setText(customer.getName());
        phoneField.setText(customer.getPhone() != null ? customer.getPhone() : "");
        emailField.setText(customer.getEmail() != null ? customer.getEmail() : "");
        addressField.setText(customer.getAddress() != null ? customer.getAddress() : "");
    }

    @FXML
    private void handleSave() {
        String name    = nameField.getText().trim();
        String phone   = phoneField.getText().trim();
        String email   = emailField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty()) {
            showError("Customer name is required.");
            return;
        }

        try {
            if (existingCustomer == null) {
                Customer c = new Customer(
                        name,
                        phone.isEmpty() ? null : phone,
                        email.isEmpty() ? null : email,
                        address.isEmpty() ? null : address
                );
                customerService.createCustomer(c);
            } else {
                existingCustomer.setName(name);
                existingCustomer.setPhone(phone.isEmpty() ? null : phone);
                existingCustomer.setEmail(email.isEmpty() ? null : email);
                existingCustomer.setAddress(address.isEmpty() ? null : address);
                customerService.updateCustomer(existingCustomer);
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
