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
        String receiptFooter
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
                "Thank you for your business!"
        );
    }
}
