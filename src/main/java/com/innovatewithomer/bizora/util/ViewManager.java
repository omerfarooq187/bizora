package com.innovatewithomer.bizora.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class ViewManager {

    private static final String FXML_PATH =
            "/com/innovatewithomer/bizora/fxml/";

    private final Pane contentArea;

    public ViewManager(Pane contentArea) {
        this.contentArea = contentArea;
    }

    public void show(String viewName) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(FXML_PATH + viewName)
            );

            Node view = loader.load();

            contentArea.getChildren().setAll(view);

        } catch (IOException | NullPointerException e) {
            throw new RuntimeException(
                    "Unable to load view: " + viewName, e
            );
        }
    }
}