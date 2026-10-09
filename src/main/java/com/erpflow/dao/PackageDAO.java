package com.erpflow.dao;

import com.erpflow.model.Customer;
import com.erpflow.model.Package;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.enums.CustomerStatus;
import com.erpflow.model.enums.PackageStatus;
import com.erpflow.model.enums.SalesOrderStatus;
import com.erpflow.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackageDAO {

    public void save(Package packageEntity) {
        String sql = """
                INSERT INTO packages
                    (package_number, packageDate, status, weight,
                     length, width, height, sales_order_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, packageEntity.getPackageNumber());
            statement.setDate(2, Date.valueOf(packageEntity.getPackageDate()));
            statement.setString(3, packageEntity.getStatus().name());
            statement.setDouble(4, packageEntity.getWeight());
            statement.setDouble(5, packageEntity.getLength());
            statement.setDouble(6, packageEntity.getWidth());
            statement.setDouble(7, packageEntity.getHeight());
            statement.setInt(8, packageEntity.getSalesOrder().getId());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    packageEntity.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save package", e);
        }
    }

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
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, packageEntity.getPackageNumber());
            statement.setDate(2, Date.valueOf(packageEntity.getPackageDate()));
            statement.setString(3, packageEntity.getStatus().name());
            statement.setDouble(4, packageEntity.getWeight());
            statement.setDouble(5, packageEntity.getLength());
            statement.setDouble(6, packageEntity.getWidth());
            statement.setDouble(7, packageEntity.getHeight());
            statement.setInt(8, packageEntity.getSalesOrder().getId());
            statement.setInt(9, packageEntity.getId());

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("Package was not found for update");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update package", e);
        }
    }

    public List<Package> findAll() {
        return findByQuery("", Collections.emptyList());
    }

    public List<Package> findAll(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status.trim())) {
            return findAll();
        }

        String normalized = status.trim().toUpperCase();
        try {
            PackageStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid package status: " + normalized);
        }

        return findByQuery(" WHERE p.status = ?", List.of(normalized));
    }

    public Package findById(int id) {
        List<Package> packages = findByQuery(" WHERE p.id = ?", List.of(id));
        return packages.isEmpty() ? null : packages.get(0);
    }

    public List<Package> findBySalesOrder(int salesOrderId) {
        return findByQuery(" WHERE p.sales_order_id = ?", List.of(salesOrderId));
    }

    /** Bulk package lookup used by shipment operations. */
    public void updateStatusBatch(List<Integer> packageIds, PackageStatus status) {
        if (packageIds == null || packageIds.isEmpty()) {
            return;
        }
        if (status == null) {
            throw new IllegalArgumentException("Package status is required");
        }

        String placeholders = String.join(",", Collections.nCopies(packageIds.size(), "?"));
        String sql = "UPDATE packages SET status = ? WHERE id IN (" + placeholders + ")";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            for (int i = 0; i < packageIds.size(); i++) {
                statement.setInt(i + 2, packageIds.get(i));
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update package statuses", e);
        }
    }

    public List<Package> findByIds(List<Integer> packageIds) {
        if (packageIds == null || packageIds.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders = String.join(",", Collections.nCopies(packageIds.size(), "?"));
        String sql = baseSelect()
                + " WHERE p.id IN (" + placeholders + ") ORDER BY p.id";

        return executeList(sql, packageIds);
    }

    private List<Package> findByQuery(String whereClause, List<?> parameters) {
        String sql = baseSelect()
                + whereClause
                + " ORDER BY p.packageDate DESC, p.id DESC";
        return executeList(sql, parameters);
    }

    private String baseSelect() {
        return """
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
                    so.orderDate AS sales_order_date,
                    so.status AS sales_order_status,
                    c.id AS customer_id,
                    c.name AS customer_name,
                    c.address AS customer_address,
                    c.email AS customer_email,
                    c.phone AS customer_phone,
                    c.status AS customer_status
                FROM packages p
                JOIN sales_orders so ON p.sales_order_id = so.id
                JOIN customers c ON so.customer_id = c.id
                """;
    }

    private List<Package> executeList(String sql, List<?> parameters) {
        List<Package> packages = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    packages.add(mapPackage(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch packages", e);
        }

        return packages;
    }

    private Package mapPackage(ResultSet rs) throws SQLException {
        Package pkg = new Package();
        pkg.setId(rs.getInt("id"));
        pkg.setPackageNumber(rs.getString("package_number"));

        Date packageDate = rs.getDate("packageDate");
        if (packageDate != null) {
            pkg.setPackageDate(packageDate.toLocalDate());
        }

        String status = rs.getString("status");
        pkg.setStatus(status == null ? null : PackageStatus.valueOf(status));
        pkg.setWeight(rs.getDouble("weight"));
        pkg.setLength(rs.getDouble("length"));
        pkg.setWidth(rs.getDouble("width"));
        pkg.setHeight(rs.getDouble("height"));

        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setId(rs.getInt("sales_order_id"));

        java.sql.Timestamp orderDate = rs.getTimestamp("sales_order_date");
        if (orderDate != null) {
            salesOrder.setOrderDate(orderDate.toLocalDateTime());
        }

        String orderStatus = rs.getString("sales_order_status");
        salesOrder.setStatus(orderStatus == null ? null : SalesOrderStatus.valueOf(orderStatus));

        Customer customer = new Customer();
        customer.setId(rs.getInt("customer_id"));
        customer.setName(rs.getString("customer_name"));
        customer.setAddress(rs.getString("customer_address"));
        customer.setEmail(rs.getString("customer_email"));
        customer.setPhone(rs.getString("customer_phone"));

        String customerStatus = rs.getString("customer_status");
        if (customerStatus != null) {
            customer.setStatus(CustomerStatus.valueOf(customerStatus));
        }

        salesOrder.setCustomer(customer);
        pkg.setSalesOrder(salesOrder);
        return pkg;
    }
    public List<Package> findPackedBySalesOrder(int salesOrderId) {

    String sql = """
            SELECT
                p.id,
                p.package_number,
                p.package_date,
                p.status,
                p.weight,
                p.length,
                p.width,
                p.height,
                p.sales_order_id
            FROM packages p
            WHERE p.sales_order_id = ?
              AND p.status = ?
            ORDER BY p.id
            """;

    List<Package> packages = new ArrayList<>();

    try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
    ) {

        statement.setInt(1, salesOrderId);
        statement.setString(
                2,
                PackageStatus.PACKED.name()
        );

        try (ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                packages.add(mapPackage(rs));
            }
        }

    } catch (SQLException e) {

        throw new RuntimeException(
                "Failed to fetch packed packages for Sales Order",
                e
        );
    }

    return packages;
}
}
