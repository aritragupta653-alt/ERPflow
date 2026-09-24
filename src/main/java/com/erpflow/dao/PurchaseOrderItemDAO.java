
package com.erpflow.dao;

import com.erpflow.model.Item;
import com.erpflow.model.PurchaseOrder;
import com.erpflow.model.PurchaseOrderItem;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.erpflow.model.enums.ItemStatus;


public class PurchaseOrderItemDAO {

    public void save(PurchaseOrderItem orderItem) {
        String sql = "INSERT INTO purchase_order_items " +
                "(purchase_order_id, item_id, quantity, purchasePrice, recievedQuantity) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, orderItem.getPurchaseOrder().getId());
            statement.setInt(2, orderItem.getItem().getId());
            statement.setInt(3, orderItem.getQuantity());
            statement.setBigDecimal(4, orderItem.getPurchasePrice());
            statement.setInt(5, orderItem.getRecievedQuantity());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    orderItem.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save purchase order item", e);
        }
    }

    public List<PurchaseOrderItem> findByPurchaseOrder(PurchaseOrder purchaseOrder) {
        String sql = "SELECT poi.id AS poi_id, poi.quantity, poi.purchasePrice, " +
                "poi.recievedQuantity, i.item_id, i.name, i.description, " +
                "i.purchase_price, i.reorder_level, i.selling_price, i.sku, i.status " +
                "FROM purchase_order_items poi " +
                "JOIN items i ON poi.item_id = i.item_id " +
                "WHERE poi.purchase_order_id = ?";

        List<PurchaseOrderItem> result = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, purchaseOrder.getId());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    PurchaseOrderItem orderItem = new PurchaseOrderItem();
                    orderItem.setId(rs.getInt("poi_id"));
                    orderItem.setQuantity(rs.getInt("quantity"));
                    orderItem.setPurchasePrice(rs.getBigDecimal("purchasePrice"));
                    orderItem.setReceivedQuantity(rs.getInt("recievedQuantity"));
                    orderItem.setPurchaseOrder(purchaseOrder);

                    Item item = new Item();
                    item.setId(rs.getInt("item_id"));
                    item.setName(rs.getString("name"));
                    item.setDescription(rs.getString("description"));
                    item.setPurchasePrice(rs.getBigDecimal("purchase_price"));
                    item.setReorderLevel(rs.getInt("reorder_level"));
                    item.setSellingPrice(rs.getBigDecimal("selling_price"));
                    item.setSku(rs.getString("sku"));
                    item.setStatus(ItemStatus.valueOf(rs.getString("status")));

                    orderItem.setItem(item);
                    result.add(orderItem);
                }
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch purchase order items", e);
        }
    }

    public void updateReceivedQuantity(int orderItemId, int receivedQuantity) {
        String sql = "UPDATE purchase_order_items SET recievedQuantity = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, receivedQuantity);
            statement.setInt(2, orderItemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update received quantity", e);
        }
    }

    public void updateItem(PurchaseOrderItem orderItem) {
        String sql = "UPDATE purchase_order_items " +
                "SET item_id = ?, quantity = ?, purchasePrice = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderItem.getItem().getId());
            statement.setInt(2, orderItem.getQuantity());
            statement.setBigDecimal(3, orderItem.getPurchasePrice());
            statement.setInt(4, orderItem.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update purchase order item", e);
        }
    }

    public void deleteByPurchaseOrder(int purchaseOrderId) {
        String sql = "DELETE FROM purchase_order_items WHERE purchase_order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, purchaseOrderId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete purchase order items", e);
        }
    }

    public void deleteItem(int orderItemId) {
        String sql = "DELETE FROM purchase_order_items WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderItemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete purchase order item", e);
        }
    }
}