package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.SalesReport;
import com.innovatewithomer.bizora.repository.SalesReportRepositoryPort;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class SalesReportService {

    private final SalesReportRepositoryPort reportRepository;

    public SalesReportService(
            SalesReportRepositoryPort reportRepository
    ) {
        this.reportRepository =
                reportRepository;
    }

    public SalesReport getSalesReport(
            LocalDate from,
            LocalDate to
    ) {

        validateDateRange(
                from,
                to
        );

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            return reportRepository.getSalesReport(
                    connection,
                    from,
                    to
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to generate sales report.",
                    e
            );
        }
    }

    private void validateDateRange(
            LocalDate from,
            LocalDate to
    ) {

        if (from == null) {

            throw new IllegalArgumentException(
                    "Start date is required."
            );
        }

        if (to == null) {

            throw new IllegalArgumentException(
                    "End date is required."
            );
        }

        if (from.isAfter(to)) {

            throw new IllegalArgumentException(
                    "Start date cannot be after end date."
            );
        }
    }
}