package com.innovatewithomer.bizora.infrastructure.migration;

import java.sql.Connection;
import java.sql.SQLException;

public interface Migration {

    int version();

    String description();

    void migrate(Connection connection) throws SQLException;
}