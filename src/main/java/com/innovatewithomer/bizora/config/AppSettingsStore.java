package com.innovatewithomer.bizora.config;

import java.util.prefs.Preferences;

public final class AppSettingsStore {

    private static final Preferences PREFERENCES =
            Preferences.userNodeForPackage(AppSettingsStore.class);
    private static volatile AppSettings cachedSettings;

    private AppSettingsStore() {
    }

    public static AppSettings load() {
        AppSettings settings = cachedSettings;
        if (settings != null) return settings;

        synchronized (AppSettingsStore.class) {
            if (cachedSettings == null) cachedSettings = readSettings();
            return cachedSettings;
        }
    }

    private static AppSettings readSettings() {
        AppSettings defaults = AppSettings.defaults();

        return new AppSettings(
                PREFERENCES.get("businessName", defaults.businessName()),
                PREFERENCES.get("ownerName", defaults.ownerName()),
                PREFERENCES.get("businessPhone", defaults.businessPhone()),
                PREFERENCES.get("businessEmail", defaults.businessEmail()),
                PREFERENCES.get("businessAddress", defaults.businessAddress()),
                PREFERENCES.get("currencySymbol", defaults.currencySymbol()),
                PREFERENCES.getDouble("taxRate", defaults.taxRate()),
                PREFERENCES.get("invoicePrefix", defaults.invoicePrefix()),
                PREFERENCES.get("receiptFooter", defaults.receiptFooter()),
                PREFERENCES.getBoolean("receiptPrintingEnabled", defaults.receiptPrintingEnabled()),
                PREFERENCES.get("receiptPrinterName", defaults.receiptPrinterName()),
                PREFERENCES.get("receiptPaperWidth", defaults.receiptPaperWidth()),
                PREFERENCES.getBoolean("autoPrintReceipt", defaults.autoPrintReceipt())
        );
    }

    public static void save(AppSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Settings cannot be null.");
        }

        PREFERENCES.put("businessName", settings.businessName());
        PREFERENCES.put("ownerName", settings.ownerName());
        PREFERENCES.put("businessPhone", settings.businessPhone());
        PREFERENCES.put("businessEmail", settings.businessEmail());
        PREFERENCES.put("businessAddress", settings.businessAddress());
        PREFERENCES.put("currencySymbol", settings.currencySymbol());
        PREFERENCES.putDouble("taxRate", settings.taxRate());
        PREFERENCES.put("invoicePrefix", settings.invoicePrefix());
        PREFERENCES.put("receiptFooter", settings.receiptFooter());
        PREFERENCES.putBoolean("receiptPrintingEnabled", settings.receiptPrintingEnabled());
        PREFERENCES.put("receiptPrinterName", settings.receiptPrinterName());
        PREFERENCES.put("receiptPaperWidth", settings.receiptPaperWidth());
        PREFERENCES.putBoolean("autoPrintReceipt", settings.autoPrintReceipt());
        cachedSettings = settings;
    }

    public static void reset() {
        save(AppSettings.defaults());
    }
}
