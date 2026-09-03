package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.config.AppSettings;
import com.innovatewithomer.bizora.model.PaymentMethod;
import com.innovatewithomer.bizora.model.Sale;
import com.innovatewithomer.bizora.model.SaleItem;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

public class ReceiptPrinterService {

    public static final String PAPER_58_MM = "58 mm";
    public static final String PAPER_80_MM = "80 mm";
    private static final String DEVELOPER_PROMOTION =
            "Powered by Bizora - innovatewithomer.dev";
    private static final DateTimeFormatter RECEIPT_DATE =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public List<String> availablePrinters() {
        return Arrays.stream(PrintServiceLookup.lookupPrintServices(null, null))
                .map(PrintService::getName)
                .filter(Objects::nonNull)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public String defaultPrinterName() {
        PrintService printer = PrintServiceLookup.lookupDefaultPrintService();
        return printer == null ? "" : printer.getName();
    }

    public void validateConfiguration(AppSettings settings) {
        if (settings == null) throw new IllegalArgumentException("Printer settings are unavailable.");
        if (!settings.receiptPrintingEnabled()) {
            throw new IllegalArgumentException("Receipt printing is disabled. Enable it in Settings first.");
        }
        if (blank(settings.businessName())) {
            throw new IllegalArgumentException("Business name is required before printing receipts.");
        }
        if (blank(settings.businessPhone())) {
            throw new IllegalArgumentException("Business phone number is required before printing receipts.");
        }
        if (blank(settings.receiptPrinterName())) {
            throw new IllegalArgumentException("Select a receipt printer in Settings.");
        }
        if (!PAPER_58_MM.equals(settings.receiptPaperWidth())
                && !PAPER_80_MM.equals(settings.receiptPaperWidth())) {
            throw new IllegalArgumentException("Select either 58 mm or 80 mm receipt paper.");
        }
        if (findPrinter(settings.receiptPrinterName()) == null) {
            throw new IllegalArgumentException(
                    "Printer '" + settings.receiptPrinterName() + "' is not currently available.");
        }
    }

    public void printReceipt(
            AppSettings settings,
            Sale sale,
            Map<Long, String> productNames,
            String customerName,
            double amountPaid,
            PaymentMethod paymentMethod
    ) {
        validateConfiguration(settings);
        if (sale == null) throw new IllegalArgumentException("Sale receipt data is unavailable.");
        List<ReceiptLine> lines = buildReceipt(
                settings, sale, productNames, customerName, amountPaid, paymentMethod);
        print(settings, lines, "Receipt " + safe(sale.getInvoiceNumber()));
    }

    public void printTestReceipt(AppSettings settings) {
        validateConfiguration(settings);
        Sale sample = new Sale("TEST-RECEIPT");
        sample.setCreatedAt(LocalDateTime.now());
        sample.addItem(new SaleItem(1L, 2, 125, 0));
        sample.setDiscount(0);
        sample.setTax(0);
        printReceipt(settings, sample, Map.of(1L, "Sample Product"),
                "Walk-in Customer", sample.getTotal(), PaymentMethod.CASH);
    }

    public List<String> receiptPreview(
            AppSettings settings,
            Sale sale,
            Map<Long, String> productNames,
            String customerName,
            double amountPaid,
            PaymentMethod paymentMethod
    ) {
        return buildReceipt(settings, sale, productNames, customerName, amountPaid, paymentMethod)
                .stream().map(ReceiptLine::text).toList();
    }

    private List<ReceiptLine> buildReceipt(
            AppSettings settings,
            Sale sale,
            Map<Long, String> productNames,
            String customerName,
            double amountPaid,
            PaymentMethod paymentMethod
    ) {
        int columns = PAPER_58_MM.equals(settings.receiptPaperWidth()) ? 30 : 42;
        List<ReceiptLine> lines = new ArrayList<>();
        lines.add(new ReceiptLine(settings.businessName(), Align.CENTER, true, 1));
        lines.add(new ReceiptLine(settings.businessPhone(), Align.CENTER, false, 0));
        if (!blank(settings.businessEmail())) {
            lines.add(new ReceiptLine(settings.businessEmail(), Align.CENTER, false, 0));
        }
        if (!blank(settings.businessAddress())) {
            wrap(settings.businessAddress(), columns).forEach(
                    text -> lines.add(new ReceiptLine(text, Align.CENTER, false, 0)));
        }
        lines.add(separator(columns));
        lines.add(new ReceiptLine("SALES RECEIPT", Align.CENTER, true, 1));
        lines.add(new ReceiptLine("Invoice: " + safe(sale.getInvoiceNumber()), Align.LEFT, false, 0));
        LocalDateTime createdAt = sale.getCreatedAt() == null ? LocalDateTime.now() : sale.getCreatedAt();
        lines.add(new ReceiptLine("Date: " + createdAt.format(RECEIPT_DATE), Align.LEFT, false, 0));
        lines.add(new ReceiptLine("Customer: " + (blank(customerName) ? "Walk-in" : customerName),
                Align.LEFT, false, 0));
        lines.add(separator(columns));

        Map<Long, String> names = productNames == null ? Map.of() : productNames;
        List<SaleItem> items = sale.getItems() == null ? List.of() : sale.getItems();
        for (SaleItem item : items) {
            String product = names.getOrDefault(item.getProductId(), "Product #" + item.getProductId());
            wrap(product, columns).forEach(
                    text -> lines.add(new ReceiptLine(text, Align.LEFT, true, 0)));
            String detail = quantity(item.getQuantity()) + " x " + money(settings, item.getUnitPrice());
            lines.add(new ReceiptLine(twoColumns(detail, money(settings, item.getSubtotal()), columns),
                    Align.LEFT, false, 0));
            if (item.getDiscount() > 0) {
                lines.add(new ReceiptLine(twoColumns("  Item discount", "-" + money(settings, item.getDiscount()), columns),
                        Align.LEFT, false, 0));
            }
        }

        lines.add(separator(columns));
        lines.add(amountLine("Subtotal", sale.getSubtotal(), settings, columns, false));
        if (sale.getDiscount() > 0) {
            lines.add(new ReceiptLine(twoColumns("Discount", "-" + money(settings, sale.getDiscount()), columns),
                    Align.LEFT, false, 0));
        }
        if (sale.getTax() > 0) lines.add(amountLine("Tax", sale.getTax(), settings, columns, false));
        lines.add(amountLine("TOTAL", sale.getTotal(), settings, columns, true));
        lines.add(amountLine("Paid", amountPaid, settings, columns, false));
        lines.add(amountLine("Balance", Math.max(0, sale.getTotal() - amountPaid), settings, columns, false));
        lines.add(new ReceiptLine("Payment: " + (paymentMethod == null ? "Not recorded" : pretty(paymentMethod.name())),
                Align.LEFT, false, 0));
        lines.add(separator(columns));
        if (!blank(settings.receiptFooter())) {
            wrap(settings.receiptFooter(), columns).forEach(
                    text -> lines.add(new ReceiptLine(text, Align.CENTER, true, 0)));
        }
        lines.add(new ReceiptLine("", Align.CENTER, false, 0));
        wrap(DEVELOPER_PROMOTION, columns).forEach(
                text -> lines.add(new ReceiptLine(text, Align.CENTER, false, 0)));
        return lines;
    }

    private void print(AppSettings settings, List<ReceiptLine> lines, String jobName) {
        PrintService printService = findPrinter(settings.receiptPrinterName());
        if (printService == null) {
            throw new IllegalArgumentException("The configured receipt printer is unavailable.");
        }
        double width = millimetresToPoints(PAPER_58_MM.equals(settings.receiptPaperWidth()) ? 58 : 80);
        double lineHeight = PAPER_58_MM.equals(settings.receiptPaperWidth()) ? 10.2 : 11.2;
        double height = Math.max(millimetresToPoints(70), lines.stream()
                .mapToInt(line -> Math.max(1, line.extraSpacing() + 1)).sum() * lineHeight + 28);

        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(printService);
            job.setJobName(jobName);
            PageFormat format = new PageFormat();
            Paper paper = new Paper();
            paper.setSize(width, height);
            paper.setImageableArea(5, 5, width - 10, height - 10);
            format.setPaper(paper);
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
                Graphics2D canvas = (Graphics2D) graphics.create();
                try {
                    canvas.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                    canvas.setColor(Color.BLACK);
                    float y = 10;
                    float printableWidth = (float) pageFormat.getImageableWidth();
                    for (ReceiptLine line : lines) {
                        y += line.extraSpacing() * lineHeight;
                        float fontSize = PAPER_58_MM.equals(settings.receiptPaperWidth()) ? 7.5f : 8.5f;
                        Font font = new Font(Font.MONOSPACED, line.bold() ? Font.BOLD : Font.PLAIN,
                                Math.round(fontSize));
                        canvas.setFont(font);
                        FontMetrics metrics = canvas.getFontMetrics(font);
                        float x = switch (line.align()) {
                            case LEFT -> 0;
                            case CENTER -> Math.max(0, (printableWidth - metrics.stringWidth(line.text())) / 2);
                            case RIGHT -> Math.max(0, printableWidth - metrics.stringWidth(line.text()));
                        };
                        canvas.drawString(line.text(), x, y);
                        y += lineHeight;
                    }
                } finally {
                    canvas.dispose();
                }
                return java.awt.print.Printable.PAGE_EXISTS;
            }, format);
            job.print();
        } catch (PrinterException exception) {
            throw new RuntimeException("Receipt could not be sent to the printer: " + exception.getMessage(), exception);
        }
    }

    private PrintService findPrinter(String name) {
        if (name == null) return null;
        return Arrays.stream(PrintServiceLookup.lookupPrintServices(null, null))
                .filter(printer -> name.equals(printer.getName()))
                .findFirst().orElse(null);
    }

    private ReceiptLine amountLine(String label, double amount, AppSettings settings, int columns, boolean bold) {
        return new ReceiptLine(twoColumns(label, money(settings, amount), columns), Align.LEFT, bold, bold ? 1 : 0);
    }

    private ReceiptLine separator(int columns) {
        return new ReceiptLine("-".repeat(columns), Align.LEFT, false, 0);
    }

    private String twoColumns(String left, String right, int columns) {
        String safeLeft = safe(left);
        String safeRight = safe(right);
        int spaces = Math.max(1, columns - safeLeft.length() - safeRight.length());
        if (safeLeft.length() + safeRight.length() + spaces > columns) {
            safeLeft = safeLeft.substring(0, Math.max(1, columns - safeRight.length() - 1));
        }
        return safeLeft + " ".repeat(Math.max(1, columns - safeLeft.length() - safeRight.length())) + safeRight;
    }

    private List<String> wrap(String value, int width) {
        String text = safe(value).trim();
        if (text.isEmpty()) return List.of("");
        List<String> result = new ArrayList<>();
        while (text.length() > width) {
            int split = text.lastIndexOf(' ', width);
            if (split <= 0) split = width;
            result.add(text.substring(0, split).trim());
            text = text.substring(split).trim();
        }
        if (!text.isEmpty()) result.add(text);
        return result;
    }

    private String money(AppSettings settings, double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        String symbol = blank(settings.currencySymbol()) ? "" : settings.currencySymbol().trim();
        return symbol + (symbol.length() > 1 ? " " : "") + formatter.format(amount);
    }

    private String quantity(double quantity) {
        return quantity == Math.rint(quantity) ? Long.toString((long) quantity) : String.format(Locale.US, "%.2f", quantity);
    }

    private String pretty(String value) {
        String lower = value.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private double millimetresToPoints(double millimetres) {
        return millimetres * 72.0 / 25.4;
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
    private String safe(String value) { return value == null ? "" : value; }

    private enum Align { LEFT, CENTER, RIGHT }
    private record ReceiptLine(String text, Align align, boolean bold, int extraSpacing) { }
}
