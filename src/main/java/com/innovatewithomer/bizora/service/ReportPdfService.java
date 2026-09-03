package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.config.AppSettings;
import com.innovatewithomer.bizora.config.AppSettingsStore;
import com.innovatewithomer.bizora.model.SalesReport;
import com.innovatewithomer.bizora.model.report.ExpenseReport;
import com.innovatewithomer.bizora.model.report.FinancialSummary;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportPdfService {

    private static final Color INK = new Color(35, 35, 43);
    private static final Color MUTED = new Color(105, 108, 120);
    private static final Color PRIMARY = new Color(81, 71, 229);
    private static final Color PALE_PRIMARY = new Color(244, 243, 255);
    private static final Color BORDER = new Color(225, 226, 234);
    private static final Color GREEN = new Color(5, 150, 105);
    private static final Color RED = new Color(220, 38, 38);
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter GENERATED_TIME = DateTimeFormatter.ofPattern("MMM dd, yyyy  h:mm a");

    public Path export(
            Path output,
            LocalDate from,
            LocalDate to,
            SalesReport sales,
            ExpenseReport expenses,
            FinancialSummary financial
    ) {
        if (output == null) throw new IllegalArgumentException("Choose where to save the PDF report.");
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("Select a valid report date range.");
        }
        if (sales == null || expenses == null || financial == null) {
            throw new IllegalArgumentException("Report data is incomplete.");
        }

        Path destination = output.toAbsolutePath().normalize();
        if (!destination.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            destination = destination.resolveSibling(destination.getFileName() + ".pdf");
        }

        Document document = new Document(PageSize.A4, 42, 42, 54, 50);
        try {
            Path parent = destination.getParent();
            if (parent != null) Files.createDirectories(parent);
            try (OutputStream stream = Files.newOutputStream(destination)) {
                PdfWriter writer = PdfWriter.getInstance(document, stream);
                AppSettings settings = AppSettingsStore.load();
                writer.setPageEvent(new ReportFooter(settings.businessName()));
                document.addTitle(settings.businessName() + " - Business Report");
                document.addAuthor("Bizora by InnovateWithOmer");
                document.addSubject("Business performance report from " + from + " to " + to);
                document.open();

                addHeader(document, settings);
                addReportTitle(document, from, to);
                addMetrics(document, settings, sales, expenses, financial);
                addExpenseBreakdown(document, settings, expenses);
                addNotes(document);
                document.close();
            }
            return destination;
        } catch (Exception exception) {
            try { Files.deleteIfExists(destination); }
            catch (Exception ignored) { }
            throw new RuntimeException("Could not create the PDF report.", exception);
        } finally {
            if (document.isOpen()) document.close();
        }
    }

    private void addHeader(Document document, AppSettings settings) throws Exception {
        PdfPTable header = new PdfPTable(new float[]{1, 4.6f});
        header.setWidthPercentage(100);
        header.setSpacingAfter(22);

        PdfPCell logoCell = plainCell();
        try (InputStream logoStream = ReportPdfService.class.getResourceAsStream(
                "/com/innovatewithomer/bizora/images/bizora_logo.png")) {
            if (logoStream == null) throw new IllegalStateException("Bizora logo is unavailable.");
            byte[] logoBytes = logoStream.readAllBytes();
            Image logo = Image.getInstance(logoBytes);
            logo.scaleToFit(54, 54);
            logoCell.addElement(logo);
        } catch (Exception ignored) {
            logoCell.addElement(new Phrase("B", font(24, Font.BOLD, PRIMARY)));
        }
        header.addCell(logoCell);

        PdfPCell identity = plainCell();
        identity.addElement(new Paragraph(settings.businessName(), font(19, Font.BOLD, INK)));
        String contact = contactLine(settings);
        if (!contact.isBlank()) identity.addElement(new Paragraph(contact, font(8.5f, Font.NORMAL, MUTED)));
        if (settings.businessAddress() != null && !settings.businessAddress().isBlank()) {
            identity.addElement(new Paragraph(settings.businessAddress(), font(8.5f, Font.NORMAL, MUTED)));
        }
        header.addCell(identity);
        document.add(header);
    }

    private void addReportTitle(Document document, LocalDate from, LocalDate to) throws DocumentException {
        Paragraph title = new Paragraph("Business Performance Report", font(22, Font.BOLD, INK));
        title.setSpacingAfter(5);
        document.add(title);

        Paragraph period = new Paragraph(
                "Reporting period: " + from.format(DISPLAY_DATE) + " to " + to.format(DISPLAY_DATE),
                font(10, Font.NORMAL, MUTED));
        period.setSpacingAfter(2);
        document.add(period);

        Paragraph generated = new Paragraph(
                "Generated offline by Bizora on " + LocalDateTime.now().format(GENERATED_TIME),
                font(8.5f, Font.NORMAL, MUTED));
        generated.setSpacingAfter(20);
        document.add(generated);
    }

    private void addMetrics(
            Document document,
            AppSettings settings,
            SalesReport sales,
            ExpenseReport expenses,
            FinancialSummary financial
    ) throws DocumentException {
        document.add(sectionHeading("Financial Summary"));
        PdfPTable metrics = new PdfPTable(2);
        metrics.setWidthPercentage(100);
        metrics.setSpacingAfter(20);
        metrics.setWidths(new float[]{1, 1});
        metrics.addCell(metricCell("Sales Revenue", money(settings, sales.getTotalRevenue()),
                sales.getTotalSales() + " completed transaction(s)", PRIMARY));
        metrics.addCell(metricCell("Cost of Goods Sold", money(settings, financial.getTotalCostOfGoodsSold()),
                "Captured cost of items sold", new Color(217, 119, 6)));
        metrics.addCell(metricCell("Gross Profit", money(settings, financial.getGrossProfit()),
                "Revenue minus cost of goods sold", financial.getGrossProfit() >= 0 ? GREEN : RED));
        metrics.addCell(metricCell("Operating Expenses", money(settings, expenses.getTotalExpenses()),
                expenses.getExpenseCount() + " expense entr" + (expenses.getExpenseCount() == 1 ? "y" : "ies"), RED));
        metrics.addCell(metricCell("Net Profit", money(settings, financial.getNetProfit()),
                "Gross profit minus expenses", financial.getNetProfit() >= 0 ? GREEN : RED));
        double margin = sales.getTotalRevenue() == 0 ? 0
                : financial.getNetProfit() / sales.getTotalRevenue() * 100;
        metrics.addCell(metricCell("Net Margin", String.format(Locale.US, "%.1f%%", margin),
                "Net profit as a share of revenue", margin >= 0 ? GREEN : RED));
        document.add(metrics);
    }

    private void addExpenseBreakdown(Document document, AppSettings settings, ExpenseReport expenses)
            throws DocumentException {
        document.add(sectionHeading("Expenses by Category"));
        Map<String, Double> categoryMap = expenses.getExpensesByCategory();
        if (categoryMap == null || categoryMap.isEmpty()) {
            Paragraph empty = new Paragraph("No expenses were recorded during this period.",
                    font(10, Font.ITALIC, MUTED));
            empty.setSpacingAfter(20);
            document.add(empty);
            return;
        }

        PdfPTable table = new PdfPTable(new float[]{3.2f, 1.5f, 1.1f});
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        table.setSpacingAfter(20);
        table.addCell(tableHeader("Category", Element.ALIGN_LEFT));
        table.addCell(tableHeader("Amount", Element.ALIGN_RIGHT));
        table.addCell(tableHeader("Share", Element.ALIGN_RIGHT));

        List<Map.Entry<String, Double>> categories = new ArrayList<>(categoryMap.entrySet());
        categories.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        double total = expenses.getTotalExpenses();
        for (Map.Entry<String, Double> entry : categories) {
            table.addCell(tableCell(entry.getKey(), Element.ALIGN_LEFT));
            table.addCell(tableCell(money(settings, entry.getValue()), Element.ALIGN_RIGHT));
            double share = total == 0 ? 0 : entry.getValue() / total * 100;
            table.addCell(tableCell(String.format(Locale.US, "%.1f%%", share), Element.ALIGN_RIGHT));
        }
        document.add(table);
    }

    private void addNotes(Document document) throws DocumentException {
        PdfPTable note = new PdfPTable(1);
        note.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(PALE_PRIMARY);
        cell.setPadding(11);
        cell.addElement(new Paragraph("How figures are calculated", font(9, Font.BOLD, PRIMARY)));
        cell.addElement(new Paragraph(
                "Gross profit = sales revenue - cost of goods sold. Net profit = gross profit - operating expenses. "
                        + "Figures include completed sales within the selected date range.",
                font(8.5f, Font.NORMAL, MUTED)));
        note.addCell(cell);
        document.add(note);
    }

    private PdfPCell metricCell(String label, String value, String detail, Color accent) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BORDER);
        cell.setPadding(13);
        cell.setMinimumHeight(76);
        cell.addElement(new Paragraph(label.toUpperCase(Locale.ROOT), font(7.5f, Font.BOLD, MUTED)));
        Paragraph amount = new Paragraph(value, font(17, Font.BOLD, accent));
        amount.setSpacingBefore(4);
        cell.addElement(amount);
        cell.addElement(new Paragraph(detail, font(7.8f, Font.NORMAL, MUTED)));
        return cell;
    }

    private Paragraph sectionHeading(String text) {
        Paragraph heading = new Paragraph(text, font(12, Font.BOLD, INK));
        heading.setSpacingAfter(9);
        return heading;
    }

    private PdfPCell tableHeader(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font(8.5f, Font.BOLD, INK)));
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(PALE_PRIMARY);
        cell.setBorderColor(BORDER);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell tableCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font(9, Font.NORMAL, INK)));
        cell.setHorizontalAlignment(alignment);
        cell.setBorderColor(BORDER);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell plainCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private Font font(float size, int style, Color color) {
        return new Font(Font.HELVETICA, size, style, color);
    }

    private String money(AppSettings settings, double amount) {
        NumberFormat number = NumberFormat.getNumberInstance(Locale.US);
        number.setMinimumFractionDigits(2);
        number.setMaximumFractionDigits(2);
        String symbol = switch (settings.currencySymbol()) {
            case "₨" -> "Rs";
            case "₹" -> "INR";
            case "€" -> "EUR";
            case "£" -> "GBP";
            case "¥" -> "JPY";
            default -> settings.currencySymbol();
        };
        return symbol + (symbol.length() > 1 ? " " : "") + number.format(amount);
    }

    private String contactLine(AppSettings settings) {
        List<String> details = new ArrayList<>();
        if (settings.ownerName() != null && !settings.ownerName().isBlank()) details.add(settings.ownerName());
        if (settings.businessPhone() != null && !settings.businessPhone().isBlank()) details.add(settings.businessPhone());
        if (settings.businessEmail() != null && !settings.businessEmail().isBlank()) details.add(settings.businessEmail());
        return String.join("  |  ", details);
    }

    private class ReportFooter extends PdfPageEventHelper {
        private final String businessName;

        private ReportFooter(String businessName) {
            this.businessName = businessName;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent();
            canvas.setColorStroke(BORDER);
            canvas.moveTo(document.left(), 38);
            canvas.lineTo(document.right(), 38);
            canvas.stroke();
            Phrase left = new Phrase(businessName + "  |  Bizora by InnovateWithOmer",
                    font(7.5f, Font.NORMAL, MUTED));
            Phrase right = new Phrase("Page " + writer.getPageNumber(), font(7.5f, Font.NORMAL, MUTED));
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, left, document.left(), 25, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT, right, document.right(), 25, 0);
        }
    }
}
