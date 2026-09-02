package com.innovatewithomer.bizora.util;

import com.innovatewithomer.bizora.config.AppSettingsStore;

import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyFormatter {

    private CurrencyFormatter() {
    }

    public static String format(double amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);

        String symbol = AppSettingsStore.load().currencySymbol().trim();
        String separator = symbol.length() > 1 ? " " : "";
        return symbol + separator + numberFormat.format(amount);
    }
}
