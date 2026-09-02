package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.Expense;
import com.innovatewithomer.bizora.service.ExpenseService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AddExpenseController {

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Rent", "Utilities", "Salaries", "Marketing",
            "Supplies", "Transportation", "Maintenance", "Other"
    );

    private final ExpenseService expenseService = AppContext.expenseService();

    @FXML private Label dialogTitleLabel;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionField;
    @FXML private Label errorLabel;

    @FXML
    private void initialize() {
        categoryCombo.setItems(FXCollections.observableArrayList(DEFAULT_CATEGORIES));
        datePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleSave() {
        String category    = categoryCombo.getValue() != null ? categoryCombo.getValue().trim() : "";
        String amountText  = amountField.getText().trim();
        LocalDate date     = datePicker.getValue();
        String description = descriptionField.getText().trim();

        if (category.isEmpty()) {
            showError("Category is required.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Please enter a valid positive amount.");
            return;
        }

        if (date == null) {
            showError("Date is required.");
            return;
        }

        try {
            Expense expense = new Expense(category, description.isEmpty() ? null : description, amount, date);
            expense.setCreatedAt(LocalDateTime.now());
            expenseService.createExpense(expense);
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
        ((Stage) amountField.getScene().getWindow()).close();
    }
}
