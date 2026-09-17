package com.erpflow.dao;

import com.erpflow.model.Item;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderItemDAO {

    public void save(SalesOrderItem salesOrderItem) {

        String sql = """
                INSERT INTO sales_order_items
                (sales_order_id, item_id, quantity, selling_price)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setInt(
                    1,
                    salesOrderItem
                            .getSalesOrder()
                            .getId()
            );

            statement.setInt(
                    2,
                    salesOrderItem
                            .getItem()
                            .getId()
            );

            statement.setInt(
                    3,
                    salesOrderItem.getQuantity()
            );

            statement.setBigDecimal(
                    4,
                    salesOrderItem.getSellingPrice()
            );

            statement.executeUpdate();

            try (
                    ResultSet rs =
                            statement.getGeneratedKeys()
            ) {

                if (rs.next()) {
                    salesOrderItem.setId(
                            rs.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save sales order item",
                    e
            );
        }
    }


    public List<SalesOrderItem> findBySalesOrder(
            SalesOrder salesOrder
    ) {

        String sql = """
                SELECT
                    soi.id,
                    soi.sales_order_id,
                    soi.item_id,
                    soi.quantity,
                    soi.selling_price,
                    i.name,
                    i.sku,
                    i.description,
                    i.purchase_price,
                    i.selling_price AS item_selling_price,
                    i.reorder_level,
                    i.status
                FROM sales_order_items soi
                JOIN items i
                    ON soi.item_id = i.item_id
                WHERE soi.sales_order_id = ?
                """;

        List<SalesOrderItem> items =
                new ArrayList<>();

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    salesOrder.getId()
            );

            try (
                    ResultSet rs =
                            statement.executeQuery()
            ) {

                while (rs.next()) {

                    SalesOrderItem orderItem =
                            new SalesOrderItem();

                    orderItem.setId(
                            rs.getInt("id")
                    );

                    orderItem.setSalesOrder(
                            salesOrder
                    );

                    Item item =
                            new Item();

                    item.setId(
                            rs.getInt("item_id")
                    );

                    item.setName(
                            rs.getString("name")
                    );

                    item.setSku(
                            rs.getString("sku")
                    );

                    item.setDescription(
                            rs.getString("description")
                    );

                    item.setPurchasePrice(
                            rs.getBigDecimal(
                                    "purchase_price"
                            )
                    );

                    item.setSellingPrice(
                            rs.getBigDecimal(
                                    "item_selling_price"
                            )
                    );

                    item.setReorderLevel(
                            rs.getInt(
                                    "reorder_level"
                            )
                    );

                    item.setStatus(
                            rs.getString("status")
                    );

                    orderItem.setItem(item);

                    orderItem.setQuantity(
                            rs.getInt("quantity")
                    );

                    orderItem.setSellingPrice(
                            rs.getBigDecimal(
                                    "selling_price"
                            )
                    );

                    items.add(orderItem);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch sales order items",
                    e
            );
        }

        return items;
    }
}
