
package com.erpflow.dao;

import com.erpflow.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class SalesReturnDAO {

    public Map<String, Object> lockSalesOrder(int salesOrderId) {
        String sql = """
            SELECT id, customer_id
            FROM sales_orders
            WHERE id = ?
            FOR UPDATE
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, salesOrderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("salesOrderId", rs.getInt("id"));
                result.put("customerId", rs.getInt("customer_id"));
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to lock sales order", e);
        }
    }

    public List<Map<String, Object>> findEligibleLines(int salesOrderId) {
        String sql = """
            SELECT
                soi.id AS salesOrderItemId,
                soi.item_id AS itemId,
                i.name AS itemName,
                i.sku AS sku,
                soi.quantity AS orderedQuantity,
                soi.shipped_quantity AS shippedQuantity,
                soi.sellingPrice AS unitPrice,
                COALESCE(SUM(
                    CASE WHEN sr.sales_return_id IS NOT NULL
                         THEN sri.quantity ELSE 0 END
                ), 0) AS reservedQuantity,
                GREATEST(
                    soi.shipped_quantity - COALESCE(SUM(
                        CASE WHEN sr.sales_return_id IS NOT NULL
                             THEN sri.quantity ELSE 0 END
                    ), 0),
                    0
                ) AS returnableQuantity
            FROM sales_order_items soi
            JOIN items i ON i.item_id = soi.item_id
            LEFT JOIN sales_return_items sri
                ON sri.sales_order_item_id = soi.id
            LEFT JOIN sales_returns sr
                ON sr.sales_return_id = sri.sales_return_id
                AND sr.sales_order_id = soi.sales_order_id
                AND sr.status IN ('CREATED', 'RECEIVED', 'COMPLETED')
            WHERE soi.sales_order_id = ?
            GROUP BY soi.id, soi.item_id, i.name, i.sku,
                     soi.quantity, soi.shipped_quantity, soi.sellingPrice
            ORDER BY soi.id
            """;

        List<Map<String, Object>> lines = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, salesOrderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> line = new LinkedHashMap<>();
                    line.put("salesOrderItemId", rs.getInt("salesOrderItemId"));
                    line.put("itemId", rs.getLong("itemId"));
                    line.put("itemName", rs.getString("itemName"));
                    line.put("sku", rs.getString("sku"));
                    line.put("orderedQuantity", rs.getInt("orderedQuantity"));
                    line.put("shippedQuantity", rs.getInt("shippedQuantity"));
                    line.put("unitPrice", rs.getBigDecimal("unitPrice"));
                    line.put("reservedQuantity", rs.getInt("reservedQuantity"));
                    line.put("returnableQuantity", rs.getInt("returnableQuantity"));
                    lines.add(line);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve returnable order lines", e);
        }

        return lines;
    }

    public Map<Integer, Map<String, Object>> findOrderLinesForUpdate(
            int salesOrderId) {

        String sql = """
            SELECT id, item_id, quantity, shipped_quantity, sellingPrice
            FROM sales_order_items
            WHERE sales_order_id = ?
            ORDER BY id
            FOR UPDATE
            """;

        Map<Integer, Map<String, Object>> lines = new LinkedHashMap<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, salesOrderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> line = new HashMap<>();
                    line.put("salesOrderItemId", rs.getInt("id"));
                    line.put("itemId", rs.getLong("item_id"));
                    line.put("orderedQuantity", rs.getInt("quantity"));
                    line.put("shippedQuantity", rs.getInt("shipped_quantity"));
                    line.put("unitPrice", rs.getBigDecimal("sellingPrice"));
                    lines.put(rs.getInt("id"), line);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to lock sales order lines", e);
        }

        return lines;
    }

    public Map<Integer, Integer> findReservedQuantities(int salesOrderId) {
        String sql = """
            SELECT sri.sales_order_item_id, SUM(sri.quantity) AS reserved_quantity
            FROM sales_return_items sri
            JOIN sales_returns sr
                ON sr.sales_return_id = sri.sales_return_id
            WHERE sr.sales_order_id = ?
              AND sr.status IN ('CREATED', 'RECEIVED', 'COMPLETED')
            GROUP BY sri.sales_order_item_id
            """;

        Map<Integer, Integer> quantities = new HashMap<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, salesOrderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    quantities.put(
                        rs.getInt("sales_order_item_id"),
                        rs.getInt("reserved_quantity")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to calculate reserved return quantities", e);
        }

        return quantities;
    }

    public int insertHeader(
            String returnNumber,
            int salesOrderId,
            int customerId,
            java.sql.Date returnDate,
            BigDecimal totalAmount,
            String reason,
            String notes) {

        String sql = """
            INSERT INTO sales_returns
                (return_number, sales_order_id, customer_id, return_date,
                 status, total_amount, reason, notes)
            VALUES (?, ?, ?, ?, 'CREATED', ?, ?, ?)
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, returnNumber);
            ps.setInt(2, salesOrderId);
            ps.setInt(3, customerId);
            ps.setDate(4, returnDate);
            ps.setBigDecimal(5, totalAmount);
            ps.setString(6, reason);
            ps.setString(7, notes);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }

            throw new SQLException("No ID generated for sales return");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create sales return header", e);
        }
    }

    public void insertReturnItem(
            int salesReturnId,
            int salesOrderItemId,
            long itemId,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal returnAmount,
            String reason) {

        String sql = """
            INSERT INTO sales_return_items
                (sales_return_id, sales_order_item_id, item_id, quantity,
                 unit_price, return_amount, reason)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, salesReturnId);
            ps.setInt(2, salesOrderItemId);
            ps.setLong(3, itemId);
            ps.setInt(4, quantity);
            ps.setBigDecimal(5, unitPrice);
            ps.setBigDecimal(6, returnAmount);
            ps.setString(7, reason);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create sales return item", e);
        }
    }

    public List<Map<String, Object>> findAll() {
        String sql = """
            SELECT sr.sales_return_id, sr.return_number, sr.sales_order_id,
                   sr.customer_id, sr.return_date, sr.status, sr.total_amount,
                   sr.reason, sr.notes, sr.created_at, sr.updated_at,
                   COUNT(sri.sales_return_item_id) AS lineCount
            FROM sales_returns sr
            LEFT JOIN sales_return_items sri
                ON sri.sales_return_id = sr.sales_return_id
            GROUP BY sr.sales_return_id, sr.return_number, sr.sales_order_id,
                     sr.customer_id, sr.return_date, sr.status, sr.total_amount,
                     sr.reason, sr.notes, sr.created_at, sr.updated_at
            ORDER BY sr.created_at DESC
            """;

        List<Map<String, Object>> returns = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = mapHeader(rs);
                row.put("lineCount", rs.getInt("lineCount"));
                returns.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list sales returns", e);
        }

        return returns;
    }

    public Map<String, Object> findById(int salesReturnId) {
        String sql = """
            SELECT sales_return_id, return_number, sales_order_id, customer_id,
                   return_date, status, total_amount, reason, notes,
                   created_at, updated_at
            FROM sales_returns
            WHERE sales_return_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, salesReturnId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Map<String, Object> header = mapHeader(rs);
                header.put("items", findReturnItems(salesReturnId));
                return header;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve sales return", e);
        }
    }

    private List<Map<String, Object>> findReturnItems(int salesReturnId) {
        String sql = """
            SELECT sri.sales_return_item_id, sri.sales_order_item_id,
                   sri.item_id, i.name AS item_name, i.sku,
                   sri.quantity, sri.unit_price, sri.return_amount,
                   sri.reason, sri.item_condition, sri.restocked_quantity,
                   sri.notes, sri.created_at
            FROM sales_return_items sri
            JOIN items i ON i.item_id = sri.item_id
            WHERE sri.sales_return_id = ?
            ORDER BY sri.sales_return_item_id
            """;

        List<Map<String, Object>> items = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, salesReturnId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("salesReturnItemId", rs.getInt("sales_return_item_id"));
                    item.put("salesOrderItemId", rs.getInt("sales_order_item_id"));
                    item.put("itemId", rs.getLong("item_id"));
                    item.put("itemName", rs.getString("item_name"));
                    item.put("sku", rs.getString("sku"));
                    item.put("quantity", rs.getInt("quantity"));
                    item.put("unitPrice", rs.getBigDecimal("unit_price"));
                    item.put("returnAmount", rs.getBigDecimal("return_amount"));
                    item.put("reason", rs.getString("reason"));
                    item.put("itemCondition", rs.getString("item_condition"));
                    item.put("restockedQuantity", rs.getInt("restocked_quantity"));
                    item.put("notes", rs.getString("notes"));
                    item.put("createdAt", rs.getTimestamp("created_at"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve sales return items", e);
        }

        return items;
    }

    public String findStatusForUpdate(int salesReturnId) {
        String sql = """
            SELECT status
            FROM sales_returns
            WHERE sales_return_id = ?
            FOR UPDATE
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, salesReturnId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("status") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to lock sales return", e);
        }
    }

    public void updateStatus(int salesReturnId, String status) {
        String sql = """
            UPDATE sales_returns
            SET status = ?
            WHERE sales_return_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, salesReturnId);

            if (ps.executeUpdate() != 1) {
                throw new IllegalArgumentException("Sales return not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update sales return status", e);
        }
    }

    private Map<String, Object> mapHeader(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("salesReturnId", rs.getInt("sales_return_id"));
        row.put("returnNumber", rs.getString("return_number"));
        row.put("salesOrderId", rs.getInt("sales_order_id"));
        row.put("customerId", rs.getInt("customer_id"));
        row.put("returnDate", rs.getDate("return_date"));
        row.put("status", rs.getString("status"));
        row.put("totalAmount", rs.getBigDecimal("total_amount"));
        row.put("reason", rs.getString("reason"));
        row.put("notes", rs.getString("notes"));
        row.put("createdAt", rs.getTimestamp("created_at"));
        row.put("updatedAt", rs.getTimestamp("updated_at"));
        return row;
    }
}
