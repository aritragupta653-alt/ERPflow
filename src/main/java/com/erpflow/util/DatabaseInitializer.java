package com.erpflow.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Small startup helper retained for the existing application. Schema changes
 * for the fulfillment redesign live in database/fulfillment_migration.sql so
 * production schema evolution remains explicit.
 */
public class DatabaseInitializer {

    private static final String SERVER_URL =
            System.getProperty(
                    "erpflow.db.serverUrl",
                    "jdbc:mysql://localhost:3306/");

    private static final String USER =
            System.getProperty("erpflow.db.user", "root");

    private static final String PASSWORD =
            System.getProperty("erpflow.db.password", "");

    public static void initialize() {
        try (Connection connection = DriverManager.getConnection(
                SERVER_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS erpflow");
            System.out.println("Database checked/created successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("Unable to initialize ERPFlow database", e);
        }
    }
}
