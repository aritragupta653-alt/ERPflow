package com.erpflow.dao;

import com.erpflow.model.Item;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.model.enums.ItemStatus;
import com.erpflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SalesOrderItemDAO {

    public void save(SalesOrderItem salesOrderItem) {
        String sql = """
                INSERT INTO sales_order_items
                    (quantity, packed_quantity, shipped_quantity,
                     sellingPrice, item_id, sales_order_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, salesOrderItem.getQuantity());
            statement.setInt(2, salesOrderItem.getPackedQuantity());
            statement.setInt(3, salesOrderItem.getShippedQuantity());
            statement.setBigDecimal(4, salesOrderItem.getSellingPrice());
            statement.setInt(5, salesOrderItem.getItem().getId());
            statement.setInt(6, salesOrderItem.getSalesOrder().getId());

            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    salesOrderItem.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving sales order item", e);
        }
    }

    public List<SalesOrderItem> findBySalesOrder(int salesOrderId) {
        return findBySalesOrderInternal(salesOrderId, false);
    }

    /**
     * Same read as findBySalesOrder, but locks the order lines when called
     * inside a transaction. Used by package/shipment operations to prevent
     * concurrent fulfillment from overshooting the counters.
     */
    public List<SalesOrderItem> findForUpdateBySalesOrder(int salesOrderId) {
        return findBySalesOrderInternal(salesOrderId, true);
    }

    private List<SalesOrderItem> findBySalesOrderInternal(
            int salesOrderId,
            boolean forUpdate) {

        String sql = """
                SELECT
                    soi.id,
                    soi.quantity,
                    COALESCE(soi.packed_quantity, 0) AS packed_quantity,
                    COALESCE(soi.shipped_quantity, 0) AS shipped_quantity,
                    soi.sellingPrice,
                    soi.item_id,
                    soi.sales_order_id,

                    i.name,
                    i.description,
                    i.purchase_price,
                    i.reorder_level,
                    i.selling_price,
                    i.sku,
                    i.status,
                    i.item_type,
                    i.track_inventory,
                    i.length,
                    i.width,
                    i.height,
                    i.in_hand_quantity,
                    i.committed_quantity,
                    i.max_stock_quantity,
                    i.weight
                FROM sales_order_items soi
                JOIN items i ON soi.item_id = i.item_id
                WHERE soi.sales_order_id = ?
                ORDER BY soi.id
                """ + (forUpdate ? " FOR UPDATE" : "");

        List<SalesOrderItem> items = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, salesOrderId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching sales order items", e);
        }

        return items;
    }

    public void update(SalesOrderItem salesOrderItem) {
        String sql = """
                UPDATE sales_order_items
                SET quantity = ?,
                    sellingPrice = ?,
                    item_id = ?
                WHERE id = ?
                  AND sales_order_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, salesOrderItem.getQuantity());
            statement.setBigDecimal(2, salesOrderItem.getSellingPrice());
            statement.setInt(3, salesOrderItem.getItem().getId());
            statement.setInt(4, salesOrderItem.getId());
            statement.setInt(5, salesOrderItem.getSalesOrder().getId());

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("Sales order item was not found for update");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating sales order item", e);
        }
    }

    /**
     * Adjust packed quantities atomically. Positive values pack more units;
     * negative values remove units from an edited package.
     */
    public void adjustPackedQuantities(Map<Integer, Integer> deltas) {
        if (deltas == null || deltas.isEmpty()) {
            return;
        }

        String sql = """
                UPDATE sales_order_items
                SET packed_quantity = packed_quantity + ?
                WHERE id = ?
                  AND packed_quantity + ? >= 0
                  AND packed_quantity + ? <= quantity
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (Map.Entry<Integer, Integer> entry : deltas.entrySet()) {
                int lineId = entry.getKey();
                int delta = entry.getValue();

                if (lineId <= 0 || delta == 0) {
                    throw new IllegalArgumentException("Invalid packed quantity adjustment");
                }

                statement.setInt(1, delta);
                statement.setInt(2, lineId);
                statement.setInt(3, delta);
                statement.setInt(4, delta);
                statement.addBatch();
            }

            int[] results = statement.executeBatch();
            for (int result : results) {
                if (result == 0 || result == Statement.EXECUTE_FAILED) {
                    throw new RuntimeException(
                            "Packed quantity could not be updated because the requested quantity is no longer available");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adjusting packed quantities", e);
        }
    }

    /**
     * Increment shipped quantities while enforcing shipped <= packed.
     */
    public void incrementShippedQuantities(Map<Integer, Integer> quantities) {
        if (quantities == null || quantities.isEmpty()) {
            return;
        }

        String sql = """
                UPDATE sales_order_items
                SET shipped_quantity = shipped_quantity + ?
                WHERE id = ?
                  AND shipped_quantity + ? <= packed_quantity
                  AND shipped_quantity + ? <= quantity
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                int lineId = entry.getKey();
                int quantity = entry.getValue();

                if (lineId <= 0 || quantity <= 0) {
                    throw new IllegalArgumentException("Invalid shipped quantity");
                }

                statement.setInt(1, quantity);
                statement.setInt(2, lineId);
                statement.setInt(3, quantity);
                statement.setInt(4, quantity);
                statement.addBatch();
            }

            int[] results = statement.executeBatch();
            for (int result : results) {
                if (result == 0 || result == Statement.EXECUTE_FAILED) {
                    throw new RuntimeException(
                            "Shipped quantity cannot exceed packed quantity");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating shipped quantities", e);
        }
    }

    public boolean isReferencedByPackage(int salesOrderItemId) {
        String sql = """
                SELECT 1
                FROM package_items
                WHERE sales_order_item_id = ?
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, salesOrderItemId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error checking package references for sales order item", e);
        }
    }

    public void deleteByIdAndSalesOrderId(int salesOrderItemId, int salesOrderId) {
        if (isReferencedByPackage(salesOrderItemId)) {
            throw new RuntimeException(
                    "Cannot remove sales order item " + salesOrderItemId
                            + " because it is referenced by a package");
        }

        String sql = """
                DELETE FROM sales_order_items
                WHERE id = ? AND sales_order_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, salesOrderItemId);
            statement.setInt(2, salesOrderId);

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("Sales order item was not found for deletion");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting sales order item", e);
        }
    }

    public void deleteBySalesOrderId(int salesOrderId) {
        String sql = "DELETE FROM sales_order_items WHERE sales_order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, salesOrderId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting sales order items", e);
        }
    }

    private SalesOrderItem mapRow(ResultSet rs) throws SQLException {
        SalesOrderItem orderItem = new SalesOrderItem();
        orderItem.setId(rs.getInt("id"));
        orderItem.setQuantity(rs.getInt("quantity"));
        orderItem.setPackedQuantity(rs.getInt("packed_quantity"));
        orderItem.setShippedQuantity(rs.getInt("shipped_quantity"));
        orderItem.setSellingPrice(rs.getBigDecimal("sellingPrice"));

        Item item = new Item();
        item.setId(rs.getInt("item_id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        item.setReorderLevel(rs.getInt("reorder_level"));
        item.setSellingPrice(rs.getBigDecimal("selling_price"));
        item.setSku(rs.getString("sku"));

        String itemStatus = rs.getString("status");
        item.setStatus(itemStatus == null ? null : ItemStatus.valueOf(itemStatus));
        item.setItemType(rs.getString("item_type"));
        item.setTrackInventory(rs.getBoolean("track_inventory"));
        item.setLength(rs.getDouble("length"));
        item.setWidth(rs.getDouble("width"));
        item.setHeight(rs.getDouble("height"));
        item.setInHandQuantity(rs.getInt("in_hand_quantity"));
        item.setCommittedQuantity(rs.getInt("committed_quantity"));

        int maxStock = rs.getInt("max_stock_quantity");
        item.setMaxStockQuantity(rs.wasNull() ? null : maxStock);
        item.setWeight(rs.getDouble("weight"));

        orderItem.setItem(item);
        return orderItem;
    }
}
