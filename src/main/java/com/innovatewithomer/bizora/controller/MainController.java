package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.util.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.util.List;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button productsButton;

    @FXML
    private Button inventoryButton;

    @FXML
    private Button salesButton;

    @FXML
    private Button customersButton;

    @FXML
    private Button suppliersButton;

    @FXML
    private Button expensesButton;

    @FXML
    private Button reportsButton;

    @FXML
    private Button settingsButton;

    private ViewManager viewManager;

    @FXML
    private void initialize() {

        viewManager =
                new ViewManager(contentArea);

        showDashboard();
    }

    @FXML
    private void showDashboard() {

        setActiveButton(dashboardButton);

        viewManager.show("dashboard-view.fxml");
    }

    @FXML
    private void showProducts() {

        setActiveButton(productsButton);

        viewManager.show("products-view.fxml");
    }

    @FXML
    private void showInventory() {

        setActiveButton(inventoryButton);

        viewManager.show("inventory-view.fxml");
    }

    @FXML
    private void showSales() {

        setActiveButton(salesButton);

        viewManager.show("sales-view.fxml");
    }

    @FXML
    private void showCustomers() {

        setActiveButton(customersButton);

        viewManager.show("customers-view.fxml");
    }

    @FXML
    private void showSuppliers() {

        setActiveButton(suppliersButton);

        viewManager.show("suppliers-view.fxml");
    }

    @FXML
    private void showExpenses() {

        setActiveButton(expensesButton);

        viewManager.show("expenses-view.fxml");
    }

    @FXML
    private void showReports() {

        setActiveButton(reportsButton);

        viewManager.show("reports-view.fxml");
    }

    @FXML
    private void showSettings() {

        setActiveButton(settingsButton);

        viewManager.show("settings-view.fxml");
    }

    private void setActiveButton(
            Button activeButton
    ) {

        List<Button> navigationButtons =
                List.of(
                        dashboardButton,
                        productsButton,
                        inventoryButton,
                        salesButton,
                        customersButton,
                        suppliersButton,
                        expensesButton,
                        reportsButton,
                        settingsButton
                );

        for (Button button : navigationButtons) {

            button.getStyleClass()
                    .remove("nav-button-active");
        }

        if (!activeButton.getStyleClass()
                .contains("nav-button-active")) {

            activeButton.getStyleClass()
                    .add("nav-button-active");
        }
    }
}