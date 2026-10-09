package com.erpflow.util;

import java.sql.Connection;

/**
 * Runs one business operation as one JDBC transaction.
 *
 * Services/use-cases own transaction boundaries. DAOs never commit or
 * rollback and never receive Connection arguments.
 */
public final class TransactionManager {

    @FunctionalInterface
    public interface TransactionWork<T> {
        T execute() throws Exception;
    }

    private TransactionManager() {
    }

    public static <T> T execute(TransactionWork<T> work) {
        if (work == null) {
            throw new IllegalArgumentException("Transaction work is required");
        }

        if (DBConnection.isTransactionActive()) {
            throw new IllegalStateException(
                    "Nested transaction is not allowed. Keep one transaction boundary per business operation.");
        }

        Connection connection = null;

        try {
            connection = DBConnection.beginTransaction();
            T result = work.execute();
            connection.commit();
            return result;

        } catch (Throwable e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (Exception rollbackException) {
                    e.addSuppressed(rollbackException);
                }
            }

            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (e instanceof Error error) {
                throw error;
            }

            throw new RuntimeException("Transaction failed", e);

        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (Exception ignored) {
                    // The physical connection is about to be closed.
                }

                try {
                    connection.close();
                } catch (Exception ignored) {
                    // Nothing else can safely be done here.
                }
            }

            DBConnection.endTransaction();
        }
    }
}
