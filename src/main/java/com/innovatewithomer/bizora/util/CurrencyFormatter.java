package com.innovatewithomer.bizora.util;

import com.innovatewithomer.bizora.config.AppSettingsStore;

import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyFormatter {

    private static final ThreadLocal<NumberFormat> NUMBER_FORMAT =
            ThreadLocal.withInitial(() -> {
                NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
                formatter.setMinimumFractionDigits(2);
                formatter.setMaximumFractionDigits(2);
                return formatter;
            });

    private CurrencyFormatter() {
    }

    public static String format(double amount) {
        String symbol = AppSettingsStore.load().currencySymbol().trim();
        String separator = symbol.length() > 1 ? " " : "";
        return symbol + separator + NUMBER_FORMAT.get().format(amount);
    }
}
