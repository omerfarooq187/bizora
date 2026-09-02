package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.App;
import com.innovatewithomer.bizora.config.AppSettings;
import com.innovatewithomer.bizora.config.AppSettingsStore;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.Year;

public class SettingsController {

    @FXML private TextField businessNameField;
    @FXML private TextField ownerNameField;
    @FXML private TextField businessPhoneField;
    @FXML private TextField businessEmailField;
    @FXML private TextArea  businessAddressField;
    @FXML private ComboBox<String> currencySymbolCombo;
    @FXML private TextField taxRateField;
    @FXML private TextField invoicePrefixField;
    @FXML private TextArea  receiptFooterField;
    @FXML private Label statusLabel;
    @FXML private Label aboutCopyrightLabel;

    @FXML
    private void initialize() {
        currencySymbolCombo.setItems(FXCollections.observableArrayList(
                "$", "€", "£", "¥", "₹", "₨", "CHF", "CAD", "AUD"
        ));
        aboutCopyrightLabel.setText(
                "© " + Year.now().getValue()
                        + " InnovateWithOmer. All rights reserved."
        );
        loadSettings();
    }

    private void loadSettings() {
        AppSettings settings = AppSettingsStore.load();
        businessNameField.setText(settings.businessName());
        ownerNameField.setText(settings.ownerName());
        businessPhoneField.setText(settings.businessPhone());
        businessEmailField.setText(settings.businessEmail());
        businessAddressField.setText(settings.businessAddress());
        currencySymbolCombo.setValue(settings.currencySymbol());
        taxRateField.setText(String.format("%.2f", settings.taxRate()));
        invoicePrefixField.setText(settings.invoicePrefix());
        receiptFooterField.setText(settings.receiptFooter());
        statusLabel.setText("");
    }

    @FXML
    private void handleSave() {
        String name = businessNameField.getText().trim();
        if (name.isEmpty()) {
            showStatus("Business name is required.", false);
            return;
        }

        String taxText = taxRateField.getText().trim();
        try {
            double tax = Double.parseDouble(taxText);
            if (!Double.isFinite(tax) || tax < 0 || tax > 100) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showStatus("Tax rate must be a number between 0 and 100.", false);
            return;
        }

        String currency = currencySymbolCombo.getValue();
        if (currency == null || currency.isBlank()) {
            showStatus("Currency symbol is required.", false);
            return;
        }

        String invoicePrefix = invoicePrefixField.getText().trim();
        if (invoicePrefix.isEmpty()) {
            showStatus("Invoice prefix is required.", false);
            return;
        }

        AppSettingsStore.save(new AppSettings(
                name,
                ownerNameField.getText().trim(),
                businessPhoneField.getText().trim(),
                businessEmailField.getText().trim(),
                businessAddressField.getText().trim(),
                currency,
                Double.parseDouble(taxText),
                invoicePrefix,
                receiptFooterField.getText().trim()
        ));

        MainController mainController = MainController.getInstance();
        if (mainController != null) {
            mainController.refreshBusinessIdentity();
        }
        showStatus("✓ Settings saved successfully.", true);
    }

    @FXML
    private void handleReset() {
        AppSettingsStore.reset();
        loadSettings();
        MainController mainController = MainController.getInstance();
        if (mainController != null) {
            mainController.refreshBusinessIdentity();
        }
        showStatus("Settings reset to defaults.", true);
    }

    @FXML
    private void handleOpenDeveloperWebsite() {
        App.openWebsite("https://innovatewithomer.dev");
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setStyle(success
                ? "-fx-text-fill:#10b981; -fx-font-size:13px; -fx-font-weight:bold;"
                : "-fx-text-fill:#ef4444; -fx-font-size:13px; -fx-font-weight:bold;");
    }
}
