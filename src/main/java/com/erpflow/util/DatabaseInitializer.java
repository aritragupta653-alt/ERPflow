package com.erpflow.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private static final String SERVER_URL =
            "jdbc:mysql://localhost:3307/";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void initialize() {

        try (
            Connection connection = DriverManager.getConnection(
                    SERVER_URL,
                    USER,
                    PASSWORD
            );

            Statement statement =
                    connection.createStatement()
        ) {

            // Create database
            statement.executeUpdate(
                    "CREATE DATABASE IF NOT EXISTS erpflow"
            );

            System.out.println(
                    "Database checked/created successfully."
            );

        } catch (SQLException e) {

            e.printStackTrace();

        }
    }
}