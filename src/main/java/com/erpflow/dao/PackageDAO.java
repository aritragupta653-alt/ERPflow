package com.erpflow.dao;

import com.erpflow.model.Customer;
import com.erpflow.model.Package;
import com.erpflow.model.SalesOrder;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PackageDAO {

    // SAVE PACKAGE
    public void save(Package packageEntity) {

        String sql = """
                INSERT INTO packages
                (packageDate, status, weight, sales_order_id)
                VALUES (?, ?, ?, ?)
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
                            packageEntity.getPackageDate()
                    )
            );

            statement.setString(
                    2,
                    packageEntity.getStatus()
            );

            statement.setDouble(
                    3,
                    packageEntity.getWeight()
            );

            statement.setInt(
                    4,
                    packageEntity.getSalesOrder().getId()
            );

            statement.executeUpdate();

            try (ResultSet rs =
                         statement.getGeneratedKeys()) {

                if (rs.next()) {
                    packageEntity.setId(
                            rs.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error saving package",
                    e
            );
        }
    }


    // GET ALL PACKAGES
    public List<Package> findAll() {

        String sql = """
                SELECT
                    p.id,
                    p.packageDate,
                    p.status,
                    p.weight,
                    p.sales_order_id,

                    so.orderDate,
                    so.status AS order_status,
                    so.customer_id,

                    c.name AS customer_name,
                    c.address AS customer_address,
                    c.email AS customer_email,
                    c.phone AS customer_phone,
                    c.status AS customer_status

                FROM packages p

                JOIN sales_orders so
                    ON p.sales_order_id = so.id

                JOIN customers c
                    ON so.customer_id = c.id

                ORDER BY p.packageDate DESC
                """;

        List<Package> packages =
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

                packages.add(
                        mapPackage(rs)
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching packages",
                    e
            );
        }

        return packages;
    }


    // GET PACKAGE BY ID
    public Package findById(int id) {

        String sql = """
                SELECT
                    p.id,
                    p.packageDate,
                    p.status,
                    p.weight,
                    p.sales_order_id,

                    so.orderDate,
                    so.status AS order_status,
                    so.customer_id,

                    c.name AS customer_name,
                    c.address AS customer_address,
                    c.email AS customer_email,
                    c.phone AS customer_phone,
                    c.status AS customer_status

                FROM packages p

                JOIN sales_orders so
                    ON p.sales_order_id = so.id

                JOIN customers c
                    ON so.customer_id = c.id

                WHERE p.id = ?
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            try (ResultSet rs =
                         statement.executeQuery()) {

                if (rs.next()) {
                    return mapPackage(rs);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching package",
                    e
            );
        }

        return null;
    }


    // GET PACKAGES FOR SALES ORDER
    public List<Package> findBySalesOrder(
            SalesOrder salesOrder) {

        String sql = """
                SELECT
                    p.id,
                    p.packageDate,
                    p.status,
                    p.weight,
                    p.sales_order_id,

                    so.orderDate,
                    so.status AS order_status,
                    so.customer_id,

                    c.name AS customer_name,
                    c.address AS customer_address,
                    c.email AS customer_email,
                    c.phone AS customer_phone,
                    c.status AS customer_status

                FROM packages p

                JOIN sales_orders so
                    ON p.sales_order_id = so.id

                JOIN customers c
                    ON so.customer_id = c.id

                WHERE p.sales_order_id = ?

                ORDER BY p.packageDate DESC
                """;

        List<Package> packages =
                new ArrayList<>();

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    salesOrder.getId()
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                while (rs.next()) {

                    packages.add(
                            mapPackage(rs)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching packages for sales order",
                    e
            );
        }

        return packages;
    }


    // UPDATE PACKAGE
    public void update(Package packageEntity) {

        String sql = """
                UPDATE packages
                SET packageDate = ?,
                    status = ?,
                    weight = ?,
                    sales_order_id = ?
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
                            packageEntity.getPackageDate()
                    )
            );

            statement.setString(
                    2,
                    packageEntity.getStatus()
            );

            statement.setDouble(
                    3,
                    packageEntity.getWeight()
            );

            statement.setInt(
                    4,
                    packageEntity.getSalesOrder().getId()
            );

            statement.setInt(
                    5,
                    packageEntity.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error updating package",
                    e
            );
        }
    }


    // MAP RESULTSET TO PACKAGE
    private Package mapPackage(
            ResultSet rs) throws SQLException {

        Package packageEntity =
                new Package();

        packageEntity.setId(
                rs.getInt("id")
        );

        packageEntity.setPackageDate(
                rs.getTimestamp("packageDate")
                        .toLocalDateTime()
        );

        packageEntity.setStatus(
                rs.getString("status")
        );

        packageEntity.setWeight(
                rs.getDouble("weight")
        );


        // Sales Order
        SalesOrder salesOrder =
                new SalesOrder();

        salesOrder.setId(
                rs.getInt("sales_order_id")
        );

        salesOrder.setOrderDate(
                rs.getTimestamp("orderDate")
                        .toLocalDateTime()
        );

        salesOrder.setStatus(
                rs.getString("order_status")
        );


        // Customer
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

        salesOrder.setCustomer(customer);

        packageEntity.setSalesOrder(
                salesOrder
        );

        return packageEntity;
    }
}