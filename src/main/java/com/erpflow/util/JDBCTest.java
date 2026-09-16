package com.erpflow.util;

import java.sql.Connection;

public class JDBCTest {

    public static void main(String[] args) {

        try (Connection connection =
                     DBConnection.getConnection()) {

            System.out.println(
                    "JDBC CONNECTION SUCCESSFUL!"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}