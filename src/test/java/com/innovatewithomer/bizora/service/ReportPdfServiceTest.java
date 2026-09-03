package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.SalesReport;
import com.innovatewithomer.bizora.model.report.ExpenseReport;
import com.innovatewithomer.bizora.model.report.FinancialSummary;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportPdfServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldCreateReadablePdfWithSelectedPeriodAndFinancialFigures() throws Exception {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 31);
        Path output = temporaryDirectory.resolve("monthly-report.pdf");

        Path saved = new ReportPdfService().export(
                output,
                from,
                to,
                new SalesReport(18, 250_000, 13_888.89),
                new ExpenseReport(30_000, 2, Map.of("Rent", 20_000.0, "Utilities", 10_000.0)),
                new FinancialSummary(from, to, 250_000, 150_000, 30_000)
        );

        assertEquals(output, saved);
        assertTrue(Files.size(saved) > 1_000);
        PdfReader reader = new PdfReader(saved.toString());
        try {
            assertEquals(1, reader.getNumberOfPages());
            String text = new PdfTextExtractor(reader).getTextFromPage(1);
            assertTrue(text.contains("Business Performance Report"));
            assertTrue(text.contains("Aug 01, 2026 to Aug 31, 2026"));
            assertTrue(text.contains("SALES REVENUE"));
            assertTrue(text.contains("NET PROFIT"));
            assertTrue(text.contains("Expenses by Category"));
            assertTrue(text.contains("InnovateWithOmer"));
        } finally {
            reader.close();
        }
    }

    @Test
    void shouldAddPdfExtensionWhenItIsMissing() {
        LocalDate today = LocalDate.now();
        Path saved = new ReportPdfService().export(
                temporaryDirectory.resolve("daily-report"),
                today,
                today,
                new SalesReport(0, 0, 0),
                new ExpenseReport(0, 0, Map.of()),
                new FinancialSummary(today, today, 0, 0, 0)
        );

        assertTrue(saved.getFileName().toString().endsWith(".pdf"));
        assertTrue(Files.isRegularFile(saved));
    }
}
