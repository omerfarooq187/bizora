package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.report.PurchaseReport;
import com.innovatewithomer.bizora.repository.PurchaseReportRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseReportServiceTest {

    @Test
    void shouldReturnPurchaseReportFromRepository() {

        LocalDate from =
                LocalDate.of(2026, 8, 1);

        LocalDate to =
                LocalDate.of(2026, 8, 31);

        PurchaseReport expected =
                new PurchaseReport(
                        10,
                        500000,
                        50000
                );

        FakePurchaseReportRepository repository =
                new FakePurchaseReportRepository(
                        expected
                );

        PurchaseReportService service =
                new PurchaseReportService(
                        repository
                );

        PurchaseReport actual =
                service.getPurchaseReport(
                        from,
                        to
                );

        assertSame(
                expected,
                actual
        );

        assertEquals(
                from,
                repository.receivedFrom
        );

        assertEquals(
                to,
                repository.receivedTo
        );
    }

    @Test
    void shouldRejectNullRepository() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new PurchaseReportService(
                                null
                        )
        );
    }

    @Test
    void shouldRejectNullFromDate() {

        PurchaseReportService service =
                new PurchaseReportService(
                        new FakePurchaseReportRepository(
                                null
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.getPurchaseReport(
                                null,
                                LocalDate.of(
                                        2026,
                                        8,
                                        31
                                )
                        )
        );
    }

    @Test
    void shouldRejectNullToDate() {

        PurchaseReportService service =
                new PurchaseReportService(
                        new FakePurchaseReportRepository(
                                null
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.getPurchaseReport(
                                LocalDate.of(
                                        2026,
                                        8,
                                        1
                                ),
                                null
                        )
        );
    }

    @Test
    void shouldRejectInvalidDateRange() {

        PurchaseReportService service =
                new PurchaseReportService(
                        new FakePurchaseReportRepository(
                                null
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.getPurchaseReport(
                                LocalDate.of(
                                        2026,
                                        8,
                                        31
                                ),
                                LocalDate.of(
                                        2026,
                                        8,
                                        1
                                )
                        )
        );
    }

    private static class FakePurchaseReportRepository
            implements PurchaseReportRepositoryPort {

        private final PurchaseReport report;

        private LocalDate receivedFrom;
        private LocalDate receivedTo;

        private FakePurchaseReportRepository(
                PurchaseReport report
        ) {
            this.report = report;
        }

        @Override
        public PurchaseReport getPurchaseReport(
                LocalDate from,
                LocalDate to
        ) {

            receivedFrom = from;
            receivedTo = to;

            return report;
        }
    }
}