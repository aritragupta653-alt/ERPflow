package com.erpflow.dao;

import com.erpflow.model.Item;
import com.erpflow.model.PurchaseOrder;
import com.erpflow.model.PurchaseOrderItem;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderItemDAO {

    public void save(
            PurchaseOrderItem purchaseOrderItem
    ) {

        String sql =
                "INSERT INTO purchase_order_items " +
                "(purchase_order_id, item_id, quantity, purchasePrice) " +
                "VALUES (?, ?, ?, ?)";

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
                    purchaseOrderItem
                            .getPurchaseOrder()
                            .getId()
            );

            statement.setInt(
                    2,
                    purchaseOrderItem
                            .getItem()
                            .getId()
            );

            statement.setInt(
                    3,
                    purchaseOrderItem.getQuantity()
            );

            statement.setBigDecimal(
                    4,
                    purchaseOrderItem
                            .getPurchasePrice()
            );

            statement.executeUpdate();

            try (ResultSet rs =
                         statement.getGeneratedKeys()) {

                if (rs.next()) {

                    purchaseOrderItem.setId(
                            rs.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save purchase order item",
                    e
            );
        }
    }


    public List<PurchaseOrderItem> findByPurchaseOrder(
            PurchaseOrder purchaseOrder
    ) {

        String sql =
                "SELECT poi.id, " +
                "poi.quantity, " +
                "poi.purchasePrice, " +
                "i.item_id, " +
                "i.name, " +
                "i.description, " +
                "i.purchase_price, " +
                "i.reorder_level, " +
                "i.selling_price, " +
                "i.sku, " +
                "i.status " +
                "FROM purchase_order_items poi " +
                "JOIN items i " +
                "ON poi.item_id = i.item_id " +
                "WHERE poi.purchase_order_id = ?";

        List<PurchaseOrderItem> items =
                new ArrayList<>();

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    purchaseOrder.getId()
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                while (rs.next()) {

                    PurchaseOrderItem orderItem =
                            new PurchaseOrderItem();

                    orderItem.setId(
                            rs.getInt("id")
                    );

                    orderItem.setQuantity(
                            rs.getInt("quantity")
                    );

                    orderItem.setPurchasePrice(
                            rs.getBigDecimal(
                                    "purchasePrice"
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

                    orderItem.setPurchaseOrder(
                            purchaseOrder
                    );

                    items.add(orderItem);
                }
            }

            return items;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch purchase order items",
                    e
            );
        }
    }
}