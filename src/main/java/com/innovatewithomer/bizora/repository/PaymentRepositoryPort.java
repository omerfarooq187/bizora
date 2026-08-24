package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.model.Payment;
import com.innovatewithomer.bizora.model.PaymentStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface PaymentRepositoryPort {

    Payment save(
            Connection connection,
            Payment payment
    ) throws SQLException;

    Payment findById(
            Connection connection,
            Long id
    ) throws SQLException;

    Payment findById(Long id);

    List<Payment> findBySaleId(
            Connection connection,
            Long saleId
    ) throws SQLException;

    List<Payment> findBySaleId(
            Long saleId
    );
}