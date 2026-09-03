package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.Customer;
import com.innovatewithomer.bizora.service.CustomerService;
import com.innovatewithomer.bizora.util.RefreshableView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class CustomersController implements RefreshableView {

    private final CustomerService customerService = AppContext.customerService();

    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredCustomers;

    @FXML private TableView<Customer> customersTable;
    @FXML private TableColumn<Customer, Long>   idColumn;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, String> addressColumn;
    @FXML private TableColumn<Customer, Void>   actionsColumn;
    @FXML private TextField searchField;
    @FXML private Label totalCustomersLabel;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        configureTable();
        configureSearch();
        loadCustomers();
    }

    private void configureTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        configureActionsColumn();
    }

    private void configureActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("secondary-button");
                deleteBtn.getStyleClass().add("danger-button");

                editBtn.setOnAction(e -> {
                    Customer c = getTableView().getItems().get(getIndex());
                    handleEditCustomer(c);
                });
                deleteBtn.setOnAction(e -> {
                    Customer c = getTableView().getItems().get(getIndex());
                    handleDeleteCustomer(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void configureSearch() {
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
    }

    private void loadCustomers() {
        customerList.setAll(customerService.getAllCustomers());

        if (filteredCustomers == null) {
            filteredCustomers = new FilteredList<>(customerList, c -> true);
            customersTable.setItems(filteredCustomers);
        }

        applyFilter();
        totalCustomersLabel.setText(String.valueOf(customerList.size()));
        statusLabel.setText(customerList.size() + " customer(s) found.");
    }

    @Override
    public void refreshView() {
        loadCustomers();
    }

    private void applyFilter() {
        String q = searchField.getText().trim().toLowerCase();
        filteredCustomers.setPredicate(c -> {
            if (q.isEmpty()) return true;
            return (c.getName()  != null && c.getName().toLowerCase().contains(q))
                || (c.getPhone() != null && c.getPhone().toLowerCase().contains(q))
                || (c.getEmail() != null && c.getEmail().toLowerCase().contains(q));
        });
    }

    @FXML
    private void handleAddCustomer() {
        openDialog(null);
    }

    private void handleEditCustomer(Customer customer) {
        openDialog(customer);
    }

    private void openDialog(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/innovatewithomer/bizora/fxml/add-customer-dialog.fxml"));
            Parent root = loader.load();

            AddCustomerController ctrl = loader.getController();
            if (customer != null) {
                ctrl.setCustomer(customer);
            }

            Stage stage = new Stage();
            stage.setTitle(customer == null ? "Add Customer" : "Edit Customer");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadCustomers();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open customer dialog.", e);
        }
    }

    private void handleDeleteCustomer(Customer customer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Customer");
        alert.setHeaderText("Delete " + customer.getName() + "?");
        alert.setContentText("This action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                customerService.deleteCustomer(customer.getId());
                loadCustomers();
            } catch (RuntimeException e) {
                showDeleteError("This customer is linked to existing sales and cannot be deleted.");
            }
        }
    }

    private void showDeleteError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Customers");
        alert.setHeaderText("Unable to delete customer");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSearch() {
        applyFilter();
    }
}
