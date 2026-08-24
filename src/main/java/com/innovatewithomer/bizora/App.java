package com.innovatewithomer.bizora;

import com.innovatewithomer.bizora.config.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        DatabaseInitializer.initialize();

        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/innovatewithomer/bizora/fxml/main-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 1200, 750);

        stage.setTitle("Bizora");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}