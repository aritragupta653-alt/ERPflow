
package com.erpflow.dao;

import com.erpflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportsDAO {

    private Date parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Date.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid date format. Use yyyy-MM-dd."
            );
        }
    }

    private void addDateFilters(
            StringBuilder sql,
            List<Date> dates,
            String column,
            Date from,
            Date to) {

        if (from != null) {
            sql.append(" AND ").append(column).append(" >= ?");
            dates.add(from);
        }

        if (to != null) {
            sql.append(" AND ").append(column).append(" <= ?");
            dates.add(to);
        }
    }

    private void bindDates(
            PreparedStatement ps,
            List<Date> dates) throws SQLException {

        for (int i = 0; i < dates.size(); i++) {
            ps.setDate(i + 1, dates.get(i));
        }
    }

    private boolean contains(String value, String search) {
        return search == null || search.isBlank()
                || (value != null
                && value.toLowerCase().contains(search.toLowerCase()));
    }

    private void validateDates(Date from, Date to) {
        if (from != null && to != null && from.after(to)) {
            throw new IllegalArgumentException(
                    "From date cannot be after To date."
            );
        }
    }

    // =========================================================
    // 1. INVENTORY STOCK SUMMARY
    // =========================================================

    public List<Map<String, Object>> getInventoryReport(
            String search,
            String stockFilter,
            String fromDate,
            String toDate) {

        Date from = parseDate(fromDate);
        Date to = parseDate(toDate);
        validateDates(from, to);

        String sql = """
                SELECT
                    i.item_id AS itemId,
                    i.sku AS sku,
                    i.name AS itemName,
                    COALESCE(inv.quantity, 0) AS onHand,
                    COALESCE(inv.committedquantity, 0) AS reserved,
                    COALESCE(i.reorder_level, 0) AS reorderLevel,
                    COALESCE((
                        SELECT SUM(t.quantity)
                        FROM inventory_transactions t
                        WHERE t.item_id = i.item_id
                          AND UPPER(t.type) = 'STOCK_IN'
                          AND (? IS NULL OR t.transactionDate >= ?)
                          AND (? IS NULL OR t.transactionDate <= ?)
                    ), 0) AS stockIn,
                    COALESCE((
                        SELECT SUM(t.quantity)
                        FROM inventory_transactions t
                        WHERE t.item_id = i.item_id
                          AND UPPER(t.type) = 'STOCK_OUT'
                          AND (? IS NULL OR t.transactionDate >= ?)
                          AND (? IS NULL OR t.transactionDate <= ?)
                    ), 0) AS stockOut
                FROM items i
                
                LEFT JOIN inventory inv ON inv.item_id = i.item_id
                WHERE i.track_inventory = TRUE
                ORDER BY i.name 
                """;

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, from);
            ps.setDate(2, from);
            ps.setDate(3, to);
            ps.setDate(4, to);
            ps.setDate(5, from);
            ps.setDate(6, from);
            ps.setDate(7, to);
            ps.setDate(8, to);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int onHand = rs.getInt("onHand");
                    int reserved = rs.getInt("reserved");
                    int reorder = rs.getInt("reorderLevel");
                    int available = onHand - reorder;

                    String status;

                    if (available<=0) {
                        status = "OUT_OF_STOCK";
                    } else if (available<= reorder) {
                        status = "LOW_STOCK";
                    } else {
                        status = "IN_STOCK";
                    }

                    String sku = rs.getString("sku");
                    String name = rs.getString("itemName");

                    if (!contains(sku, search) && !contains(name, search)) {
                        continue;
                    }

                    if (stockFilter != null
                            && !stockFilter.isBlank()
                            && !stockFilter.equalsIgnoreCase("all")
                            && !stockFilter.equalsIgnoreCase(status)) {
                        continue;
                    }

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("itemId", rs.getLong("itemId"));
                    row.put("sku", sku);
                    row.put("name", name);
                    row.put("quantity", onHand);
                    row.put("reserved", reserved);
                    row.put("available", onHand - reserved);
                    row.put("reorderLevel", reorder);
                    row.put("status", status);
                    row.put("stockIn", rs.getBigDecimal("stockIn"));
                    row.put("stockOut", rs.getBigDecimal("stockOut"));

                    rows.add(row);
                }
            }

            return rows;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load inventory report", e);
        }
    }

    // =========================================================
    // 2. SALES PER ITEM
    // =========================================================

    public List<Map<String, Object>> getSalesByItemReport(
            String search,
            String fromDate,
            String toDate) {

        Date from = parseDate(fromDate);
        Date to = parseDate(toDate);
        validateDates(from, to);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    i.item_id AS itemId,
                    i.sku AS sku,
                    i.name AS itemName,
                    SUM(soi.quantity) AS quantitySold,
                    SUM(soi.quantity * soi.sellingPrice) AS revenue,
                    COUNT(DISTINCT so.id) AS orderCount
                FROM sales_order_items soi
                JOIN sales_orders so ON so.id = soi.sales_order_id
                JOIN items i ON i.item_id = soi.item_id
                WHERE UPPER(so.status) = 'SHIPPED'
                """);

        List<Date> dates = new ArrayList<>();
        addDateFilters(sql, dates, "so.orderDate", from, to);

        sql.append("""
                GROUP BY i.item_id, i.sku, i.name
                ORDER BY revenue DESC
                """);

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            bindDates(ps, dates);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sku = rs.getString("sku");
                    String name = rs.getString("itemName");

                    if (!contains(sku, search) && !contains(name, search)) {
                        continue;
                    }

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("itemId", rs.getLong("itemId"));
                    row.put("sku", sku);
                    row.put("name", name);
                    row.put("quantitySold", rs.getBigDecimal("quantitySold"));
                    row.put("revenue", rs.getBigDecimal("revenue"));
                    row.put("orderCount", rs.getLong("orderCount"));

                    rows.add(row);
                }
            }

            return rows;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load sales per item report", e
            );
        }
    }

    // =========================================================
    // 3. SALES PER CUSTOMER
    // =========================================================

    public List<Map<String, Object>> getSalesByCustomerReport(
            String search,
            String fromDate,
            String toDate) {

        Date from = parseDate(fromDate);
        Date to = parseDate(toDate);
        validateDates(from, to);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    c.id AS customerId,
                    c.name AS customerName,
                    COUNT(DISTINCT so.id) AS orderCount,
                    SUM(soi.quantity * soi.sellingPrice) AS revenue,
                    SUM(soi.quantity) AS quantitySold
                FROM customers c
                JOIN sales_orders so ON so.customer_id = c.id
                JOIN sales_order_items soi ON soi.sales_order_id = so.id
                WHERE UPPER(so.status) = 'SHIPPED'
                """);

        List<Date> dates = new ArrayList<>();
        addDateFilters(sql, dates, "so.orderDate", from, to);

        sql.append("""
                GROUP BY c.id, c.name
                ORDER BY revenue DESC
                """);

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            bindDates(ps, dates);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("customerName");

                    if (!contains(name, search)) {
                        continue;
                    }

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("customerId", rs.getLong("customerId"));
                    row.put("customerName", name);
                    row.put("orderCount", rs.getLong("orderCount"));
                    row.put("revenue", rs.getBigDecimal("revenue"));
                    row.put("quantitySold", rs.getBigDecimal("quantitySold"));

                    rows.add(row);
                }
            }

            return rows;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load sales per customer report", e
            );
        }
    }

    // =========================================================
    // 4. SALES ORDER SUMMARY
    // =========================================================

    public List<Map<String, Object>> getSalesOrderSummaryReport(
            String search,
            String fromDate,
            String toDate) {

        Date from = parseDate(fromDate);
        Date to = parseDate(toDate);
        validateDates(from, to);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    so.id AS orderId,
                    so.orderDate AS orderDate,
                    so.status AS status,
                    c.name AS customerName,
                    COALESCE(SUM(soi.quantity), 0) AS itemQuantity,
                    COALESCE(SUM(soi.quantity * soi.sellingPrice), 0) AS subtotal
                FROM sales_orders so
                JOIN customers c ON c.id = so.customer_id
                LEFT JOIN sales_order_items soi ON soi.sales_order_id = so.id
                WHERE 1 = 1
                """);

        List<Date> dates = new ArrayList<>();
        addDateFilters(sql, dates, "so.orderDate", from, to);

        sql.append("""
                GROUP BY so.id, so.orderDate, so.status, c.name
                ORDER BY so.orderDate DESC, so.id DESC
                """);

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            bindDates(ps, dates);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String customer = rs.getString("customerName");
                    String status = rs.getString("status");
                    String orderId = String.valueOf(rs.getLong("orderId"));

                    if (!contains(customer, search)
                            && !contains(orderId, search)
                            && !contains(status, search)) {
                        continue;
                    }

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("orderId", rs.getLong("orderId"));
                    row.put("orderDate", rs.getDate("orderDate"));
                    row.put("status", status);
                    row.put("customerName", customer);
                    row.put("itemQuantity", rs.getBigDecimal("itemQuantity"));
                    row.put("subtotal", rs.getBigDecimal("subtotal"));

                    rows.add(row);
                }
            }

            return rows;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load sales order summary report", e
            );
        }
    }
}