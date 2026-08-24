package com.innovatewithomer.bizora.infrastructure;

import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionManager {

    private TransactionManager() {
    }

    public static void execute(
            TransactionCallback callback
    ) {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try {

                callback.execute(connection);

                connection.commit();

            } catch (Exception e) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }

                throw new RuntimeException(
                        "Transaction failed.",
                        e
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to execute transaction.",
                    e
            );
        }
    }

    public static <T> T executeReturning(
            TransactionCallbackReturning<T> callback
    ) {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try {

                T result =
                        callback.execute(connection);

                connection.commit();

                return result;

            } catch (Exception e) {

                try {
                    connection.rollback();

                } catch (SQLException rollbackException) {

                    e.addSuppressed(
                            rollbackException
                    );
                }

                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }

                throw new RuntimeException(
                        "Transaction failed.",
                        e
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to execute transaction.",
                    e
            );
        }
    }

    @FunctionalInterface
    public interface TransactionCallback {

        void execute(Connection connection)
                throws Exception;
    }

    @FunctionalInterface
    public interface TransactionCallbackReturning<T> {

        T execute(Connection connection)
                throws Exception;
    }
}