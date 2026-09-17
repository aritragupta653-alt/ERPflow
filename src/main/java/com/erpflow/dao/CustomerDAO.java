package com.erpflow.dao;

import com.erpflow.model.Customer;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public void save(Customer customer) {

        String sql = """
                INSERT INTO customers
                (name, email, phone, address, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(1, customer.getName());
            statement.setString(2, customer.getEmail());
            statement.setString(3, customer.getPhone());
            statement.setString(4, customer.getAddress());
            statement.setString(5, customer.getStatus());

            statement.executeUpdate();

            try (ResultSet resultSet =
                         statement.getGeneratedKeys()) {

                if (resultSet.next()) {
                    customer.setId(
                            resultSet.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error saving customer",
                    e
            );
        }
    }


    public List<Customer> findAll() {

        String sql = """
                SELECT id, name, email, phone,
                       address, status
                FROM customers
                WHERE status = 'ACTIVE'
                ORDER BY id
                """;

        List<Customer> customers =
                new ArrayList<>();

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                customers.add(
                        mapRowToCustomer(resultSet)
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching customers",
                    e
            );
        }

        return customers;
    }


    public Customer findById(int id) {

        String sql = """
                SELECT id, name, email, phone,
                       address, status
                FROM customers
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapRowToCustomer(
                            resultSet
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching customer",
                    e
            );
        }

        return null;
    }


    public void update(Customer customer) {

        String sql = """
                UPDATE customers
                SET name = ?,
                    email = ?,
                    phone = ?,
                    address = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, customer.getName());
            statement.setString(2, customer.getEmail());
            statement.setString(3, customer.getPhone());
            statement.setString(4, customer.getAddress());
            statement.setInt(5, customer.getId());

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error updating customer",
                    e
            );
        }
    }


    public void delete(int id) {

        String sql = """
                UPDATE customers
                SET status = 'INACTIVE'
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error deleting customer",
                    e
            );
        }
    }


    private Customer mapRowToCustomer(
            ResultSet resultSet)
            throws SQLException {

        Customer customer =
                new Customer();

        customer.setId(
                resultSet.getInt("id")
        );

        customer.setName(
                resultSet.getString("name")
        );

        customer.setEmail(
                resultSet.getString("email")
        );

        customer.setPhone(
                resultSet.getString("phone")
        );

        customer.setAddress(
                resultSet.getString("address")
        );

        customer.setStatus(
                resultSet.getString("status")
        );

        return customer;
    }
}