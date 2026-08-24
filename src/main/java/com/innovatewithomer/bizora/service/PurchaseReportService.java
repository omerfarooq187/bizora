package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.report.PurchaseReport;
import com.innovatewithomer.bizora.repository.PurchaseReportRepositoryPort;

import java.time.LocalDate;

public class PurchaseReportService {

    private final PurchaseReportRepositoryPort purchaseReportRepository;

    public PurchaseReportService(
            PurchaseReportRepositoryPort purchaseReportRepository
    ) {

        if (purchaseReportRepository == null) {
            throw new IllegalArgumentException(
                    "Purchase report repository cannot be null."
            );
        }

        this.purchaseReportRepository =
                purchaseReportRepository;
    }

    public PurchaseReport getPurchaseReport(
            LocalDate from,
            LocalDate to
    ) {

        validateDateRange(
                from,
                to
        );

        return purchaseReportRepository.getPurchaseReport(
                from,
                to
        );
    }

    private void validateDateRange(
            LocalDate from,
            LocalDate to
    ) {

        if (from == null) {
            throw new IllegalArgumentException(
                    "From date cannot be null."
            );
        }

        if (to == null) {
            throw new IllegalArgumentException(
                    "To date cannot be null."
            );
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "From date cannot be after to date."
            );
        }
    }
}