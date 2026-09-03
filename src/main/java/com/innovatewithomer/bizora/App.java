package com.innovatewithomer.bizora;

import com.innovatewithomer.bizora.config.DatabaseInitializer;
import com.innovatewithomer.bizora.service.BackupService;
import com.innovatewithomer.bizora.util.AppLogger;
import com.innovatewithomer.bizora.util.SingleInstanceLock;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.concurrent.atomic.AtomicBoolean;

public class App extends Application {

    private static HostServices appHostServices;
    private static final AtomicBoolean ERROR_DIALOG_VISIBLE = new AtomicBoolean();
    private SingleInstanceLock instanceLock;

    @Override
    public void start(Stage stage) {
        AppLogger.initialize();
        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
        try {
            instanceLock = SingleInstanceLock.tryAcquire();
            if (instanceLock == null) {
                showAlreadyRunningMessage();
                Platform.exit();
                return;
            }
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

            createAutomaticBackupInBackground();
        } catch (Throwable error) {
            AppLogger.error("Bizora could not start.", error);
            showFatalError(
                    "Bizora could not start",
                    "Your business data was not changed. "
                            + "Please restart Bizora or contact support.",
                    error
            );
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        if (instanceLock != null) {
            instanceLock.close();
            instanceLock = null;
        }
    }

    private void createAutomaticBackupInBackground() {
        Thread worker = new Thread(() -> {
            try {
                new BackupService().createDailyBackupIfNeeded();
            } catch (RuntimeException exception) {
                AppLogger.warning("Automatic backup could not be created.", exception);
            }
        }, "bizora-automatic-backup");
        worker.setDaemon(true);
        worker.start();
    }

    private void handleUncaughtException(Thread thread, Throwable error) {
        AppLogger.error("Unexpected error on thread " + thread.getName() + ".", error);
        Runnable showError = () -> showFatalError(
                "Something went wrong",
                "Bizora recorded the error. Your saved business data remains on this computer.",
                error
        );
        if (Platform.isFxApplicationThread()) showError.run();
        else Platform.runLater(showError);
    }

    private void showFatalError(String title, String message, Throwable error) {
        if (!ERROR_DIALOG_VISIBLE.compareAndSet(false, true)) return;
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(title);
            String detail = error == null || error.getMessage() == null
                    ? ""
                    : "\n\nDetails: " + error.getMessage();
            String logHint = AppLogger.getLogDirectory() == null
                    ? ""
                    : "\nLogs: " + AppLogger.getLogDirectory();
            alert.setContentText(message + detail + logHint);
            alert.showAndWait();
        } finally {
            ERROR_DIALOG_VISIBLE.set(false);
        }
    }

    private void showAlreadyRunningMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bizora is already running");
        alert.setHeaderText("Bizora is already open");
        alert.setContentText(
                "Use the existing Bizora window. Only one copy can run at a time "
                        + "to protect your business data."
        );
        alert.showAndWait();
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
