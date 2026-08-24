package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.report.FinancialSummary;
import com.innovatewithomer.bizora.repository.FinancialSummaryRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FinancialSummaryServiceTest {

    @Test
    void shouldRejectNullRepository() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new FinancialSummaryService(null)
        );
    }

    @Test
    void shouldRejectNullFromDate() {

        FinancialSummaryRepositoryPort repository =
                (from, to) -> null;

        FinancialSummaryService service =
                new FinancialSummaryService(repository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getFinancialSummary(
                        null,
                        LocalDate.of(2026, 8, 20)
                )
        );
    }

    @Test
    void shouldRejectNullToDate() {

        FinancialSummaryRepositoryPort repository =
                (from, to) -> null;

        FinancialSummaryService service =
                new FinancialSummaryService(repository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getFinancialSummary(
                        LocalDate.of(2026, 8, 1),
                        null
                )
        );
    }

    @Test
    void shouldRejectInvalidDateRange() {

        FinancialSummaryRepositoryPort repository =
                (from, to) -> null;

        FinancialSummaryService service =
                new FinancialSummaryService(repository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getFinancialSummary(
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 1)
                )
        );
    }

    @Test
    void shouldAllowSameDate() {

        FinancialSummary expected =
                new FinancialSummary();

        FinancialSummaryRepositoryPort repository =
                (from, to) -> expected;

        FinancialSummaryService service =
                new FinancialSummaryService(repository);

        FinancialSummary result =
                service.getFinancialSummary(
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 20)
                );

        assertSame(
                expected,
                result
        );
    }

    @Test
    void shouldDelegateToRepository() {

        FinancialSummary expected =
                new FinancialSummary();

        LocalDate from =
                LocalDate.of(2026, 8, 1);

        LocalDate to =
                LocalDate.of(2026, 8, 20);

        FinancialSummaryRepositoryPort repository =
                (repositoryFrom, repositoryTo) -> {

                    assertEquals(
                            from,
                            repositoryFrom
                    );

                    assertEquals(
                            to,
                            repositoryTo
                    );

                    return expected;
                };

        FinancialSummaryService service =
                new FinancialSummaryService(repository);

        FinancialSummary result =
                service.getFinancialSummary(
                        from,
                        to
                );

        assertSame(
                expected,
                result
        );
    }
}