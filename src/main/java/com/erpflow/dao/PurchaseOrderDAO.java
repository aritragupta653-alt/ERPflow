
package com.erpflow.dao;

import com.erpflow.model.PurchaseOrder;
import com.erpflow.model.Supplier;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDAO {

    public void save(PurchaseOrder purchaseOrder) {
        String sql =
                "INSERT INTO purchase_orders " +
                "(supplier_id, status, orderDate) " +
                "VALUES (?, ?, ?)";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            statement.setInt(1, purchaseOrder.getSupplier().getId());
            statement.setString(2, purchaseOrder.getStatus());
            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(purchaseOrder.getOrderDate())
            );

            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    purchaseOrder.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save purchase order", e);
        }
    }

    public List<PurchaseOrder> findAll() {
        String sql =
                "SELECT po.id, po.status, po.orderDate, " +
                "s.id AS supplier_id, " +
                "s.name AS supplier_name, " +
                "s.contact_person, " +
                "s.phone, " +
                "s.email, " +
                "s.address " +
                "FROM purchase_orders po " +
                "JOIN suppliers s ON po.supplier_id = s.id " +
                "ORDER BY po.orderDate DESC";

        List<PurchaseOrder> orders = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            while (rs.next()) {
                orders.add(mapPurchaseOrder(rs));
            }

            return orders;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch purchase orders", e);
        }
    }

    public PurchaseOrder findById(int id) {
        String sql =
                "SELECT po.id, po.status, po.orderDate, " +
                "s.id AS supplier_id, " +
                "s.name AS supplier_name, " +
                "s.contact_person, " +
                "s.phone, " +
                "s.email, " +
                "s.address " +
                "FROM purchase_orders po " +
                "JOIN suppliers s ON po.supplier_id = s.id " +
                "WHERE po.id = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapPurchaseOrder(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch purchase order", e);
        }
    }

    public void update(PurchaseOrder purchaseOrder) {
        String sql =
                "UPDATE purchase_orders " +
                "SET supplier_id = ?, status = ?, orderDate = ? " +
                "WHERE id = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, purchaseOrder.getSupplier().getId());
            statement.setString(2, purchaseOrder.getStatus());
            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(purchaseOrder.getOrderDate())
            );
            statement.setInt(4, purchaseOrder.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update purchase order", e);
        }
    }

    private PurchaseOrder mapPurchaseOrder(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();

        supplier.setId(rs.getInt("supplier_id"));
        supplier.setName(rs.getString("supplier_name"));
        supplier.setContactPerson(rs.getString("contact_person"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setEmail(rs.getString("email"));
        supplier.setAddress(rs.getString("address"));

        PurchaseOrder purchaseOrder = new PurchaseOrder();

        purchaseOrder.setId(rs.getInt("id"));
        purchaseOrder.setStatus(rs.getString("status"));

        Timestamp orderTimestamp = rs.getTimestamp("orderDate");
        if (orderTimestamp != null) {
            purchaseOrder.setOrderDate(orderTimestamp.toLocalDateTime());
        }

        purchaseOrder.setSupplier(supplier);

        return purchaseOrder;
    }
}