package com.erpflow.dao;

import com.erpflow.model.OrderFulfillmentReport;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class OrderFulfillmentReportDAO {


    public List<OrderFulfillmentReport> getReport(
            String fromDate,
            String toDate,
            String status) {

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

                    COALESCE(
                        (
                            SELECT SUM(pi.quantity)
                            FROM package_items pi
                            JOIN packages p
                                ON pi.package_id = p.id
                            WHERE p.sales_order_id = so.id
                              AND pi.item_id = i.item_id
                        ),
                        0
                    ) AS packed_quantity,

                    COALESCE(
                        (
                            SELECT SUM(pi.quantity)
                            FROM package_items pi
                            JOIN packages p
                                ON pi.package_id = p.id
                            WHERE p.sales_order_id = so.id
                              AND pi.item_id = i.item_id
                              AND EXISTS (
                                  SELECT 1
                                  FROM shipment_packages sp
                                  WHERE sp.package_id = p.id
                              )
                        ),
                        0
                    ) AS shipped_quantity

                FROM sales_orders so

                JOIN customers c
                    ON so.customer_id = c.id

                JOIN sales_order_items soi
                    ON soi.sales_order_id = so.id

                JOIN items i
                    ON soi.item_id = i.item_id

                WHERE 1 = 1
                """);


        List<Object> parameters =
                new ArrayList<>();


        // DATE FILTER

        if (fromDate != null &&
                !fromDate.isBlank()) {

            sql.append(
                    " AND DATE(so.orderDate) >= ? "
            );

            parameters.add(
                    Date.valueOf(fromDate)
            );
        }


        if (toDate != null &&
                !toDate.isBlank()) {

            sql.append(
                    " AND DATE(so.orderDate) <= ? "
            );

            parameters.add(
                    Date.valueOf(toDate)
            );
        }


        // STATUS FILTER

        if (status != null &&
                !status.isBlank() &&
                !"ALL".equalsIgnoreCase(status)) {

            sql.append(
                    " AND so.status = ? "
            );

            parameters.add(status);
        }


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
                    so.orderDate DESC,
                    so.id DESC,
                    i.name
                """);


        List<OrderFulfillmentReport> report =
                new ArrayList<>();


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql.toString()
                        )
        ) {


            // BIND PARAMETERS

            for (int i = 0;
                 i < parameters.size();
                 i++) {

                statement.setObject(
                        i + 1,
                        parameters.get(i)
                );
            }


            try (
                    ResultSet rs =
                            statement.executeQuery()
            ) {

                while (rs.next()) {

                    OrderFulfillmentReport row =
                            new OrderFulfillmentReport();


                    row.setOrderId(
                            rs.getInt("order_id")
                    );


                    Timestamp timestamp =
                            rs.getTimestamp(
                                    "order_date"
                            );

                    if (timestamp != null) {

                        row.setOrderDate(
                                timestamp
                                        .toLocalDateTime()
                                        .toLocalDate()
                                        .toString()
                        );
                    }


                    row.setCustomerName(
                            rs.getString(
                                    "customer_name"
                            )
                    );


                    row.setOrderStatus(
                            rs.getString(
                                    "order_status"
                            )
                    );


                    row.setItemId(
                            rs.getInt("item_id")
                    );


                    row.setItemName(
                            rs.getString(
                                    "item_name"
                            )
                    );


                    row.setSku(
                            rs.getString("sku")
                    );


                    int ordered =
                            rs.getInt(
                                    "ordered_quantity"
                            );

                    int packed =
                            rs.getInt(
                                    "packed_quantity"
                            );

                    int shipped =
                            rs.getInt(
                                    "shipped_quantity"
                            );


                    int pending =
                            Math.max(
                                    ordered - shipped,
                                    0
                            );


                    double fulfillment =
                            ordered > 0
                                    ? (
                                        shipped * 100.0
                                      ) / ordered
                                    : 0;


                    row.setOrderedQuantity(
                            ordered
                    );

                    row.setPackedQuantity(
                            packed
                    );

                    row.setShippedQuantity(
                            shipped
                    );

                    row.setPendingQuantity(
                            pending
                    );

                    row.setFulfillmentPercentage(
                            fulfillment
                    );


                    report.add(row);
                }
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to generate order fulfillment report",
                    e
            );
        }


        return report;
    }
    public List<OrderFulfillmentReport> getSummaryReport(
        String fromDate,
        String toDate,
        String status) throws SQLException {

    StringBuilder sql = new StringBuilder("""
        SELECT
            so.id AS order_id,
            so.orderDate AS order_date,
            c.name AS customer_name,
            so.status AS order_status,

            SUM(soi.quantity) AS ordered_quantity,

            COALESCE(
                (
                    SELECT SUM(pi.quantity)
                    FROM package_items pi
                    JOIN packages p
                        ON pi.package_id = p.id
                    WHERE p.sales_order_id = so.id
                ),
                0
            ) AS packed_quantity,

            COALESCE(
                (
                    SELECT SUM(pi.quantity)
                    FROM package_items pi
                    JOIN packages p
                        ON pi.package_id = p.id
                    WHERE p.sales_order_id = so.id
                      AND EXISTS (
                          SELECT 1
                          FROM shipment_packages sp
                          WHERE sp.package_id = p.id
                      )
                ),
                0
            ) AS shipped_quantity

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

    sql.append("""
        GROUP BY
            so.id,
            so.orderDate,
            c.name,
            so.status
        ORDER BY so.orderDate DESC
        """);

    List<OrderFulfillmentReport> reports = new ArrayList<>();

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql.toString())) {

        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                OrderFulfillmentReport report =
                        new OrderFulfillmentReport();

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
public List<OrderFulfillmentReport> getItemReport(
        String fromDate,
        String toDate,
        String status) throws SQLException {

    StringBuilder sql = new StringBuilder("""
        SELECT
            i.item_id AS item_id,
            i.name AS item_name,
            i.sku AS sku,

            SUM(soi.quantity) AS ordered_quantity,

            COALESCE(
                (
                    SELECT SUM(pi.quantity)
                    FROM package_items pi
                    JOIN packages p
                        ON pi.package_id = p.id
                    WHERE pi.item_id = i.item_id
                ),
                0
            ) AS packed_quantity,

            COALESCE(
                (
                    SELECT SUM(pi.quantity)
                    FROM package_items pi
                    JOIN packages p
                        ON pi.package_id = p.id
                    WHERE pi.item_id = i.item_id
                      AND EXISTS (
                          SELECT 1
                          FROM shipment_packages sp
                          WHERE sp.package_id = p.id
                      )
                ),
                0
            ) AS shipped_quantity

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

    sql.append("""
        GROUP BY
            i.item_id,
            i.name,
            i.sku

        ORDER BY
            i.name
        """);

    List<OrderFulfillmentReport> reports = new ArrayList<>();

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps =
                 conn.prepareStatement(sql.toString())) {

        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                OrderFulfillmentReport report =
                        new OrderFulfillmentReport();

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

                int pending =
                        report.getOrderedQuantity()
                        - report.getShippedQuantity();

                report.setPendingQuantity(pending);

                double percentage =
                        report.getOrderedQuantity() == 0
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