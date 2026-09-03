package com.innovatewithomer.bizora.config;

public record AppSettings(
        String businessName,
        String ownerName,
        String businessPhone,
        String businessEmail,
        String businessAddress,
        String currencySymbol,
        double taxRate,
        String invoicePrefix,
        String receiptFooter,
        boolean receiptPrintingEnabled,
        String receiptPrinterName,
        String receiptPaperWidth,
        boolean autoPrintReceipt
) {
    public static AppSettings defaults() {
        return new AppSettings(
                "Bizora",
                "",
                "",
                "",
                "",
                "$",
                0.0,
                "INV-",
                "Thank you for your business!",
                false,
                "",
                "80 mm",
                true
        );
    }
}
