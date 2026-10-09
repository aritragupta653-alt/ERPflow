package com.erpflow.dao;

import com.erpflow.model.OrderFulfillmentReport;
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
import java.sql.Timestamp;

public class ReportsDAO {

        // =========================================================
        // DATE HELPERS
        // =========================================================

        private Date parseDate(String value) {

                if (value == null || value.isBlank()) {
                        return null;
                }

                try {

                        return Date.valueOf(value.trim());

                } catch (IllegalArgumentException e) {

                        throw new IllegalArgumentException(
                                        "Invalid date format. Use yyyy-MM-dd.");
                }
        }

        private void addDateFilters(
                        StringBuilder sql,
                        List<Date> dates,
                        String column,
                        Date from,
                        Date to) {

                if (from != null) {

                        sql.append(" AND ")
                                        .append(column)
                                        .append(" >= ?");

                        dates.add(from);
                }

                if (to != null) {

                        sql.append(" AND ")
                                        .append(column)
                                        .append(" <= ?");

                        dates.add(to);
                }
        }

        private void bindDates(
                        PreparedStatement ps,
                        List<Date> dates)
                        throws SQLException {

                for (int i = 0; i < dates.size(); i++) {

                        ps.setDate(
                                        i + 1,
                                        dates.get(i));
                }
        }

        private boolean contains(
                        String value,
                        String search) {

                return search == null
                                || search.isBlank()
                                || (value != null
                                                && value.toLowerCase()
                                                                .contains(search.toLowerCase()));
        }

        private void validateDates(
                        Date from,
                        Date to) {

                if (from != null
                                && to != null
                                && from.after(to)) {

                        throw new IllegalArgumentException(
                                        "From date cannot be after To date.");
                }
        }

        // =========================================================
        // 1. INVENTORY STOCK SUMMARY
        // =========================================================

        public List<Map<String, Object>> getInventoryReport(
                        String search,
                        String stockFilter,
                        String fromDate,
                        String toDate,
                        String sortBy,
                        String sortDirection) {

                Date from = parseDate(fromDate);
                Date to = parseDate(toDate);

                validateDates(from, to);
                String orderBy;

                switch (sortBy == null ? "" : sortBy) {

                        case "available":
                                orderBy = """
                                                (
                                                    COALESCE(i.in_hand_quantity, 0)
                                                    - COALESCE(i.committed_quantity, 0)
                                                )
                                                """;
                                break;

                        case "onHand":
                                orderBy = "COALESCE(inv.quantity, 0)";
                                break;

                        case "stockOut":
                                orderBy = "stockOut";
                                break;

                        case "":
                        default:
                                orderBy = "i.name";
                                break;
                }

                String direction = "desc".equalsIgnoreCase(sortDirection)
                                ? "DESC"
                                : "ASC";

                /*
                 * =====================================================
                 * INVENTORY SOURCE
                 * =====================================================
                 *
                 * On Hand:
                 * items.in_hand_quantity
                 *
                 * Reserved:
                 * items.committed_quantity
                 *
                 * Available:
                 * in_hand_quantity - committed_quantity
                 *
                 * Reorder Level:
                 * items.reorder_level
                 *
                 * Stock In:
                 * inventory_transactions
                 *
                 * Stock Out:
                 * inventory_transactions
                 *
                 * The old INVENTORY table is no longer used.
                 */

                String sql = """
                                SELECT
                                    i.item_id AS itemId,
                                    i.sku AS sku,
                                    i.name AS itemName,

                                    COALESCE(
                                        i.in_hand_quantity,
                                        0
                                    ) AS onHand,

                                    COALESCE(
                                        i.committed_quantity,
                                        0
                                    ) AS reserved,

                                    COALESCE(
                                        i.reorder_level,
                                        0
                                    ) AS reorderLevel,

                                    COALESCE((
                                        SELECT SUM(t.quantity)
                                        FROM inventory_transactions t
                                        WHERE t.item_id = i.item_id
                                          AND UPPER(t.type) = 'STOCK_IN'
                                          AND (
                                              ? IS NULL
                                              OR t.transactionDate >= ?
                                          )
                                          AND (
                                              ? IS NULL
                                              OR t.transactionDate <= ?
                                          )
                                    ), 0) AS stockIn,

                                    COALESCE((
                                        SELECT SUM(t.quantity)
                                        FROM inventory_transactions t
                                        WHERE t.item_id = i.item_id
                                          AND UPPER(t.type) = 'STOCK_OUT'
                                          AND (
                                              ? IS NULL
                                              OR t.transactionDate >= ?
                                          )
                                          AND (
                                              ? IS NULL
                                              OR t.transactionDate <= ?
                                          )
                                    ), 0) AS stockOut

                                FROM items i


                                WHERE i.track_inventory = TRUE
                                ORDER BY %s %s """.formatted(orderBy, direction);

                List<Map<String, Object>> rows = new ArrayList<>();

                try (
                                Connection con = DBConnection.getConnection();

                                PreparedStatement ps = con.prepareStatement(sql)) {

                        // =================================================
                        // STOCK IN DATE PARAMETERS
                        // =================================================

                        ps.setDate(1, from);
                        ps.setDate(2, from);

                        ps.setDate(3, to);
                        ps.setDate(4, to);

                        // =================================================
                        // STOCK OUT DATE PARAMETERS
                        // =================================================

                        ps.setDate(5, from);
                        ps.setDate(6, from);

                        ps.setDate(7, to);
                        ps.setDate(8, to);

                        // =================================================
                        // EXECUTE
                        // =================================================

                        try (ResultSet rs = ps.executeQuery()) {

                                while (rs.next()) {

                                        // =========================================
                                        // CURRENT STOCK
                                        // =========================================

                                        int onHand = rs.getInt("onHand");

                                        int reserved = rs.getInt("reserved");

                                        int reorder = rs.getInt("reorderLevel");

                                        // =========================================
                                        // AVAILABLE STOCK
                                        // =========================================

                                        int available = onHand - reserved;

                                        // =========================================
                                        // STOCK STATUS
                                        // =========================================

                                        String status;

                                        if (available <= 0) {

                                                status = "OUT_OF_STOCK";

                                        } else if (available <= reorder) {

                                                status = "LOW_STOCK";

                                        } else {

                                                status = "IN_STOCK";
                                        }

                                        // =========================================
                                        // ITEM DETAILS
                                        // =========================================

                                        String sku = rs.getString("sku");

                                        String name = rs.getString("itemName");

                                        // =========================================
                                        // SEARCH FILTER
                                        // =========================================

                                        if (!contains(sku, search)
                                                        && !contains(name, search)) {

                                                continue;
                                        }

                                        // =========================================
                                        // STOCK FILTER
                                        // =========================================

                                        if (stockFilter != null
                                                        && !stockFilter.isBlank()
                                                        && !stockFilter.equalsIgnoreCase("all")
                                                        && !stockFilter.equalsIgnoreCase(status)) {

                                                continue;
                                        }

                                        // =========================================
                                        // BUILD RESPONSE
                                        // =========================================

                                        Map<String, Object> row = new LinkedHashMap<>();

                                        row.put(
                                                        "itemId",
                                                        rs.getLong("itemId"));

                                        row.put(
                                                        "sku",
                                                        sku);

                                        row.put(
                                                        "name",
                                                        name);

                                        // On Hand
                                        row.put(
                                                        "quantity",
                                                        onHand);

                                        // Reserved / Committed
                                        row.put(
                                                        "reserved",
                                                        reserved);

                                        // Available
                                        row.put(
                                                        "available",
                                                        available);

                                        // Reorder Level
                                        row.put(
                                                        "reorderLevel",
                                                        reorder);

                                        // Status
                                        row.put(
                                                        "status",
                                                        status);

                                        // Stock In
                                        row.put(
                                                        "stockIn",
                                                        rs.getBigDecimal("stockIn"));

                                        // Stock Out
                                        row.put(
                                                        "stockOut",
                                                        rs.getBigDecimal("stockOut"));

                                        rows.add(row);
                                }
                        }

                        return rows;

                } catch (SQLException e) {

                        throw new RuntimeException(
                                        "Failed to load inventory report",
                                        e);
                }
        }

        // =========================================================
        // 2. SALES PER ITEM
        // =========================================================

        public List<Map<String, Object>> getSalesByItemReport(
                        String search,
                        String fromDate,
                        String toDate,
                        String sortBy,
                        String sortDirection) {

                Date from = parseDate(fromDate);
                Date to = parseDate(toDate);

                validateDates(from, to);
                String orderBy;

                switch (sortBy == null ? "" : sortBy) {

                        case "name":
                                orderBy = "i.name";
                                break;

                        case "revenue":
                                orderBy = "revenue";
                                break;

                        case "quantitySold":
                                orderBy = "quantitySold";
                                break;

                        case "orderCount":
                                orderBy = "orderCount";
                                break;

                        default:
                                orderBy = "revenue";
                                break;
                }

                String direction = "desc".equalsIgnoreCase(sortDirection)
                                ? "DESC"
                                : "ASC";

                StringBuilder sql = new StringBuilder("""
                                SELECT
                                    i.item_id AS itemId,
                                    i.sku AS sku,
                                    i.name AS itemName,

                                    SUM(soi.quantity) AS quantitySold,

                                    SUM(
                                        soi.quantity * soi.sellingPrice
                                    ) AS revenue,

                                    COUNT(DISTINCT so.id) AS orderCount

                                FROM sales_order_items soi

                                JOIN sales_orders so
                                    ON so.id = soi.sales_order_id

                                JOIN items i
                                    ON i.item_id = soi.item_id

                                WHERE UPPER(so.status) = 'SHIPPED'
                                """);

                List<Date> dates = new ArrayList<>();

                addDateFilters(
                                sql,
                                dates,
                                "so.orderDate",
                                from,
                                to);

                sql.append("""
                                GROUP BY
                                    i.item_id,
                                    i.sku,
                                    i.name

                                ORDER BY %s %s

                                """.formatted(orderBy, direction));

                List<Map<String, Object>> rows = new ArrayList<>();

                try (
                                Connection con = DBConnection.getConnection();

                                PreparedStatement ps = con.prepareStatement(
                                                sql.toString())) {

                        bindDates(
                                        ps,
                                        dates);

                        try (ResultSet rs = ps.executeQuery()) {

                                while (rs.next()) {

                                        String sku = rs.getString("sku");

                                        String name = rs.getString("itemName");

                                        if (!contains(sku, search)
                                                        && !contains(name, search)) {

                                                continue;
                                        }

                                        Map<String, Object> row = new LinkedHashMap<>();

                                        row.put(
                                                        "itemId",
                                                        rs.getLong("itemId"));

                                        row.put(
                                                        "sku",
                                                        sku);

                                        row.put(
                                                        "name",
                                                        name);

                                        row.put(
                                                        "quantitySold",
                                                        rs.getBigDecimal(
                                                                        "quantitySold"));

                                        row.put(
                                                        "revenue",
                                                        rs.getBigDecimal(
                                                                        "revenue"));

                                        row.put(
                                                        "orderCount",
                                                        rs.getLong(
                                                                        "orderCount"));

                                        rows.add(row);
                                }
                        }

                        return rows;

                } catch (SQLException e) {

                        throw new RuntimeException(
                                        "Failed to load sales per item report",
                                        e);
                }
        }

        // =========================================================
        // 3. SALES PER CUSTOMER
        // =========================================================

        public List<Map<String, Object>> getSalesByCustomerReport(
                        String search,
                        String fromDate,
                        String toDate,
                        String sortBy,
                        String sortDirection) {

                Date from = parseDate(fromDate);
                Date to = parseDate(toDate);

                validateDates(from, to);
                String orderBy;

                switch (sortBy == null ? "" : sortBy) {

                        case "customerName":
                                orderBy = "c.name";
                                break;

                        case "revenue":
                                orderBy = "revenue";
                                break;

                        case "quantitySold":
                                orderBy = "quantitySold";
                                break;

                        case "orderCount":
                                orderBy = "orderCount";
                                break;

                        default:
                                orderBy = "revenue";
                                break;
                }

                String direction = "desc".equalsIgnoreCase(sortDirection)
                                ? "DESC"
                                : "ASC";

                StringBuilder sql = new StringBuilder("""
                                SELECT
                                    c.id AS customerId,
                                    c.name AS customerName,

                                    COUNT(DISTINCT so.id) AS orderCount,

                                    SUM(
                                        soi.quantity * soi.sellingPrice
                                    ) AS revenue,

                                    SUM(soi.quantity) AS quantitySold

                                FROM customers c

                                JOIN sales_orders so
                                    ON so.customer_id = c.id

                                JOIN sales_order_items soi
                                    ON soi.sales_order_id = so.id

                                WHERE UPPER(so.status) = 'SHIPPED'
                                """);

                List<Date> dates = new ArrayList<>();

                addDateFilters(
                                sql,
                                dates,
                                "so.orderDate",
                                from,
                                to);

                sql.append("""
                                GROUP BY
                                    c.id,
                                    c.name

                                ORDER BY %s %s
                                """.formatted(orderBy, direction));

                List<Map<String, Object>> rows = new ArrayList<>();

                try (
                                Connection con = DBConnection.getConnection();

                                PreparedStatement ps = con.prepareStatement(
                                                sql.toString())) {

                        bindDates(
                                        ps,
                                        dates);

                        try (ResultSet rs = ps.executeQuery()) {

                                while (rs.next()) {

                                        String name = rs.getString(
                                                        "customerName");

                                        if (!contains(
                                                        name,
                                                        search)) {

                                                continue;
                                        }

                                        Map<String, Object> row = new LinkedHashMap<>();

                                        row.put(
                                                        "customerId",
                                                        rs.getLong(
                                                                        "customerId"));

                                        row.put(
                                                        "customerName",
                                                        name);

                                        row.put(
                                                        "orderCount",
                                                        rs.getLong(
                                                                        "orderCount"));

                                        row.put(
                                                        "revenue",
                                                        rs.getBigDecimal(
                                                                        "revenue"));

                                        row.put(
                                                        "quantitySold",
                                                        rs.getBigDecimal(
                                                                        "quantitySold"));

                                        rows.add(row);
                                }
                        }

                        return rows;

                } catch (SQLException e) {

                        throw new RuntimeException(
                                        "Failed to load sales per customer report",
                                        e);
                }
        }

        // =========================================================
        // 4. SALES ORDER SUMMARY
        // =========================================================

        public List<Map<String, Object>> getSalesOrderSummaryReport(
                        String search,
                        String fromDate,
                        String toDate,
                        String sortBy,
                        String sortDirection) {

                Date from = parseDate(fromDate);
                Date to = parseDate(toDate);

                validateDates(from, to);
                String orderBy;

                switch (sortBy == null ? "" : sortBy) {

                        case "orderDate":
                                orderBy = "so.orderDate";
                                break;

                        case "subtotal":
                                orderBy = "subtotal";
                                break;

                        case "itemQuantity":
                                orderBy = "itemQuantity";
                                break;

                        case "customerName":
                                orderBy = "c.name";
                                break;

                        default:
                                orderBy = "so.orderDate";
                                break;
                }

                String direction = "desc".equalsIgnoreCase(sortDirection)
                                ? "DESC"
                                : "ASC";

                StringBuilder sql = new StringBuilder("""
                                SELECT
                                    so.id AS orderId,
                                    so.orderDate AS orderDate,
                                    so.status AS status,
                                    c.name AS customerName,

                                    COALESCE(
                                        SUM(soi.quantity),
                                        0
                                    ) AS itemQuantity,

                                    COALESCE(
                                        SUM(
                                            soi.quantity
                                            * soi.sellingPrice
                                        ),
                                        0
                                    ) AS subtotal

                                FROM sales_orders so

                                JOIN customers c
                                    ON c.id = so.customer_id

                                LEFT JOIN sales_order_items soi
                                    ON soi.sales_order_id = so.id

                                WHERE 1 = 1
                                """);

                List<Date> dates = new ArrayList<>();

                addDateFilters(
                                sql,
                                dates,
                                "so.orderDate",
                                from,
                                to);

                sql.append("""
                                GROUP BY
                                    so.id,
                                    so.orderDate,
                                    so.status,
                                    c.name

                                ORDER BY
                                    %s %s
                                """.formatted(orderBy, direction));

                List<Map<String, Object>> rows = new ArrayList<>();

                try (
                                Connection con = DBConnection.getConnection();

                                PreparedStatement ps = con.prepareStatement(
                                                sql.toString())) {

                        bindDates(
                                        ps,
                                        dates);

                        try (ResultSet rs = ps.executeQuery()) {

                                while (rs.next()) {

                                        String customer = rs.getString(
                                                        "customerName");

                                        String status = rs.getString(
                                                        "status");

                                        String orderId = String.valueOf(
                                                        rs.getLong(
                                                                        "orderId"));

                                        if (!contains(
                                                        customer,
                                                        search)
                                                        && !contains(
                                                                        orderId,
                                                                        search)
                                                        && !contains(
                                                                        status,
                                                                        search)) {

                                                continue;
                                        }

                                        Map<String, Object> row = new LinkedHashMap<>();

                                        row.put(
                                                        "orderId",
                                                        rs.getLong(
                                                                        "orderId"));

                                        row.put(
                                                        "orderDate",
                                                        rs.getDate(
                                                                        "orderDate"));

                                        row.put(
                                                        "status",
                                                        status);

                                        row.put(
                                                        "customerName",
                                                        customer);

                                        row.put(
                                                        "itemQuantity",
                                                        rs.getBigDecimal(
                                                                        "itemQuantity"));

                                        row.put(
                                                        "subtotal",
                                                        rs.getBigDecimal(
                                                                        "subtotal"));

                                        rows.add(row);
                                }
                        }

                        return rows;

                } catch (SQLException e) {

                        throw new RuntimeException(
                                        "Failed to load sales order summary report",
                                        e);
                }
        }

        public List<OrderFulfillmentReport> getFulfillmentReport(
                        String fromDate,
                        String toDate,
                        String status,
                        String sortBy,
                        String sortDirection) {

                StringBuilder sql = new StringBuilder("""
                                SELECT
                                    so.id AS order_id,
                                    so.orderDate AS order_date,
                                    c.name AS customer_name,
                                    so.status AS order_status,

                                    i.item_id AS item_id,
                                    i.name AS item_name,
                                    i.sku AS sku,

                                    SUM(soi.quantity) AS ordered_quantity,

                                    SUM(soi.packed_quantity) AS packed_quantity,

                                    SUM(soi.shipped_quantity) AS shipped_quantity

                                FROM sales_orders so

                                JOIN customers c
                                    ON so.customer_id = c.id

                                JOIN sales_order_items soi
                                    ON soi.sales_order_id = so.id

                                JOIN items i
                                    ON soi.item_id = i.item_id

                                WHERE 1 = 1
                                """);

                List<Object> parameters = new ArrayList<>();

                // DATE FILTER

                if (fromDate != null &&
                                !fromDate.isBlank()) {

                        sql.append(
                                        " AND DATE(so.orderDate) >= ? ");

                        parameters.add(
                                        Date.valueOf(fromDate));
                }

                if (toDate != null &&
                                !toDate.isBlank()) {

                        sql.append(
                                        " AND DATE(so.orderDate) <= ? ");

                        parameters.add(
                                        Date.valueOf(toDate));
                }

                // STATUS FILTER

                if (status != null &&
                                !status.isBlank() &&
                                !"ALL".equalsIgnoreCase(status)) {

                        sql.append(
                                        " AND so.status = ? ");

                        parameters.add(status);
                }
                String orderBy;

                switch (sortBy == null ? "" : sortBy) {

                        case "orderDate":
                                orderBy = "so.orderDate";
                                break;

                        case "fulfillmentPercentage":
                                orderBy = """
                                                CASE
                                                    WHEN SUM(soi.quantity) = 0 THEN 0
                                                    ELSE
                                                        (
                                                            SUM(soi.shipped_quantity) * 100.0
                                                            / SUM(soi.quantity)
                                                        )
                                                END
                                                """;
                                break;

                        case "pendingQuantity":
                                orderBy = """
                                                (
                                                    SUM(soi.quantity)
                                                    - SUM(soi.shipped_quantity)
                                                )
                                                """;
                                break;

                        default:
                                orderBy = "so.orderDate";
                                break;
                }

                String direction = "desc".equalsIgnoreCase(sortDirection)
                                ? "DESC"
                                : "ASC";

                sql.append("""
                                GROUP BY
                                    so.id,
                                    so.orderDate,
                                    c.name,
                                    so.status,
                                    i.item_id,
                                    i.name,
                                    i.sku

                                ORDER BY
                                    %s %s
                                """.formatted(orderBy, direction));

                List<OrderFulfillmentReport> report = new ArrayList<>();

                try (
                                Connection connection = DBConnection.getConnection();

                                PreparedStatement statement = connection.prepareStatement(
                                                sql.toString())) {

                        // BIND PARAMETERS

                        for (int i = 0; i < parameters.size(); i++) {

                                statement.setObject(
                                                i + 1,
                                                parameters.get(i));
                        }

                        try (
                                        ResultSet rs = statement.executeQuery()) {

                                while (rs.next()) {

                                        OrderFulfillmentReport row = new OrderFulfillmentReport();

                                        row.setOrderId(
                                                        rs.getInt("order_id"));

                                        Timestamp timestamp = rs.getTimestamp(
                                                        "order_date");

                                        if (timestamp != null) {

                                                row.setOrderDate(
                                                                timestamp
                                                                                .toLocalDateTime()
                                                                                .toLocalDate()
                                                                                .toString());
                                        }

                                        row.setCustomerName(
                                                        rs.getString(
                                                                        "customer_name"));

                                        row.setOrderStatus(
                                                        rs.getString(
                                                                        "order_status"));

                                        row.setItemId(
                                                        rs.getInt("item_id"));

                                        row.setItemName(
                                                        rs.getString(
                                                                        "item_name"));

                                        row.setSku(
                                                        rs.getString("sku"));

                                        int ordered = rs.getInt(
                                                        "ordered_quantity");

                                        int packed = rs.getInt(
                                                        "packed_quantity");

                                        int shipped = rs.getInt(
                                                        "shipped_quantity");

                                        int pending = Math.max(
                                                        ordered - shipped,
                                                        0);

                                        double fulfillment = ordered > 0
                                                        ? (shipped * 100.0) / ordered
                                                        : 0;

                                        row.setOrderedQuantity(
                                                        ordered);

                                        row.setPackedQuantity(
                                                        packed);

                                        row.setShippedQuantity(
                                                        shipped);

                                        row.setPendingQuantity(
                                                        pending);

                                        row.setFulfillmentPercentage(
                                                        fulfillment);

                                        report.add(row);
                                }
                        }

                } catch (SQLException e) {

                        throw new RuntimeException(
                                        "Failed to generate order fulfillment report",
                                        e);
                }

                return report;
        }

        public List<OrderFulfillmentReport> getFulfillmentSummaryReport(
                        String fromDate,
                        String toDate,
                        String status,
                        String sortBy,
                        String sortDirection) throws SQLException {

                StringBuilder sql = new StringBuilder("""
                                SELECT
                                    so.id AS order_id,
                                    so.orderDate AS order_date,
                                    c.name AS customer_name,
                                    so.status AS order_status,

                                    SUM(soi.quantity) AS ordered_quantity,

                                    SUM(soi.packed_quantity) AS packed_quantity,

                                    SUM(soi.shipped_quantity) AS shipped_quantity

                                FROM sales_orders so

                                JOIN customers c
                                    ON so.customer_id = c.id

                                JOIN sales_order_items soi
                                    ON soi.sales_order_id = so.id

                                WHERE 1 = 1
                                """);

                List<Object> params = new ArrayList<>();

                if (fromDate != null && !fromDate.isBlank()) {
                        sql.append(" AND so.orderDate >= ?");
                        params.add(fromDate);
                }

                if (toDate != null && !toDate.isBlank()) {
                        sql.append(" AND so.orderDate <= ?");
                        params.add(toDate);
                }

                if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                        sql.append(" AND so.status = ?");
                        params.add(status);
                }
                String orderBy;

                switch (sortBy == null ? "" : sortBy) {

                        case "orderDate":
                                orderBy = "so.orderDate";
                                break;

                        case "orderId":
                                orderBy = "so.id";
                                break;

                        case "customerName":
                                orderBy = "c.name";
                                break;

                        default:
                                orderBy = "so.orderDate";
                                break;
                }

                String direction = "desc".equalsIgnoreCase(sortDirection)
                                ? "DESC"
                                : "ASC";

                sql.append("""
                                GROUP BY
                                    so.id,
                                    so.orderDate,
                                    c.name,
                                    so.status
                                ORDER BY %s %s
                                """.formatted(orderBy, direction));

                List<OrderFulfillmentReport> reports = new ArrayList<>();

                try (Connection conn = DBConnection.getConnection();
                                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

                        for (int i = 0; i < params.size(); i++) {
                                ps.setObject(i + 1, params.get(i));
                        }

                        try (ResultSet rs = ps.executeQuery()) {

                                while (rs.next()) {

                                        OrderFulfillmentReport report = new OrderFulfillmentReport();

                                        report.setOrderId(rs.getInt("order_id"));
                                        report.setOrderDate(rs.getString("order_date"));
                                        report.setCustomerName(rs.getString("customer_name"));
                                        report.setOrderStatus(rs.getString("order_status"));

                                        report.setOrderedQuantity(
                                                        rs.getInt("ordered_quantity"));

                                        report.setPackedQuantity(
                                                        rs.getInt("packed_quantity"));

                                        report.setShippedQuantity(
                                                        rs.getInt("shipped_quantity"));

                                        reports.add(report);
                                }
                        }
                }

                return reports;
        }

        public List<OrderFulfillmentReport> getItemFulfillmentReport(
                        String fromDate,
                        String toDate,
                        String status,
                        String sortBy,
                        String sortDirection) throws SQLException {

                StringBuilder sql = new StringBuilder("""
                                SELECT
                                    i.item_id AS item_id,
                                    i.name AS item_name,
                                    i.sku AS sku,

                                    SUM(soi.quantity) AS ordered_quantity,

                                    SUM(soi.packed_quantity) AS packed_quantity,

                                    SUM(soi.shipped_quantity) AS shipped_quantity

                                FROM sales_orders so

                                JOIN sales_order_items soi
                                    ON soi.sales_order_id = so.id

                                JOIN items i
                                    ON soi.item_id = i.item_id

                                WHERE 1 = 1
                                """);

                List<Object> params = new ArrayList<>();

                if (fromDate != null && !fromDate.isBlank()) {
                        sql.append(" AND so.orderDate >= ?");
                        params.add(fromDate);
                }

                if (toDate != null && !toDate.isBlank()) {
                        sql.append(" AND so.orderDate <= ?");
                        params.add(toDate);
                }

                if (status != null
                                && !status.isBlank()
                                && !"ALL".equalsIgnoreCase(status)) {

                        sql.append(" AND so.status = ?");
                        params.add(status);
                }
                String orderBy;

                switch (sortBy == null ? "" : sortBy) {

                        case "itemName":
                                orderBy = "i.name";
                                break;

                        case "fulfillmentPercentage":
                                orderBy = """
                                                CASE
                                                    WHEN SUM(soi.quantity) = 0 THEN 0
                                                    ELSE
                                                        (
                                                            SUM(soi.shipped_quantity) * 100.0
                                                            / SUM(soi.quantity)
                                                        )
                                                END
                                                """;
                                break;

                        case "pendingQuantity":
                                orderBy = """
                                                (
                                                    SUM(soi.quantity)
                                                    - SUM(soi.shipped_quantity)
                                                )
                                                """;
                                break;

                        case "shippedQuantity":
                                orderBy = "SUM(soi.shipped_quantity)";
                                break;

                        default:
                                orderBy = "i.name";
                                break;
                }

                String direction = "desc".equalsIgnoreCase(sortDirection)
                                ? "DESC"
                                : "ASC";

                sql.append("""
                                GROUP BY
                                    i.item_id,
                                    i.name,
                                    i.sku

                                ORDER BY
                                    %s %s
                                """.formatted(orderBy, direction));

                List<OrderFulfillmentReport> reports = new ArrayList<>();

                try (Connection conn = DBConnection.getConnection();
                                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

                        for (int i = 0; i < params.size(); i++) {
                                ps.setObject(i + 1, params.get(i));
                        }

                        try (ResultSet rs = ps.executeQuery()) {

                                while (rs.next()) {

                                        OrderFulfillmentReport report = new OrderFulfillmentReport();

                                        report.setItemId(
                                                        rs.getInt("item_id"));

                                        report.setItemName(
                                                        rs.getString("item_name"));

                                        report.setSku(
                                                        rs.getString("sku"));

                                        report.setOrderedQuantity(
                                                        rs.getInt("ordered_quantity"));

                                        report.setPackedQuantity(
                                                        rs.getInt("packed_quantity"));

                                        report.setShippedQuantity(
                                                        rs.getInt("shipped_quantity"));

                                        int pending = report.getOrderedQuantity()
                                                        - report.getShippedQuantity();

                                        report.setPendingQuantity(pending);

                                        double percentage = report.getOrderedQuantity() == 0
                                                        ? 0
                                                        : ((double) report.getShippedQuantity()
                                                                        / report.getOrderedQuantity()) * 100;

                                        report.setFulfillmentPercentage(percentage);

                                        reports.add(report);
                                }
                        }
                }

                return reports;
        }
}