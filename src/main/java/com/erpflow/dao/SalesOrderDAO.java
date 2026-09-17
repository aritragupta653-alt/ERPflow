package com.erpflow.dao;

import com.erpflow.model.Customer;
import com.erpflow.model.SalesOrder;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderDAO {

    public void save(SalesOrder salesOrder) {

        String sql = """
                INSERT INTO sales_orders
                (orderDate, status, customer_id)
                VALUES (?, ?, ?)
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setTimestamp(
                    1,
                    Timestamp.valueOf(salesOrder.getOrderDate())
            );

            statement.setString(
                    2,
                    salesOrder.getStatus()
            );

            statement.setInt(
                    3,
                    salesOrder.getCustomer().getId()
            );

            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {

                if (rs.next()) {
                    salesOrder.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error saving sales order",
                    e
            );
        }
    }

    public List<SalesOrder> findAll() {

        String sql = """
                SELECT
                    so.id,
                    so.orderDate,
                    so.status,
                    so.customer_id,
                    c.name,
                    c.address,
                    c.email,
                    c.phone,
                    c.status AS customer_status
                FROM sales_orders so
                JOIN customers c
                    ON so.customer_id = c.id
                ORDER BY so.orderDate DESC
                """;

        List<SalesOrder> orders = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {

            while (rs.next()) {

                SalesOrder order = new SalesOrder();

                order.setId(rs.getInt("id"));

                order.setOrderDate(
                        rs.getTimestamp("orderDate")
                                .toLocalDateTime()
                );

                order.setStatus(
                        rs.getString("status")
                );

                Customer customer = new Customer();

                customer.setId(
                        rs.getInt("customer_id")
                );

                customer.setName(
                        rs.getString("name")
                );

                customer.setAddress(
                        rs.getString("address")
                );

                customer.setEmail(
                        rs.getString("email")
                );

                customer.setPhone(
                        rs.getString("phone")
                );

                customer.setStatus(
                        rs.getString("customer_status")
                );

                order.setCustomer(customer);

                orders.add(order);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching sales orders",
                    e
            );
        }

        return orders;
    }

    public SalesOrder findById(int id) {

        String sql = """
                SELECT
                    so.id,
                    so.orderDate,
                    so.status,
                    so.customer_id,
                    c.name,
                    c.address,
                    c.email,
                    c.phone,
                    c.status AS customer_status
                FROM sales_orders so
                JOIN customers c
                    ON so.customer_id = c.id
                WHERE so.id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {

                    SalesOrder order = new SalesOrder();

                    order.setId(
                            rs.getInt("id")
                    );

                    order.setOrderDate(
                            rs.getTimestamp("orderDate")
                                    .toLocalDateTime()
                    );

                    order.setStatus(
                            rs.getString("status")
                    );

                    Customer customer = new Customer();

                    customer.setId(
                            rs.getInt("customer_id")
                    );

                    customer.setName(
                            rs.getString("name")
                    );

                    customer.setAddress(
                            rs.getString("address")
                    );

                    customer.setEmail(
                            rs.getString("email")
                    );

                    customer.setPhone(
                            rs.getString("phone")
                    );

                    customer.setStatus(
                            rs.getString("customer_status")
                    );

                    order.setCustomer(customer);

                    return order;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching sales order",
                    e
            );
        }

        return null;
    }

    public void update(SalesOrder salesOrder) {

        String sql = """
                UPDATE sales_orders
                SET orderDate = ?,
                    status = ?,
                    customer_id = ?
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setTimestamp(
                    1,
                    Timestamp.valueOf(
                            salesOrder.getOrderDate()
                    )
            );

            statement.setString(
                    2,
                    salesOrder.getStatus()
            );

            statement.setInt(
                    3,
                    salesOrder.getCustomer().getId()
            );

            statement.setInt(
                    4,
                    salesOrder.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error updating sales order",
                    e
            );
        }
    }
}