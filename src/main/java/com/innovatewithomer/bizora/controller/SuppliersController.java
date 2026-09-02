package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.Supplier;
import com.innovatewithomer.bizora.service.SupplierService;

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

public class SuppliersController {

    private final SupplierService supplierService = AppContext.supplierService();

    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList();
    private FilteredList<Supplier> filteredSuppliers;

    @FXML private TableView<Supplier> suppliersTable;
    @FXML private TableColumn<Supplier, Long>   idColumn;
    @FXML private TableColumn<Supplier, String> nameColumn;
    @FXML private TableColumn<Supplier, String> phoneColumn;
    @FXML private TableColumn<Supplier, String> emailColumn;
    @FXML private TableColumn<Supplier, String> addressColumn;
    @FXML private TableColumn<Supplier, Void>   actionsColumn;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        configureTable();
        configureSearch();
        loadSuppliers();
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
                    Supplier s = getTableView().getItems().get(getIndex());
                    openDialog(s);
                });
                deleteBtn.setOnAction(e -> {
                    Supplier s = getTableView().getItems().get(getIndex());
                    handleDelete(s);
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

    private void loadSuppliers() {
        supplierList.setAll(supplierService.getAllSuppliers());

        if (filteredSuppliers == null) {
            filteredSuppliers = new FilteredList<>(supplierList, s -> true);
            suppliersTable.setItems(filteredSuppliers);
        }

        applyFilter();
        statusLabel.setText(supplierList.size() + " supplier(s) found.");
    }

    private void applyFilter() {
        String q = searchField.getText().trim().toLowerCase();
        filteredSuppliers.setPredicate(s -> {
            if (q.isEmpty()) return true;
            return (s.getName()  != null && s.getName().toLowerCase().contains(q))
                || (s.getPhone() != null && s.getPhone().toLowerCase().contains(q))
                || (s.getEmail() != null && s.getEmail().toLowerCase().contains(q));
        });
    }

    @FXML
    private void handleAddSupplier() {
        openDialog(null);
    }

    private void openDialog(Supplier supplier) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/innovatewithomer/bizora/fxml/add-supplier-dialog.fxml"));
            Parent root = loader.load();

            AddSupplierController ctrl = loader.getController();
            if (supplier != null) ctrl.setSupplier(supplier);

            Stage stage = new Stage();
            stage.setTitle(supplier == null ? "Add Supplier" : "Edit Supplier");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadSuppliers();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open supplier dialog.", e);
        }
    }

    private void handleDelete(Supplier supplier) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Supplier");
        alert.setHeaderText("Delete " + supplier.getName() + "?");
        alert.setContentText("This action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                supplierService.deleteSupplier(supplier.getId());
                loadSuppliers();
            } catch (RuntimeException e) {
                showDeleteError("This supplier is linked to existing purchases and cannot be deleted.");
            }
        }
    }

    private void showDeleteError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Suppliers");
        alert.setHeaderText("Unable to delete supplier");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSearch() {
        applyFilter();
    }
}
