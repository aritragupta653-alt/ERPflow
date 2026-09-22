
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

    // =========================================================
    // SAVE SALES ORDER ITEM
    // =========================================================

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
                    salesOrderItem.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error saving sales order item",
                    e
            );
        }
    }


    // =========================================================
    // FIND ITEMS BY SALES ORDER
    // =========================================================

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
                    i.status,
                    i.item_type,
                    i.track_inventory

                FROM sales_order_items soi

                JOIN items i
                    ON soi.item_id = i.item_id

                WHERE soi.sales_order_id = ?

                ORDER BY soi.id
                """;

        List<SalesOrderItem> items = new ArrayList<>();

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
                            rs.getBigDecimal("sellingPrice")
                    );

                    // Build the associated item.

                    Item item = new Item();

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
                            rs.getBigDecimal("purchase_price")
                    );

                    item.setReorderLevel(
                            rs.getInt("reorder_level")
                    );

                    item.setSellingPrice(
                            rs.getBigDecimal("selling_price")
                    );

                    item.setSku(
                            rs.getString("sku")
                    );

                    item.setStatus(
                            rs.getString("status")
                    );

                    item.setItemType(
                            rs.getString("item_type")
                    );

                    item.setTrackInventory(
                            rs.getBoolean("track_inventory")
                    );

                    orderItem.setItem(item);

                    orderItem.setSalesOrder(salesOrder);

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


    // =========================================================
    // UPDATE AN EXISTING SALES ORDER ITEM
    // Preserves the existing sales_order_items.id.
    // =========================================================

    public void update(SalesOrderItem salesOrderItem) {

        String sql = """
                UPDATE sales_order_items
                SET
                    quantity = ?,
                    sellingPrice = ?,
                    item_id = ?
                WHERE id = ?
                  AND sales_order_id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
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
                    salesOrderItem.getId()
            );

            statement.setInt(
                    5,
                    salesOrderItem.getSalesOrder().getId()
            );

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException(
                        "Sales order item was not found for update"
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error updating sales order item",
                    e
            );
        }
    }


    // =========================================================
    // CHECK WHETHER A SALES ORDER ITEM IS REFERENCED BY A PACKAGE
    // =========================================================

    public boolean isReferencedByPackage(int salesOrderItemId) {

        String sql = """
                SELECT 1
                FROM package_items
                WHERE sales_order_item_id = ?
                LIMIT 1
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    salesOrderItemId
            );

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error checking package references for sales order item",
                    e
            );
        }
    }


    // =========================================================
    // DELETE ONE SALES ORDER ITEM
    // Only called for lines that have been removed from the order.
    // =========================================================

    public void deleteByIdAndSalesOrderId(
            int salesOrderItemId,
            int salesOrderId
    ) {

        // Protect package references before attempting deletion.
        if (isReferencedByPackage(salesOrderItemId)) {
            throw new RuntimeException(
                    "Cannot remove sales order item "
                            + salesOrderItemId
                            + " because it is referenced by a package"
            );
        }

        String sql = """
                DELETE FROM sales_order_items
                WHERE id = ?
                  AND sales_order_id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    salesOrderItemId
            );

            statement.setInt(
                    2,
                    salesOrderId
            );

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException(
                        "Sales order item was not found for deletion"
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error deleting sales order item",
                    e
            );
        }
    }


    // =========================================================
    // LEGACY DELETE METHOD
    // Kept for compatibility with any existing callers.
    // The sales order edit flow must not use this method.
    // =========================================================

    public void deleteBySalesOrderId(int salesOrderId) {

        String sql = """
                DELETE FROM sales_order_items
                WHERE sales_order_id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    salesOrderId
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error deleting sales order items",
                    e
            );
        }
    }
}