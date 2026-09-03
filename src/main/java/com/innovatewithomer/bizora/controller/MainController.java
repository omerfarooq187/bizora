package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.App;
import com.innovatewithomer.bizora.config.AppSettings;
import com.innovatewithomer.bizora.config.AppSettingsStore;
import com.innovatewithomer.bizora.util.ViewManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.time.Year;

public class MainController {

    private static final double COMPACT_BREAKPOINT = 1180;

    @FXML
    private BorderPane rootPane;

    @FXML
    private VBox sidebar;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button productsButton;

    @FXML
    private Button inventoryButton;

    @FXML
    private Button purchasesButton;

    @FXML
    private Button salesButton;

    @FXML
    private Button customersButton;

    @FXML
    private Button suppliersButton;

    @FXML
    private Button expensesButton;

    @FXML
    private Button reportsButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Label businessNameLabel;

    @FXML private Label overviewLabel;
    @FXML private Label operationsLabel;
    @FXML private Label preferencesLabel;
    @FXML private Label copyrightLabel;
    @FXML private VBox developerFooter;
    @FXML private ImageView brandLogoImage;

    private ViewManager viewManager;
    private Boolean compactMode;
    private Boolean footerMode;

    private static MainController instance;

    public static MainController getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {
        instance = this;

        viewManager =
                new ViewManager(contentArea);

        refreshBusinessIdentity();
        configureBranding();
        configureResponsiveLayout();
        showDashboard();
    }

    private void configureBranding() {
        brandLogoImage.setViewport(
                new Rectangle2D(245, 20, 770, 825)
        );
        copyrightLabel.setText(
                "© " + Year.now().getValue() + " InnovateWithOmer"
        );
    }

    public void refreshBusinessIdentity() {
        AppSettings settings = AppSettingsStore.load();
        businessNameLabel.setText(settings.businessName().toUpperCase());
    }

    private void configureResponsiveLayout() {
        rootPane.widthProperty().addListener(
                (observable, oldWidth, newWidth) ->
                        applyResponsiveLayout(newWidth.doubleValue())
        );
        rootPane.heightProperty().addListener(
                (observable, oldHeight, newHeight) ->
                        applyResponsiveLayout(rootPane.getWidth())
        );
        applyResponsiveLayout(rootPane.getPrefWidth());
    }

    private void applyResponsiveLayout(double width) {
        boolean compact = width > 0 && width < COMPACT_BREAKPOINT;
        boolean compactChanged = compactMode == null || compactMode != compact;

        if (compactChanged) {
            compactMode = compact;

            sidebar.setPrefWidth(compact ? 82 : 238);
            sidebar.setMinWidth(compact ? 82 : 180);

            setManagedAndVisible(overviewLabel, !compact);
            setManagedAndVisible(operationsLabel, !compact);
            setManagedAndVisible(preferencesLabel, !compact);

            for (Button button : navigationButtons()) {
                button.setContentDisplay(
                        compact ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.LEFT
                );
                button.setAlignment(compact ? Pos.CENTER : Pos.CENTER_LEFT);
                button.setTooltip(compact ? new Tooltip(button.getText()) : null);
            }

            rootPane.getStyleClass().removeAll("app-compact", "app-wide");
            rootPane.getStyleClass().add(compact ? "app-compact" : "app-wide");
        }

        boolean showFooter = !compact && rootPane.getHeight() >= 800;
        if (footerMode == null || footerMode != showFooter) {
            footerMode = showFooter;
            developerFooter.setVisible(showFooter);
            developerFooter.setManaged(showFooter);
        }
    }

    private void setManagedAndVisible(Label label, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
    }

    @FXML
    public void showDashboard() {

        setActiveButton(dashboardButton);

        viewManager.show("dashboard-view.fxml");
    }

    @FXML
    public void showProducts() {

        setActiveButton(productsButton);

        viewManager.show("products-view.fxml");
    }

    @FXML
    public void showInventory() {

        setActiveButton(inventoryButton);

        viewManager.show("inventory-view.fxml");
    }

    @FXML
    public void showPurchases() {

        setActiveButton(purchasesButton);

        viewManager.show("purchases-view.fxml");
    }

    @FXML
    public void showSales() {

        setActiveButton(salesButton);

        viewManager.show("sales-view.fxml");
    }

    @FXML
    public void showCustomers() {

        setActiveButton(customersButton);

        viewManager.show("customers-view.fxml");
    }

    @FXML
    public void showSuppliers() {

        setActiveButton(suppliersButton);

        viewManager.show("suppliers-view.fxml");
    }

    @FXML
    public void showExpenses() {

        setActiveButton(expensesButton);

        viewManager.show("expenses-view.fxml");
    }

    @FXML
    public void showReports() {

        setActiveButton(reportsButton);

        viewManager.show("reports-view.fxml");
    }

    @FXML
    public void showSettings() {

        setActiveButton(settingsButton);

        viewManager.show("settings-view.fxml");
    }

    @FXML
    private void handleOpenDeveloperWebsite() {
        App.openWebsite("https://innovatewithomer.dev");
    }

    private void setActiveButton(
            Button activeButton
    ) {

        for (Button button : navigationButtons()) {

            button.getStyleClass()
                    .remove("nav-button-active");
        }

        if (!activeButton.getStyleClass()
                .contains("nav-button-active")) {

            activeButton.getStyleClass()
                    .add("nav-button-active");
        }
    }

    private List<Button> navigationButtons() {
        return List.of(
                dashboardButton,
                productsButton,
                inventoryButton,
                purchasesButton,
                salesButton,
                customersButton,
                suppliersButton,
                expensesButton,
                reportsButton,
                settingsButton
        );
    }
}
