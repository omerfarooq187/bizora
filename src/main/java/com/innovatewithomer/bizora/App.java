package com.innovatewithomer.bizora;

import com.innovatewithomer.bizora.config.DatabaseInitializer;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class App extends Application {

    private static HostServices appHostServices;

    @Override
    public void start(Stage stage) throws IOException {

        DatabaseInitializer.initialize();
        appHostServices = getHostServices();
        installGlobalWindowTheme();

        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/innovatewithomer/bizora/fxml/main-view.fxml")
        );

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double initialWidth = Math.min(1440, Math.max(960, screen.getWidth() * 0.90));
        double initialHeight = Math.min(900, Math.max(640, screen.getHeight() * 0.90));

        Scene scene = new Scene(loader.load(), initialWidth, initialHeight);

        stage.setTitle("Bizora — Business Management");
        stage.getIcons().add(new Image(
                App.class.getResourceAsStream(
                        "/com/innovatewithomer/bizora/images/bizora_logo.png"
                )
        ));
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.centerOnScreen();
        stage.show();
    }

    public static void openWebsite(String url) {
        if (appHostServices != null && url != null && !url.isBlank()) {
            appHostServices.showDocument(url);
        }
    }

    private void installGlobalWindowTheme() {
        String stylesheet = App.class.getResource(
                "/com/innovatewithomer/bizora/css/main.css"
        ).toExternalForm();

        Window.getWindows().addListener((ListChangeListener<Window>) change -> {
            while (change.next()) {
                if (!change.wasAdded()) {
                    continue;
                }
                for (Window window : change.getAddedSubList()) {
                    if (window.getScene() != null
                            && !window.getScene().getStylesheets().contains(stylesheet)) {
                        window.getScene().getStylesheets().add(stylesheet);
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
