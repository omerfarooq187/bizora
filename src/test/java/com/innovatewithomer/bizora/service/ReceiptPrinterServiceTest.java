package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.config.AppSettings;
import com.innovatewithomer.bizora.model.PaymentMethod;
import com.innovatewithomer.bizora.model.Sale;
import com.innovatewithomer.bizora.model.SaleItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptPrinterServiceTest {

    private final ReceiptPrinterService service = new ReceiptPrinterService();

    @Test
    void shouldBuildCompact58MillimetreReceiptWithDeveloperPromotion() {
        AppSettings settings = settings(ReceiptPrinterService.PAPER_58_MM, "");
        Sale sale = sale();

        var lines = service.receiptPreview(
                settings, sale, Map.of(1L, "Extra Long Premium Tea Product"),
                "Walk-in Customer", 570, PaymentMethod.CASH);

        assertTrue(lines.contains("Omer Corner Shop"));
        assertTrue(lines.stream().anyMatch(line -> line.contains("INV-1001")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("TOTAL")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("innovatewithomer.dev")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("null")));
    }

    @Test
    void shouldIncludeOptionalEmailAndUseWider80MillimetreLayout() {
        AppSettings settings = settings(ReceiptPrinterService.PAPER_80_MM, "shop@example.com");

        var lines = service.receiptPreview(
                settings, sale(), Map.of(1L, "Tea"),
                "Ali", 570, PaymentMethod.CARD);

        assertTrue(lines.contains("shop@example.com"));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Customer: Ali")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Payment: Card")));
        assertTrue(lines.stream().anyMatch(line -> line.length() == 42 && line.chars().allMatch(ch -> ch == '-')));
    }

    @Test
    void shouldRejectDisabledPrintingBeforeTryingPrinterHardware() {
        AppSettings disabled = new AppSettings(
                "Shop", "", "03001234567", "", "", "Rs", 0,
                "INV-", "Thanks", false, "", ReceiptPrinterService.PAPER_80_MM, true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> service.validateConfiguration(disabled));
        assertTrue(exception.getMessage().contains("disabled"));
    }

    @Test
    void shouldRequirePhoneWhenPrintingIsEnabled() {
        AppSettings missingPhone = new AppSettings(
                "Shop", "", "", "", "", "Rs", 0,
                "INV-", "Thanks", true, "Printer", ReceiptPrinterService.PAPER_80_MM, true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> service.validateConfiguration(missingPhone));
        assertTrue(exception.getMessage().contains("phone"));
    }

    private AppSettings settings(String paper, String email) {
        return new AppSettings(
                "Omer Corner Shop", "Omer", "0300 1234567", email,
                "Main Market", "Rs", 0, "INV-", "Thank you!",
                true, "Test Printer", paper, true);
    }

    private Sale sale() {
        Sale sale = new Sale("INV-1001");
        sale.setCreatedAt(LocalDateTime.of(2026, 9, 3, 14, 30));
        sale.addItem(new SaleItem(1L, 2, 300, 30));
        sale.setDiscount(20);
        sale.setTax(20);
        return sale;
    }
}
