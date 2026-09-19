package com.erpflow.dao;

import com.erpflow.model.Item;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderItemDAO {

    public void save(SalesOrderItem salesOrderItem) {

        String sql = """
                INSERT INTO sales_order_items
                (
                    quantity,
                    sellingPrice,
                    item_id,
                    sales_order_id
                )
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setInt(
                    1,
                    salesOrderItem.getQuantity()
            );

            statement.setBigDecimal(
                    2,
                    salesOrderItem.getSellingPrice()
            );

            statement.setInt(
                    3,
                    salesOrderItem.getItem().getId()
            );

            statement.setInt(
                    4,
                    salesOrderItem.getSalesOrder().getId()
            );

            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {

                if (rs.next()) {

                    salesOrderItem.setId(
                            rs.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error saving sales order item",
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
                    soi.quantity,
                    soi.sellingPrice,
                    soi.item_id,
                    soi.sales_order_id,

                    i.name,
                    i.description,
                    i.purchase_price,
                    i.reorder_level,
                    i.selling_price,
                    i.sku,
                    i.status

                FROM sales_order_items soi

                JOIN items i
                    ON soi.item_id = i.item_id

                WHERE soi.sales_order_id = ?
                """;

        List<SalesOrderItem> items =
                new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    salesOrder.getId()
            );

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {

                    SalesOrderItem orderItem =
                            new SalesOrderItem();

                    orderItem.setId(
                            rs.getInt("id")
                    );

                    orderItem.setQuantity(
                            rs.getInt("quantity")
                    );

                    orderItem.setSellingPrice(
                            rs.getBigDecimal(
                                    "sellingPrice"
                            )
                    );


                    Item item =
                            new Item();

                    item.setId(
                            rs.getInt("item_id")
                    );

                    item.setName(
                            rs.getString("name")
                    );

                    item.setDescription(
                            rs.getString("description")
                    );

                    item.setPurchasePrice(
                            rs.getBigDecimal(
                                    "purchase_price"
                            )
                    );

                    item.setReorderLevel(
                            rs.getInt(
                                    "reorder_level"
                            )
                    );

                    item.setSellingPrice(
                            rs.getBigDecimal(
                                    "selling_price"
                            )
                    );

                    item.setSku(
                            rs.getString("sku")
                    );

                    item.setStatus(
                            rs.getString("status")
                    );


                    orderItem.setItem(item);

                    orderItem.setSalesOrder(
                            salesOrder
                    );

                    items.add(orderItem);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching sales order items",
                    e
            );
        }

        return items;
    }
}
