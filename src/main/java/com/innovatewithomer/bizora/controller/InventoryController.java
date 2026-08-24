package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.InventoryMovement;
import com.innovatewithomer.bizora.model.Product;
import com.innovatewithomer.bizora.service.InventoryService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InventoryController {

    private final InventoryService inventoryService =
            AppContext.inventoryService();

    private final ObservableList<Product> products =
            FXCollections.observableArrayList();

    private final ObservableList<InventoryMovement> movements =
            FXCollections.observableArrayList();


    // =========================================================
    // PRODUCTS TABLE
    // =========================================================

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, Long> idColumn;

    @FXML
    private TableColumn<Product, String> nameColumn;

    @FXML
    private TableColumn<Product, String> skuColumn;

    @FXML
    private TableColumn<Product, Double> stockColumn;

    @FXML
    private TableColumn<Product, Double> sellingPriceColumn;


    // =========================================================
    // MOVEMENT TABLE
    // =========================================================

    @FXML
    private TableView<InventoryMovement> movementTable;

    @FXML
    private TableColumn<InventoryMovement, String> movementTypeColumn;

    @FXML
    private TableColumn<InventoryMovement, Double> quantityColumn;

    @FXML
    private TableColumn<InventoryMovement, String> referenceTypeColumn;

    @FXML
    private TableColumn<InventoryMovement, String> noteColumn;

    @FXML
    private TableColumn<InventoryMovement, LocalDateTime> dateColumn;


    // =========================================================
    // DETAILS
    // =========================================================

    @FXML
    private Label selectedProductLabel;

    @FXML
    private Label currentStockLabel;

    @FXML
    private Button adjustStockButton;


    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a"
            );


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    private void initialize() {

        configureProductTable();

        configureMovementTable();

        configureSelection();

        loadProducts();

        clearSelection();
    }


    // =========================================================
    // PRODUCT TABLE
    // =========================================================

    private void configureProductTable() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        skuColumn.setCellValueFactory(
                new PropertyValueFactory<>("sku")
        );

        stockColumn.setCellValueFactory(
                new PropertyValueFactory<>("stockQuantity")
        );

        sellingPriceColumn.setCellValueFactory(
                new PropertyValueFactory<>("sellingPrice")
        );

        productTable.setItems(products);
    }


    // =========================================================
    // MOVEMENT TABLE
    // =========================================================

    private void configureMovementTable() {

        movementTypeColumn.setCellValueFactory(
                new PropertyValueFactory<>("movementType")
        );

        quantityColumn.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        referenceTypeColumn.setCellValueFactory(
                new PropertyValueFactory<>("referenceType")
        );

        noteColumn.setCellValueFactory(
                new PropertyValueFactory<>("note")
        );

        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("createdAt")
        );

        movementTable.setItems(movements);
    }


    // =========================================================
    // PRODUCT SELECTION
    // =========================================================

    private void configureSelection() {

        productTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldProduct, newProduct) -> {

                            if (newProduct == null) {

                                clearSelection();

                            } else {

                                showProductDetails(
                                        newProduct
                                );

                                loadMovements(
                                        newProduct.getId()
                                );
                            }
                        }
                );
    }


    // =========================================================
    // LOAD PRODUCTS
    // =========================================================

    private void loadProducts() {

        List<Product> productList =
                AppContext.productService()
                        .getAllProducts();

        products.setAll(productList);
    }


    // =========================================================
    // LOAD MOVEMENTS
    // =========================================================

    private void loadMovements(
            Long productId
    ) {

        List<InventoryMovement> movementList =
                inventoryService.getMovements(
                        productId
                );

        movements.setAll(movementList);
    }


    // =========================================================
    // PRODUCT DETAILS
    // =========================================================

    private void showProductDetails(
            Product product
    ) {

        selectedProductLabel.setText(
                product.getName()
        );

        currentStockLabel.setText(
                String.valueOf(
                        product.getStockQuantity()
                )
        );

        adjustStockButton.setDisable(false);
    }

    private void selectProductById(Long productId) {

        if (productId == null) {
            clearSelection();
            return;
        }

        for (Product product : products) {

            if (productId.equals(product.getId())) {

                productTable
                        .getSelectionModel()
                        .select(product);

                productTable
                        .scrollTo(product);

                return;
            }
        }

        clearSelection();
    }


    private void clearSelection() {

        selectedProductLabel.setText(
                "No product selected"
        );

        currentStockLabel.setText(
                "—"
        );

        movements.clear();

        adjustStockButton.setDisable(true);
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @FXML
    private void handleRefresh() {

        Product selectedProduct =
                productTable
                        .getSelectionModel()
                        .getSelectedItem();

        Long selectedProductId =
                selectedProduct != null
                        ? selectedProduct.getId()
                        : null;

        loadProducts();

        selectProductById(
                selectedProductId
        );
    }



    // =========================================================
    // STOCK ADJUSTMENT
    // =========================================================

    @FXML
    private void handleAdjustStock() {

        Product selectedProduct =
                productTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selectedProduct == null) {
            showWarning("Please select a product first.");
            return;
        }

        Long selectedProductId =
                selectedProduct.getId();

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/innovatewithomer/bizora/fxml/adjust-stock-view.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            AdjustStockController controller =
                    loader.getController();

            controller.setProduct(
                    selectedProduct
            );

            Stage stage =
                    new Stage();

            stage.setTitle("Adjust Stock");

            stage.initModality(
                    Modality.APPLICATION_MODAL
            );

            stage.setResizable(false);

            stage.setScene(
                    new Scene(root)
            );

            stage.showAndWait();

            // Reload products so the new stock quantity appears.
            loadProducts();

            // Select the refreshed product.
            selectProductById(
                    selectedProductId
            );

        } catch (Exception e) {

            showWarning(
                    "Failed to open stock adjustment dialog."
            );

            e.printStackTrace();
        }
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

        alert.setTitle("Inventory");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }


    private void showInfo(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle("Inventory");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}