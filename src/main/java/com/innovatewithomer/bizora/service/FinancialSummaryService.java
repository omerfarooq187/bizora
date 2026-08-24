package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.model.report.FinancialSummary;
import com.innovatewithomer.bizora.repository.FinancialSummaryRepositoryPort;

import java.time.LocalDate;

public class FinancialSummaryService {

    private final FinancialSummaryRepositoryPort financialSummaryRepository;

    public FinancialSummaryService(
            FinancialSummaryRepositoryPort financialSummaryRepository
    ) {
        if (financialSummaryRepository == null) {
            throw new IllegalArgumentException(
                    "Financial summary repository cannot be null."
            );
        }

        this.financialSummaryRepository =
                financialSummaryRepository;
    }

    public FinancialSummary getFinancialSummary(
            LocalDate from,
            LocalDate to
    ) {

        validateDateRange(
                from,
                to
        );

        return financialSummaryRepository.getFinancialSummary(
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