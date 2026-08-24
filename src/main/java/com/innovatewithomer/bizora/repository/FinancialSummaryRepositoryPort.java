package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.report.FinancialSummary;

import java.time.LocalDate;

public interface FinancialSummaryRepositoryPort {

    FinancialSummary getFinancialSummary(
            LocalDate from,
            LocalDate to
    );
}