package com.erpflow.dao;

import com.erpflow.model.Customer;
import com.erpflow.model.SalesOrder;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderDAO {


    // =========================================================
    // SAVE
    // =========================================================

    public void save(SalesOrder salesOrder) {

        String sql = """
                INSERT INTO sales_orders
                (
                    orderDate,
                    status,
                    customer_id,
                    tax_rate,
                    subtotal,
                    tax_amount,
                    total_amount
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
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

            statement.setBigDecimal(
                    4,
                    salesOrder.getTaxRate()
            );

            statement.setBigDecimal(
                    5,
                    salesOrder.getSubtotal()
            );

            statement.setBigDecimal(
                    6,
                    salesOrder.getTaxAmount()
            );

            statement.setBigDecimal(
                    7,
                    salesOrder.getTotalAmount()
            );


            statement.executeUpdate();


            try (
                    ResultSet rs =
                            statement.getGeneratedKeys()
            ) {

                if (rs.next()) {

                    salesOrder.setId(
                            rs.getInt(1)
                    );

                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error saving sales order",
                    e
            );
        }
    }


    // =========================================================
    // FIND ALL
    // =========================================================

    public List<SalesOrder> findAll() {

        String sql = """
                SELECT
                    so.id,
                    so.orderDate,
                    so.status,
                    so.customer_id,

                    so.tax_rate AS tax_rate,
                    so.subtotal AS subtotal,
                    so.tax_amount AS tax_amount,
                    so.total_amount AS total_amount,

                    c.name AS customer_name,
                    c.address AS customer_address,
                    c.email AS customer_email,
                    c.phone AS customer_phone,
                    c.status AS customer_status

                FROM sales_orders so

                JOIN customers c
                    ON so.customer_id = c.id

                ORDER BY so.orderDate DESC
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
                        mapSalesOrder(rs);

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


    // =========================================================
    // FIND BY ID
    // =========================================================

    public SalesOrder findById(int id) {

        String sql = """
                SELECT
                    so.id,
                    so.orderDate,
                    so.status,
                    so.customer_id,

                    so.tax_rate AS tax_rate,
                    so.subtotal AS subtotal,
                    so.tax_amount AS tax_amount,
                    so.total_amount AS total_amount,

                    c.name AS customer_name,
                    c.address AS customer_address,
                    c.email AS customer_email,
                    c.phone AS customer_phone,
                    c.status AS customer_status

                FROM sales_orders so

                JOIN customers c
                    ON so.customer_id = c.id

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

                if (rs.next()) {

                    return mapSalesOrder(rs);

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


    // =========================================================
    // UPDATE
    // =========================================================

    public void update(SalesOrder salesOrder) {

        String sql = """
                UPDATE sales_orders
                SET
                    orderDate = ?,
                    status = ?,
                    customer_id = ?,
                    tax_rate = ?,
                    subtotal = ?,
                    tax_amount = ?,
                    total_amount = ?

                WHERE id = ?
                """;


        try (
                Connection connection =
                        DBConnection.getConnection();

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

            statement.setBigDecimal(
                    4,
                    salesOrder.getTaxRate()
            );

            statement.setBigDecimal(
                    5,
                    salesOrder.getSubtotal()
            );

            statement.setBigDecimal(
                    6,
                    salesOrder.getTaxAmount()
            );

            statement.setBigDecimal(
                    7,
                    salesOrder.getTotalAmount()
            );

            statement.setInt(
                    8,
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


    // =========================================================
    // MAP RESULT SET
    // =========================================================

    private SalesOrder mapSalesOrder(
            ResultSet rs
    ) throws SQLException {

        SalesOrder order =
                new SalesOrder();


        order.setId(
                rs.getInt("id")
        );


        Timestamp timestamp =
                rs.getTimestamp("orderDate");


        if (timestamp != null) {

            order.setOrderDate(
                    timestamp.toLocalDateTime()
            );

        }


        order.setStatus(
                rs.getString("status")
        );


        // TAX VALUES

        order.setTaxRate(
                rs.getBigDecimal("tax_rate")
        );

        order.setSubtotal(
                rs.getBigDecimal("subtotal")
        );

        order.setTaxAmount(
                rs.getBigDecimal("tax_amount")
        );

        order.setTotalAmount(
                rs.getBigDecimal("total_amount")
        );


        // CUSTOMER

        Customer customer =
                new Customer();


        customer.setId(
                rs.getInt("customer_id")
        );

        customer.setName(
                rs.getString("customer_name")
        );

        customer.setAddress(
                rs.getString("customer_address")
        );

        customer.setEmail(
                rs.getString("customer_email")
        );

        customer.setPhone(
                rs.getString("customer_phone")
        );

        customer.setStatus(
                rs.getString("customer_status")
        );


        order.setCustomer(customer);


        return order;
    }
}