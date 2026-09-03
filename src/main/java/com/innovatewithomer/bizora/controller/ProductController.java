package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.Product;
import com.innovatewithomer.bizora.service.ProductService;
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

public class ProductController implements RefreshableView {

    private final ProductService productService = AppContext.productService();

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    
    private FilteredList<Product> filteredProducts;

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, String> skuColumn;

    @FXML
    private TableColumn<Product, String> nameColumn;

    @FXML
    private TableColumn<Product, Double> purchasePriceColumn;

    @FXML
    private TableColumn<Product, Double> sellingPriceColumn;

    @FXML
    private TableColumn<Product, Double> stockColumn;

    @FXML
    private TableColumn<Product, Void> actionsColumn;

    @FXML
    private TextField searchField;


    @FXML
    private void initialize() {

        configureTable();

        configureSearch();

        loadProducts();
    }

    private void configureTable() {

        skuColumn.setCellValueFactory(
                new PropertyValueFactory<>("sku")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        purchasePriceColumn.setCellValueFactory(
                new PropertyValueFactory<>("purchasePrice")
        );

        sellingPriceColumn.setCellValueFactory(
                new PropertyValueFactory<>("sellingPrice")
        );

        stockColumn.setCellValueFactory(
                new PropertyValueFactory<>("stockQuantity")
        );

        configureActionsColumn();
    }

    private void configureActionsColumn() {

        actionsColumn.setCellFactory(column ->
                new TableCell<>() {

                    private final Button editButton =
                            new Button("Edit");

                    private final Button deleteButton =
                            new Button("Delete");

                    private final HBox container =
                            new HBox(
                                    8,
                                    editButton,
                                    deleteButton
                            );

                    {
                        editButton.getStyleClass()
                                .add("secondary-button");

                        deleteButton.getStyleClass()
                                .add("danger-button");

                        editButton.setOnAction(event -> {

                            Product product =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleEditProduct(product);
                        });

                        deleteButton.setOnAction(event -> {

                            Product product =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleDeleteProduct(product);
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty
                    ) {

                        super.updateItem(item, empty);

                        setGraphic(
                                empty ? null : container
                        );
                    }
                }
        );
    }

    private void configureSearch() {

        searchField.textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                applySearchFilter()
                );
    }

    private void loadProducts() {

        productList.setAll(
                productService.getAllProducts()
        );

        if (filteredProducts == null) {

            filteredProducts =
                    new FilteredList<>(
                            productList,
                            product -> true
                    );

            productTable.setItems(
                    filteredProducts
            );
        }

        applySearchFilter();
    }

    @Override
    public void refreshView() {
        loadProducts();
    }

    private void applySearchFilter() {

        String searchText =
                searchField
                        .getText()
                        .trim()
                        .toLowerCase();

        filteredProducts.setPredicate(product -> {

            if (searchText.isEmpty()) {
                return true;
            }

            return product.getName()
                    .toLowerCase()
                    .contains(searchText)

                    || (product.getSku() != null
                    && product.getSku()
                            .toLowerCase()
                            .contains(searchText));
        });
    }

    @FXML
    private void handleAddProduct() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/innovatewithomer/bizora/fxml/add-product-dialog.fxml"
                            )
                    );

            Parent root = loader.load();

            Stage stage = new Stage();

            stage.setTitle("Add Product");

            stage.setScene(
                    new Scene(root)
            );

            stage.initModality(
                    Modality.APPLICATION_MODAL
            );

            stage.showAndWait();

            loadProducts();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to open Add Product dialog.",
                    e
            );
        }
    }

    private void handleEditProduct(Product product) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/innovatewithomer/bizora/fxml/add-product-dialog.fxml"
                            )
                    );

            Parent root = loader.load();

            AddProductController controller =
                    loader.getController();

            controller.setProduct(product);

            Stage stage = new Stage();

            stage.setTitle("Edit Product");

            stage.setScene(
                    new Scene(root)
            );

            stage.initModality(
                    Modality.APPLICATION_MODAL
            );

            stage.showAndWait();

            loadProducts();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to open Edit Product dialog.",
                    e
            );
        }
    }

    private void handleDeleteProduct(Product product) {

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle("Delete Product");
        confirmation.setHeaderText(
                "Delete " + product.getName() + "?"
        );

        confirmation.setContentText(
                "This action cannot be undone."
        );

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isPresent()
                && result.get() == ButtonType.OK) {
            try {
                productService.deleteProduct(product.getId());
                loadProducts();
            } catch (RuntimeException e) {
                showDeleteError(
                        "This product is used by sales, purchases, or inventory records and cannot be deleted."
                );
            }
        }
    }

    private void showDeleteError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Products");
        alert.setHeaderText("Unable to delete product");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
