package com.innovatewithomer.bizora.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import com.innovatewithomer.bizora.util.RefreshableView;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * Controller for the main Sales screen.
 *
 * This screen acts as a container for:
 *
 *  - New Sale
 *  - Sales History
 *
 * The actual POS logic is handled by NewSaleController.
 * Sales history logic is handled by SalesHistoryController.
 */
public class SalesController implements RefreshableView {

    private final Map<String, LoadedView> viewCache = new HashMap<>();
    private String currentResource;


    // =========================================================
    // FXML
    // =========================================================

    @FXML
    private StackPane salesContentArea;

    @FXML
    private Button newSaleButton;

    @FXML
    private Button salesHistoryButton;


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    private void initialize() {

        /*
         * Open New Sale automatically when
         * the Sales section is opened.
         */
        showNewSale();
    }


    // =========================================================
    // NEW SALE
    // =========================================================

    /**
     * Loads the New Sale screen.
     *
     * This method is intentionally public to FXML
     * through @FXML because sales-view.fxml calls:
     *
     * onAction="#showNewSale"
     */
    @FXML
    private void showNewSale() {

        loadView(
                "/com/innovatewithomer/bizora/fxml/new-sale-view.fxml"
        );

        updateNavigationState(
                true,
                false
        );
    }


    // =========================================================
    // SALES HISTORY
    // =========================================================

    /**
     * Loads the Sales History screen.
     *
     * This method is intentionally public to FXML
     * through @FXML because sales-view.fxml calls:
     *
     * onAction="#showSalesHistory"
     */
    @FXML
    private void showSalesHistory() {

        loadView(
                "/com/innovatewithomer/bizora/fxml/sales-history-view.fxml"
        );

        updateNavigationState(
                false,
                true
        );
    }


    // =========================================================
    // LOAD VIEW
    // =========================================================

    private void loadView(
            String resource
    ) {

        try {

            URL location =
                    getClass()
                            .getResource(resource);


            /*
             * Give a clear error if the FXML file
             * does not exist at the specified path.
             */
            if (location == null) {

                throw new IOException(
                        "FXML resource not found:\n"
                                + resource
                );
            }


            LoadedView loaded = viewCache.get(resource);
            if (loaded == null) {
                FXMLLoader loader = new FXMLLoader(location);
                Node view = loader.load();
                loaded = new LoadedView(view, loader.getController());
                viewCache.put(resource, loaded);
            } else if (loaded.controller() instanceof RefreshableView refreshable) {
                refreshable.refreshView();
            }


            /*
             * StackPane automatically resizes
             * its child to fill the available area.
             */
            salesContentArea
                    .getChildren()
                    .setAll(loaded.view());

            currentResource = resource;


        } catch (IOException e) {
            showLoadError(
                    resource,
                    e
            );
        }
    }

    @Override
    public void refreshView() {
        if (currentResource == null) return;
        LoadedView loaded = viewCache.get(currentResource);
        if (loaded != null && loaded.controller() instanceof RefreshableView refreshable) {
            refreshable.refreshView();
        }
    }

    private record LoadedView(Node view, Object controller) { }


    // =========================================================
    // NAVIGATION STATE
    // =========================================================

    private void updateNavigationState(
            boolean newSaleActive,
            boolean historyActive
    ) {

        /*
         * Remove active class first.
         */
        if (newSaleButton != null) {

            newSaleButton
                    .getStyleClass()
                    .remove(
                            "active-nav-button"
                    );

            if (newSaleActive) {

                newSaleButton
                        .getStyleClass()
                        .add(
                                "active-nav-button"
                        );
            }
        }


        /*
         * Remove active class first.
         */
        if (salesHistoryButton != null) {

            salesHistoryButton
                    .getStyleClass()
                    .remove(
                            "active-nav-button"
                    );

            if (historyActive) {

                salesHistoryButton
                        .getStyleClass()
                        .add(
                                "active-nav-button"
                        );
            }
        }
    }


    // =========================================================
    // ERROR
    // =========================================================

    private void showLoadError(
            String resource,
            Exception exception
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "Sales"
        );

        alert.setHeaderText(
                "Unable to load sales view"
        );

        String message =
                "Could not load:\n"
                        + resource
                        + "\n\n"
                        + exception.getMessage();

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }
}
