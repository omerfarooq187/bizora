package com.innovatewithomer.bizora.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;


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
public class SalesController {


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


            FXMLLoader loader =
                    new FXMLLoader(
                            location
                    );


            Node view =
                    loader.load();


            /*
             * StackPane automatically resizes
             * its child to fill the available area.
             */
            salesContentArea
                    .getChildren()
                    .setAll(view);


        } catch (IOException e) {
            showLoadError(
                    resource,
                    e
            );
        }
    }


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
