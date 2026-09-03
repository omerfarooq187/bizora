package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.App;
import com.innovatewithomer.bizora.config.AppSettings;
import com.innovatewithomer.bizora.config.AppSettingsStore;
import com.innovatewithomer.bizora.service.BackupService;
import com.innovatewithomer.bizora.service.ReceiptPrinterService;
import com.innovatewithomer.bizora.util.RefreshableView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.scene.layout.VBox;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class SettingsController implements RefreshableView {

    private static final DateTimeFormatter BACKUP_TIME =
            DateTimeFormatter.ofPattern("MMM dd, yyyy  h:mm a");
    private static final ExecutorService SETTINGS_EXECUTOR = Executors.newFixedThreadPool(
            2,
            new SettingsThreadFactory()
    );
    private final BackupService backupService = new BackupService();
    private final ReceiptPrinterService receiptPrinterService = new ReceiptPrinterService();

    @FXML private TextField businessNameField;
    @FXML private TextField ownerNameField;
    @FXML private TextField businessPhoneField;
    @FXML private TextField businessEmailField;
    @FXML private TextArea  businessAddressField;
    @FXML private ComboBox<String> currencySymbolCombo;
    @FXML private TextField taxRateField;
    @FXML private TextField invoicePrefixField;
    @FXML private TextArea  receiptFooterField;
    @FXML private Label statusLabel;
    @FXML private Label aboutCopyrightLabel;
    @FXML private Label lastBackupLabel;
    @FXML private Label backupFolderLabel;
    @FXML private CheckBox receiptPrintingEnabledCheck;
    @FXML private CheckBox autoPrintReceiptCheck;
    @FXML private ComboBox<String> receiptPrinterCombo;
    @FXML private ComboBox<String> receiptPaperWidthCombo;
    @FXML private VBox printerSettingsBox;
    @FXML private ProgressIndicator printerProgressIndicator;
    @FXML private Button createBackupButton;
    @FXML private Button restoreBackupButton;
    @FXML private Button openBackupFolderButton;
    @FXML private ProgressIndicator backupProgressIndicator;
    @FXML private ImageView aboutLogoImage;

    private long printerRequestVersion;
    private long backupRequestVersion;

    @FXML
    private void initialize() {
        loadAboutLogo();
        currencySymbolCombo.setItems(FXCollections.observableArrayList(
                "$", "€", "£", "¥", "₹", "₨", "CHF", "CAD", "AUD"
        ));
        receiptPaperWidthCombo.setItems(FXCollections.observableArrayList(
                ReceiptPrinterService.PAPER_58_MM,
                ReceiptPrinterService.PAPER_80_MM
        ));
        receiptPrintingEnabledCheck.selectedProperty().addListener(
                (observable, oldValue, enabled) -> updatePrinterControls(enabled));
        aboutCopyrightLabel.setText(
                "© " + Year.now().getValue()
                        + " InnovateWithOmer. All rights reserved."
        );
        loadSettings();
        refreshPrinters(false);
        refreshBackupInfo();
    }

    private void loadAboutLogo() {
        var logo = SettingsController.class.getResource(
                "/com/innovatewithomer/bizora/images/bizora_logo.png"
        );
        if (logo != null) {
            // Decode close to its displayed size and do it without blocking the UI thread.
            aboutLogoImage.setImage(new Image(
                    logo.toExternalForm(), 216, 216, true, true, true
            ));
        }
    }

    private void loadSettings() {
        AppSettings settings = AppSettingsStore.load();
        businessNameField.setText(settings.businessName());
        ownerNameField.setText(settings.ownerName());
        businessPhoneField.setText(settings.businessPhone());
        businessEmailField.setText(settings.businessEmail());
        businessAddressField.setText(settings.businessAddress());
        currencySymbolCombo.setValue(settings.currencySymbol());
        taxRateField.setText(String.format("%.2f", settings.taxRate()));
        invoicePrefixField.setText(settings.invoicePrefix());
        receiptFooterField.setText(settings.receiptFooter());
        receiptPrintingEnabledCheck.setSelected(settings.receiptPrintingEnabled());
        autoPrintReceiptCheck.setSelected(settings.autoPrintReceipt());
        receiptPaperWidthCombo.setValue(settings.receiptPaperWidth());
        if (settings.receiptPrinterName() != null && !settings.receiptPrinterName().isBlank()
                && !receiptPrinterCombo.getItems().contains(settings.receiptPrinterName())) {
            receiptPrinterCombo.getItems().add(settings.receiptPrinterName());
        }
        if (settings.receiptPrinterName() != null && !settings.receiptPrinterName().isBlank()) {
            receiptPrinterCombo.setValue(settings.receiptPrinterName());
        }
        updatePrinterControls(settings.receiptPrintingEnabled());
        statusLabel.setText("");
    }

    @Override
    public void refreshView() {
        loadSettings();
        refreshBackupInfo();
    }

    @FXML
    private void handleSave() {
        try {
            AppSettingsStore.save(settingsFromForm());
        } catch (IllegalArgumentException exception) {
            showStatus(exception.getMessage(), false);
            return;
        }

        MainController mainController = MainController.getInstance();
        if (mainController != null) {
            mainController.refreshBusinessIdentity();
        }
        showStatus("✓ Settings saved successfully.", true);
    }

    private AppSettings settingsFromForm() {
        String name = businessNameField.getText().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Business name is required.");

        double tax;
        try {
            tax = Double.parseDouble(taxRateField.getText().trim());
            if (!Double.isFinite(tax) || tax < 0 || tax > 100) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Tax rate must be a number between 0 and 100.");
        }

        String currency = currencySymbolCombo.getValue();
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency symbol is required.");
        }
        String invoicePrefix = invoicePrefixField.getText().trim();
        if (invoicePrefix.isEmpty()) throw new IllegalArgumentException("Invoice prefix is required.");

        boolean printingEnabled = receiptPrintingEnabledCheck.isSelected();
        String phone = businessPhoneField.getText().trim();
        String printer = receiptPrinterCombo.getValue();
        String paper = receiptPaperWidthCombo.getValue();
        if (printingEnabled && phone.isEmpty()) {
            throw new IllegalArgumentException("Business phone number is required when receipt printing is enabled.");
        }
        if (printingEnabled && (printer == null || printer.isBlank())) {
            throw new IllegalArgumentException("Select an installed printer before enabling receipt printing.");
        }
        if (printingEnabled && paper == null) {
            throw new IllegalArgumentException("Select a receipt paper width.");
        }

        return new AppSettings(
                name,
                ownerNameField.getText().trim(),
                phone,
                businessEmailField.getText().trim(),
                businessAddressField.getText().trim(),
                currency,
                tax,
                invoicePrefix,
                receiptFooterField.getText().trim(),
                printingEnabled,
                printer == null ? "" : printer,
                paper == null ? ReceiptPrinterService.PAPER_80_MM : paper,
                autoPrintReceiptCheck.isSelected()
        );
    }

    @FXML
    private void handleRefreshPrinters() {
        refreshPrinters(true);
    }

    @FXML
    private void handleTestReceipt() {
        final AppSettings settings;
        try {
            settings = settingsFromForm();
            receiptPrinterService.validateConfiguration(settings);
        } catch (IllegalArgumentException exception) {
            showStatus(exception.getMessage(), false);
            return;
        }

        setPrinterBusy(true);
        showStatus("Sending test receipt to " + settings.receiptPrinterName() + "…", true);
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                receiptPrinterService.printTestReceipt(settings);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            setPrinterBusy(false);
            showStatus("✓ Test receipt sent successfully.", true);
        });
        task.setOnFailed(event -> {
            setPrinterBusy(false);
            showStatus(rootMessage(task.getException()), false);
        });
        Thread worker = new Thread(task, "bizora-test-receipt");
        worker.setDaemon(true);
        worker.start();
    }

    private void refreshPrinters(boolean announceResult) {
        String selected = receiptPrinterCombo == null ? null : receiptPrinterCombo.getValue();
        long requestVersion = ++printerRequestVersion;
        setPrinterBusy(true);
        Task<PrinterDiscovery> task = new Task<>() {
            @Override protected PrinterDiscovery call() {
                return new PrinterDiscovery(
                        receiptPrinterService.availablePrinters(),
                        receiptPrinterService.defaultPrinterName()
                );
            }
        };
        task.setOnSucceeded(event -> {
            if (requestVersion != printerRequestVersion) return;
            PrinterDiscovery discovery = task.getValue();
            List<String> printers = discovery.printers();
            receiptPrinterCombo.setItems(FXCollections.observableArrayList(printers));
            if (selected != null && printers.contains(selected)) {
                receiptPrinterCombo.setValue(selected);
            } else {
                receiptPrinterCombo.setValue(printers.contains(discovery.defaultPrinter())
                        ? discovery.defaultPrinter()
                        : printers.stream().findFirst().orElse(null));
            }
            setPrinterBusy(false);
            if (announceResult) {
                showStatus(printers.isEmpty()
                        ? "No printers were found. Install the printer in your operating system first."
                        : "Printer list refreshed.", !printers.isEmpty());
            }
        });
        task.setOnFailed(event -> {
            if (requestVersion != printerRequestVersion) return;
            setPrinterBusy(false);
            if (announceResult) showStatus("Could not refresh installed printers.", false);
        });
        SETTINGS_EXECUTOR.execute(task);
    }

    private void updatePrinterControls(boolean enabled) {
        printerSettingsBox.setDisable(!enabled);
        printerSettingsBox.setOpacity(enabled ? 1 : 0.58);
    }

    private void setPrinterBusy(boolean busy) {
        printerProgressIndicator.setVisible(busy);
        printerProgressIndicator.setManaged(busy);
        printerSettingsBox.setDisable(busy || !receiptPrintingEnabledCheck.isSelected());
    }

    @FXML
    private void handleReset() {
        AppSettingsStore.reset();
        loadSettings();
        MainController mainController = MainController.getInstance();
        if (mainController != null) {
            mainController.refreshBusinessIdentity();
        }
        showStatus("Settings reset to defaults.", true);
    }

    @FXML
    private void handleOpenDeveloperWebsite() {
        App.openWebsite("https://innovatewithomer.dev");
    }

    @FXML
    private void handleCreateBackup() {
        setBackupBusy(true);
        showStatus("Creating backup…", true);
        Task<Path> task = new Task<>() {
            @Override protected Path call() {
                return backupService.createBackup();
            }
        };
        task.setOnSucceeded(event -> {
            setBackupBusy(false);
            refreshBackupInfo();
            showStatus("✓ Backup created: " + task.getValue().getFileName(), true);
        });
        task.setOnFailed(event -> {
            setBackupBusy(false);
            showStatus("Backup failed: " + rootMessage(task.getException()), false);
        });
        SETTINGS_EXECUTOR.execute(task);
    }

    @FXML
    private void handleOpenBackupFolder() {
        try {
            Files.createDirectories(backupService.getBackupDirectory());
            App.openWebsite(backupService.getBackupDirectory().toUri().toString());
        } catch (Exception exception) {
            showStatus("Could not open the backup folder.", false);
        }
    }

    @FXML
    private void handleRestoreBackup() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Restore Bizora Backup");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Bizora database backup", "*.db"));
        if (Files.isDirectory(backupService.getBackupDirectory())) {
            chooser.setInitialDirectory(backupService.getBackupDirectory().toFile());
        }
        java.io.File selected = chooser.showOpenDialog(statusLabel.getScene().getWindow());
        if (selected == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Restore Backup");
        confirmation.setHeaderText("Replace current data with this backup?");
        confirmation.setContentText(
                "Bizora will first create a safety backup of your current data, then restore the selected file. "
                        + "The app will close afterward so the restored data can load safely.");
        Optional<ButtonType> choice = confirmation.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.OK) return;

        setBackupBusy(true);
        showStatus("Restoring backup…", true);
        Task<Path> task = new Task<>() {
            @Override protected Path call() {
                return backupService.restoreBackup(selected.toPath());
            }
        };
        task.setOnSucceeded(event -> {
            setBackupBusy(false);
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Backup Restored");
            success.setHeaderText("Your data was restored successfully.");
            success.setContentText("Restart Bizora to continue with the restored data.");
            success.showAndWait();
            Platform.exit();
        });
        task.setOnFailed(event -> {
            setBackupBusy(false);
            showStatus("Restore failed: " + rootMessage(task.getException()), false);
        });
        SETTINGS_EXECUTOR.execute(task);
    }

    private void refreshBackupInfo() {
        Path folder = backupService.getBackupDirectory();
        backupFolderLabel.setText(folder.toString());
        long requestVersion = ++backupRequestVersion;
        Task<BackupInfo> task = new Task<>() {
            @Override protected BackupInfo call() {
                List<Path> backups = backupService.listBackups();
                if (backups.isEmpty()) return new BackupInfo(null, null);
                Path latest = backups.get(0);
                try {
                    return new BackupInfo(latest, Files.getLastModifiedTime(latest).toInstant());
                } catch (Exception ignored) {
                    return new BackupInfo(latest, null);
                }
            }
        };
        task.setOnSucceeded(event -> {
            if (requestVersion != backupRequestVersion) return;
            BackupInfo info = task.getValue();
            if (info.latest() == null) {
                lastBackupLabel.setText("No backup created yet");
            } else if (info.modified() == null) {
                lastBackupLabel.setText(info.latest().getFileName().toString());
            } else {
                lastBackupLabel.setText(info.latest().getFileName() + "  ·  "
                        + BACKUP_TIME.format(info.modified().atZone(ZoneId.systemDefault())));
            }
        });
        task.setOnFailed(event -> {
            if (requestVersion == backupRequestVersion) {
                lastBackupLabel.setText("Backup information unavailable");
            }
        });
        SETTINGS_EXECUTOR.execute(task);
    }

    private void setBackupBusy(boolean busy) {
        backupProgressIndicator.setVisible(busy);
        backupProgressIndicator.setManaged(busy);
        createBackupButton.setDisable(busy);
        restoreBackupButton.setDisable(busy);
        openBackupFolderButton.setDisable(busy);
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? "Unknown error" : current.getMessage();
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setStyle(success
                ? "-fx-text-fill:#10b981; -fx-font-size:13px; -fx-font-weight:bold;"
                : "-fx-text-fill:#ef4444; -fx-font-size:13px; -fx-font-weight:bold;");
    }

    private record PrinterDiscovery(List<String> printers, String defaultPrinter) { }

    private record BackupInfo(Path latest, Instant modified) { }

    private static final class SettingsThreadFactory implements ThreadFactory {
        private int sequence;

        @Override
        public synchronized Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "bizora-settings-worker-" + (++sequence));
            thread.setDaemon(true);
            return thread;
        }
    }
}
