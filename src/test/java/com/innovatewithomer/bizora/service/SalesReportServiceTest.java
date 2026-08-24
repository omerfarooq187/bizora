package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.SalesReport;
import com.innovatewithomer.bizora.repository.SalesReportRepositoryPort;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SalesReportServiceTest {

    private SalesReportService service;

    private FakeSalesReportRepository repository;

    @BeforeEach
    void setUp() {

        repository =
                new FakeSalesReportRepository();

        service =
                new SalesReportService(
                        repository
                );
    }

    @Test
    void shouldGenerateSalesReport() {

        repository.report =
                new SalesReport(
                        5,
                        50000,
                        10000
                );

        SalesReport result =
                service.getSalesReport(
                        LocalDate.of(
                                2026,
                                8,
                                1
                        ),
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        assertEquals(
                5,
                result.getTotalSales()
        );

        assertEquals(
                50000,
                result.getTotalRevenue()
        );

        assertEquals(
                10000,
                result.getAverageSaleValue()
        );
    }

    @Test
    void shouldRejectNullStartDate() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getSalesReport(
                        null,
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                )
        );
    }

    @Test
    void shouldRejectNullEndDate() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getSalesReport(
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
    void shouldRejectStartDateAfterEndDate() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.getSalesReport(
                                LocalDate.of(
                                        2026,
                                        8,
                                        20
                                ),
                                LocalDate.of(
                                        2026,
                                        8,
                                        1
                                )
                        )
                );

        assertEquals(
                "Start date cannot be after end date.",
                exception.getMessage()
        );
    }

    private static class FakeSalesReportRepository
            implements SalesReportRepositoryPort {

        private SalesReport report;

        @Override
        public SalesReport getSalesReport(
                Connection connection,
                LocalDate from,
                LocalDate to
        ) throws SQLException {

            return report;
        }
    }
}