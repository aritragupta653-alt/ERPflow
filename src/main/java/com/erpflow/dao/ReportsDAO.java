
package com.erpflow.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.erpflow.util.DBConnection;

public class ReportsDAO {

    /*
     * Convert an optional date string into java.sql.Date.
     * Blank or null dates mean that no date restriction is applied.
     */
    private Date parseOptionalDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Date.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid date. Expected format: yyyy-MM-dd"
            );
        }
    }

    /*
     * Add a date condition only when the date exists.
     * This avoids comparing a DATE column with an empty string.
     */
    private void appendDateCondition(
            StringBuilder sql,
            List<Date> parameters,
            String column,
            Date date,
            String operator) {

        if (date != null) {
            sql.append(" AND ").append(column).append(" ")
               .append(operator).append(" ?");
            parameters.add(date);
        }
    }

    private void bindDates(
            PreparedStatement ps,
            List<Date> dates,
            int startIndex) throws SQLException {

        int index = startIndex;

        for (Date date : dates) {
            ps.setDate(index++, date);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean matchesSearch(String value, String search) {
        return search.isEmpty()
            || (value != null
                && value.toLowerCase().contains(search.toLowerCase()));
    }

    private BigDecimal decimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        return new BigDecimal(value.toString());
    }

    /*
     * INVENTORY STOCK SUMMARY
     *
     * Returns current stock, reserved stock, available stock,
     * stock-in and stock-out totals for the optional date range.
     *
     * Expected transaction types: STOCK_IN and STOCK_OUT.
     */
    public List<Map<String, Object>> getInventoryReport(
            String search,
            String stockFilter,
            String fromDate,
            String toDate) {

        Date from = parseOptionalDate(fromDate);
        Date to = parseOptionalDate(toDate);

        if (from != null && to != null && from.after(to)) {
            throw new IllegalArgumentException(
                "From date cannot be after To date."
            );
        }

        StringBuilder sql = new StringBuilder();

        sql.append(
            "SELECT " +
            " i.item_id AS itemId, " +
            " i.sku AS sku, " +
            " i.name AS itemName, " +
            " COALESCE(inv.quantity, 0) AS onHand, " +
            " COALESCE(inv.committedquantity, 0) AS reserved, " +
            " COALESCE(i.reorder_level, 0) AS reorderLevel, " +

            " COALESCE(( " +
            "   SELECT SUM(t.quantity) " +
            "   FROM inventory_transactions t " +
            "   WHERE t.item_id = i.item_id " +
            "     AND UPPER(t.type) = 'STOCK_IN' "
        );

        List<Date> stockInDates = new ArrayList<>();

        appendDateCondition(
            sql, stockInDates, "t.transactionDate", from, ">="
        );

        appendDateCondition(
            sql, stockInDates, "t.transactionDate", to, "<="
        );

        sql.append(
            " ), 0) AS stockIn, " +

            " COALESCE(( " +
            "   SELECT SUM(t.quantity) " +
            "   FROM inventory_transactions t " +
            "   WHERE t.item_id = i.item_id " +
            "     AND UPPER(t.type) = 'STOCK_OUT' "
        );

        List<Date> stockOutDates = new ArrayList<>();

        appendDateCondition(
            sql, stockOutDates, "t.transactionDate", from, ">="
        );

        appendDateCondition(
            sql, stockOutDates, "t.transactionDate", to, "<="
        );

        sql.append(
            " ), 0) AS stockOut " +
            "FROM items i " +
            "LEFT JOIN inventory inv ON inv.item_id = i.item_id " +
            "WHERE 1 = 1 "
        );

        List<Date> allDates = new ArrayList<>();
        allDates.addAll(stockInDates);
        allDates.addAll(stockOutDates);

        sql.append(" ORDER BY i.name ASC");

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            bindDates(ps, allDates, 1);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int onHand = rs.getInt("onHand");
                    int reserved = rs.getInt("reserved");
                    int available = onHand - reserved;
                    int reorderLevel = rs.getInt("reorderLevel");

                    String sku = rs.getString("sku");
                    String itemName = rs.getString("itemName");

                    String status;

                    if (onHand <= 0) {
                        status = "OUT_OF_STOCK";
                    } else if (onHand <= reorderLevel) {
                        status = "LOW_STOCK";
                    } else {
                        status = "IN_STOCK";
                    }

                    Map<String, Object> row = new LinkedHashMap<>();

                    row.put("itemId", rs.getLong("itemId"));
                    row.put("sku", sku);
                    row.put("name", itemName);
                    row.put("quantity", onHand);
                    row.put("reserved", reserved);
                    row.put("available", available);
                    row.put("reorderLevel", reorderLevel);
                    row.put("status", status);
                    row.put("stockIn", rs.getBigDecimal("stockIn"));
                    row.put("stockOut", rs.getBigDecimal("stockOut"));

                    // Apply search and status filtering in Java.
                    if (!matchesSearch(sku, normalize(search))
                            && !matchesSearch(itemName, normalize(search))) {
                        continue;
                    }

                    String filter = normalize(stockFilter);

                    if (!filter.isEmpty()
                            && !filter.equalsIgnoreCase("all")
                            && !filter.equalsIgnoreCase(status)) {
                        continue;
                    }

                    rows.add(row);
                }
            }

            return rows;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load inventory report", e);
        }
    }

    /*
     * SALES PER ITEM
     *
     * A sale is counted only when the sales order status is SHIPPED.
     */
    public List<Map<String, Object>> getSalesByItemReport(
            String search,
            String fromDate,
            String toDate) {

        Date from = parseOptionalDate(fromDate);
        Date to = parseOptionalDate(toDate);

        if (from != null && to != null && from.after(to)) {
            throw new IllegalArgumentException(
                "From date cannot be after To date."
            );
        }

        StringBuilder sql = new StringBuilder();

        sql.append(
            "SELECT " +
            " i.item_id AS itemId, " +
            " i.sku AS sku, " +
            " i.name AS itemName, " +
            " SUM(soi.quantity) AS quantitySold, " +
            " SUM(soi.quantity * soi.sellingPrice) AS revenue, " +
            " COUNT(DISTINCT so.id) AS orderCount " +
            "FROM sales_order_items soi " +
            "JOIN sales_orders so ON so.id = soi.sales_order_id " +
            "JOIN items i ON i.item_id = soi.item_id " +
            "WHERE UPPER(so.status) = 'SHIPPED' "
        );

        List<Date> dates = new ArrayList<>();

        appendDateCondition(
            sql, dates, "so.orderDate", from, ">="
        );

        appendDateCondition(
            sql, dates, "so.orderDate", to, "<="
        );

        sql.append(
            " GROUP BY i.item_id, i.sku, i.name " +
            " ORDER BY revenue DESC"
        );

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            bindDates(ps, dates, 1);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sku = rs.getString("sku");
                    String itemName = rs.getString("itemName");

                    if (!matchesSearch(sku, normalize(search))
                            && !matchesSearch(itemName, normalize(search))) {
                        continue;
                    }

                    Map<String, Object> row = new LinkedHashMap<>();

                    row.put("itemId", rs.getLong("itemId"));
                    row.put("sku", sku);
                    row.put("name", itemName);
                    row.put("quantitySold", rs.getBigDecimal("quantitySold"));
                    row.put("revenue", rs.getBigDecimal("revenue"));
                    row.put("orderCount", rs.getLong("orderCount"));

                    rows.add(row);
                }
            }

            return rows;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load sales per item report", e);
        }
    }

    /*
     * SALES PER CUSTOMER
     *
     * Only SHIPPED sales orders contribute to customer totals.
     */
    public List<Map<String, Object>> getSalesByCustomerReport(
            String search,
            String fromDate,
            String toDate) {

        Date from = parseOptionalDate(fromDate);
        Date to = parseOptionalDate(toDate);

        if (from != null && to != null && from.after(to)) {
            throw new IllegalArgumentException(
                "From date cannot be after To date."
            );
        }

        StringBuilder sql = new StringBuilder();

        sql.append(
            "SELECT " +
            " c.id AS customerId, " +
            " c.name AS customerName, " +
            " COUNT(DISTINCT so.id) AS orderCount, " +
            " SUM(soi.quantity * soi.sellingPrice) AS revenue, " +
            " SUM(soi.quantity) AS quantitySold " +
            "FROM customers c " +
            "JOIN sales_orders so ON so.customer_id = c.id " +
            "JOIN sales_order_items soi ON soi.sales_order_id = so.id " +
            "WHERE UPPER(so.status) = 'SHIPPED' "
        );

        List<Date> dates = new ArrayList<>();

        appendDateCondition(
            sql, dates, "so.orderDate", from, ">="
        );

        appendDateCondition(
            sql, dates, "so.orderDate", to, "<="
        );

        sql.append(
            " GROUP BY c.id, c.name " +
            " ORDER BY revenue DESC"
        );

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            bindDates(ps, dates, 1);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String customerName = rs.getString("customerName");

                    if (!matchesSearch(customerName, normalize(search))) {
                        continue;
                    }

                    Map<String, Object> row = new LinkedHashMap<>();

                    row.put("customerId", rs.getLong("customerId"));
                    row.put("customerName", customerName);
                    row.put("orderCount", rs.getLong("orderCount"));
                    row.put("revenue", rs.getBigDecimal("revenue"));
                    row.put("quantitySold", rs.getBigDecimal("quantitySold"));

                    rows.add(row);
                }
            }

            return rows;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(
                "Failed to load sales per customer report", e
            );
        }
    }
}