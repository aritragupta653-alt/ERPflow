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
                (customer_id, order_date, status)
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

            statement.setInt(
                    1,
                    salesOrder.getCustomer().getId()
            );

            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            salesOrder.getOrderDate()
                    )
            );

            statement.setString(
                    3,
                    salesOrder.getStatus()
            );

            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {

                if (rs.next()) {
                    salesOrder.setId(
                            rs.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save sales order",
                    e
            );
        }
    }


    public List<SalesOrder> findAll() {

        String sql = """
                SELECT
                    so.id,
                    so.customer_id,
                    so.order_date,
                    so.status,
                    c.name,
                    c.email,
                    c.phone
                FROM sales_orders so
                JOIN customers c
                    ON so.customer_id = c.customer_id
                ORDER BY so.order_date DESC
                """;

        List<SalesOrder> orders =
                new ArrayList<>();

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet rs =
                        statement.executeQuery()
        ) {

            while (rs.next()) {

                SalesOrder order =
                        new SalesOrder();

                order.setId(
                        rs.getInt("id")
                );

                Customer customer =
                        new Customer();

                customer.setId(
                        rs.getInt("customer_id")
                );

                customer.setName(
                        rs.getString("name")
                );

                customer.setEmail(
                        rs.getString("email")
                );

                customer.setPhone(
                        rs.getString("phone")
                );

                order.setCustomer(customer);

                order.setOrderDate(
                        rs.getTimestamp("order_date")
                                .toLocalDateTime()
                );

                order.setStatus(
                        rs.getString("status")
                );

                orders.add(order);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch sales orders",
                    e
            );
        }

        return orders;
    }


    public SalesOrder findById(int id) {

        String sql = """
                SELECT
                    so.id,
                    so.customer_id,
                    so.order_date,
                    so.status,
                    c.name,
                    c.email,
                    c.phone
                FROM sales_orders so
                JOIN customers c
                    ON so.customer_id = c.customer_id
                WHERE so.id = ?
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            try (
                    ResultSet rs =
                            statement.executeQuery()
            ) {

                if (!rs.next()) {
                    return null;
                }

                SalesOrder order =
                        new SalesOrder();

                order.setId(
                        rs.getInt("id")
                );

                Customer customer =
                        new Customer();

                customer.setId(
                        rs.getInt("customer_id")
                );

                customer.setName(
                        rs.getString("name")
                );

                customer.setEmail(
                        rs.getString("email")
                );

                customer.setPhone(
                        rs.getString("phone")
                );

                order.setCustomer(customer);

                order.setOrderDate(
                        rs.getTimestamp("order_date")
                                .toLocalDateTime()
                );

                order.setStatus(
                        rs.getString("status")
                );

                return order;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch sales order",
                    e
            );
        }
    }


    public void update(SalesOrder salesOrder) {

        String sql = """
                UPDATE sales_orders
                SET customer_id = ?,
                    order_date = ?,
                    status = ?
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    salesOrder.getCustomer().getId()
            );

            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            salesOrder.getOrderDate()
                    )
            );

            statement.setString(
                    3,
                    salesOrder.getStatus()
            );

            statement.setInt(
                    4,
                    salesOrder.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update sales order",
                    e
            );
        }
    }
}
