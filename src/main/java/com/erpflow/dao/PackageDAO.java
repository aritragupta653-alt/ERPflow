
package com.erpflow.dao;

import com.erpflow.model.Customer;
import com.erpflow.model.Package;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.enums.PackageStatus;
import com.erpflow.model.enums.SalesOrderStatus;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PackageDAO {

    // =========================
    // SAVE
    // =========================

    public void save(Package packageEntity) {

        String sql = """
                INSERT INTO packages
                (
                    package_number,
                    packageDate,
                    status,
                    weight,
                    length,
                    width,
                    height,
                    sales_order_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(
                    1,
                    packageEntity.getPackageNumber()
            );

            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            packageEntity.getPackageDate()
                    )
            );

            statement.setString(
                    3,
                    packageEntity.getStatus().name()
            );

            statement.setDouble(
                    4,
                    packageEntity.getWeight()
            );

            statement.setDouble(
                    5,
                    packageEntity.getLength()
            );

            statement.setDouble(
                    6,
                    packageEntity.getWidth()
            );

            statement.setDouble(
                    7,
                    packageEntity.getHeight()
            );

            statement.setInt(
                    8,
                    packageEntity.getSalesOrder().getId()
            );

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {
                    packageEntity.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save package",
                    e
            );
        }
    }


    // =========================
    // UPDATE
    // =========================

    public void update(Package packageEntity) {

        String sql = """
                UPDATE packages
                SET package_number = ?,
                    packageDate = ?,
                    status = ?,
                    weight = ?,
                    length = ?,
                    width = ?,
                    height = ?,
                    sales_order_id = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    packageEntity.getPackageNumber()
            );

            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            packageEntity.getPackageDate()
                    )
            );

            statement.setString(
                    3,
                    packageEntity.getStatus().name()
            );

            statement.setDouble(
                    4,
                    packageEntity.getWeight()
            );

            statement.setDouble(
                    5,
                    packageEntity.getLength()
            );

            statement.setDouble(
                    6,
                    packageEntity.getWidth()
            );

            statement.setDouble(
                    7,
                    packageEntity.getHeight()
            );

            statement.setInt(
                    8,
                    packageEntity.getSalesOrder().getId()
            );

            statement.setInt(
                    9,
                    packageEntity.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update package",
                    e
            );
        }
    }


    // =========================
    // FIND ALL
    // =========================

    public List<Package> findAll() {

        String sql = """
                SELECT
                    p.id,
                    p.package_number,
                    p.packageDate,
                    p.status,
                    p.weight,
                    p.length,
                    p.width,
                    p.height,
                    so.id AS sales_order_id,
                    so.status AS sales_order_status,
                    c.id AS customer_id,
                    c.name AS customer_name
                FROM packages p
                JOIN sales_orders so
                    ON p.sales_order_id = so.id
                JOIN customers c
                    ON so.customer_id = c.id
                ORDER BY p.packageDate DESC
                """;

        List<Package> packages = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                packages.add(mapPackage(rs));
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch packages",
                    e
            );
        }

        return packages;
    }
    
public List<Package> findAll(String status) {

    if (status == null || status.isBlank()
            || "ALL".equalsIgnoreCase(status.trim())) {
        return findAll();
    }

    String normalizedStatus = status.trim().toUpperCase();

    // Validate the status against the PackageStatus enum
    try {
        PackageStatus.valueOf(normalizedStatus);
    } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
                "Invalid package status: " + normalizedStatus
        );
    }

    String sql = """
            SELECT
                p.id,
                p.package_number,
                p.packageDate,
                p.status,
                p.weight,
                p.length,
                p.width,
                p.height,
                so.id AS sales_order_id,
                so.status AS sales_order_status,
                c.id AS customer_id,
                c.name AS customer_name
            FROM packages p
            JOIN sales_orders so
                ON p.sales_order_id = so.id
            JOIN customers c
                ON so.customer_id = c.id
            WHERE p.status = ?
            ORDER BY p.packageDate DESC
            """;

    List<Package> packages = new ArrayList<>();

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setString(1, normalizedStatus);

        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                packages.add(mapPackage(rs));
            }
        }

    } catch (SQLException e) {
        throw new RuntimeException(
                "Failed to find packages by status", e
        );
    }

    return packages;
}


    // =========================
    // FIND BY ID
    // =========================

    public Package findById(int id) {

        String sql = """
                SELECT
                    p.id,
                    p.package_number,
                    p.packageDate,
                    p.status,
                    p.weight,
                    p.length,
                    p.width,
                    p.height,
                    so.id AS sales_order_id,
                    so.status AS sales_order_status,
                    c.id AS customer_id,
                    c.name AS customer_name
                FROM packages p
                JOIN sales_orders so
                    ON p.sales_order_id = so.id
                JOIN customers c
                    ON so.customer_id = c.id
                WHERE p.id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return mapPackage(rs);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch package",
                    e
            );
        }

        return null;
    }


    // =========================
    // FIND BY SALES ORDER
    // =========================

    public List<Package> findBySalesOrder(int salesOrderId) {

        String sql = """
                SELECT
                    p.id,
                    p.package_number,
                    p.packageDate,
                    p.status,
                    p.weight,
                    p.length,
                    p.width,
                    p.height,
                    so.id AS sales_order_id,
                    so.status AS sales_order_status,
                    c.id AS customer_id,
                    c.name AS customer_name
                FROM packages p
                JOIN sales_orders so
                    ON p.sales_order_id = so.id
                JOIN customers c
                    ON so.customer_id = c.id
                WHERE so.id = ?
                ORDER BY p.packageDate DESC
                """;

        List<Package> packages = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, salesOrderId);

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    packages.add(mapPackage(rs));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch packages for sales order",
                    e
            );
        }

        return packages;
    }


    // =========================
    // MAP PACKAGE
    // =========================

    private Package mapPackage(ResultSet rs) throws SQLException {

        Package pkg = new Package();

        pkg.setId(rs.getInt("id"));

        pkg.setPackageNumber(
                rs.getString("package_number")
        );

        Timestamp packageDate = rs.getTimestamp("packageDate");

        if (packageDate != null) {
            pkg.setPackageDate(
                    packageDate.toLocalDateTime()
            );
        }

        String packageStatus = rs.getString("status");

        pkg.setStatus(
                packageStatus == null
                        ? null
                        : PackageStatus.valueOf(packageStatus)
        );

        pkg.setWeight(rs.getDouble("weight"));
        pkg.setLength(rs.getDouble("length"));
        pkg.setWidth(rs.getDouble("width"));
        pkg.setHeight(rs.getDouble("height"));


        // Sales Order

        SalesOrder salesOrder = new SalesOrder();

        salesOrder.setId(rs.getInt("sales_order_id"));

        String salesOrderStatus =
                rs.getString("sales_order_status");

        salesOrder.setStatus(
                salesOrderStatus == null
                        ? null
                        : SalesOrderStatus.valueOf(salesOrderStatus)
        );


        // Customer

        Customer customer = new Customer();

        customer.setId(rs.getInt("customer_id"));

        customer.setName(
                rs.getString("customer_name")
        );

        salesOrder.setCustomer(customer);

        pkg.setSalesOrder(salesOrder);

        return pkg;
    }
}