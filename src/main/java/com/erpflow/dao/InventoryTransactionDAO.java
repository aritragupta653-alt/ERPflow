package com.erpflow.dao;

import com.erpflow.model.InventoryTransaction;
import com.erpflow.model.Item;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryTransactionDAO {

    // =========================================================
    // SAVE TRANSACTION
    // =========================================================

    public void save(InventoryTransaction inventoryTransaction) {

        String sql = """
                INSERT INTO inventory_transactions
                (item_id, type, quantity, transactionDate)
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
                    inventoryTransaction.getItem().getId()
            );

            statement.setString(
                    2,
                    inventoryTransaction.getType()
            );

            statement.setInt(
                    3,
                    inventoryTransaction.getQuantity()
            );

            statement.setTimestamp(
                    4,
                    Timestamp.valueOf(
                            inventoryTransaction.getTransactionDate()
                    )
            );

            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {

                if (rs.next()) {
                    inventoryTransaction.setId(
                            rs.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error saving inventory transaction",
                    e
            );
        }
    }


    // =========================================================
    // GET ALL TRANSACTIONS
    // =========================================================

    public List<InventoryTransaction> findAll() {

        List<InventoryTransaction> transactions =
                new ArrayList<>();

        String sql = """
                SELECT
                    t.id,
                    t.item_id,
                    t.type,
                    t.quantity,
                    t.transactionDate,

                    i.name,
                    i.sku,
                    i.description,
                    i.purchase_price,
                    i.selling_price,
                    i.reorder_level,
                    i.status

                FROM inventory_transactions t

                JOIN items i
                    ON t.item_id = i.item_id

                ORDER BY t.transactionDate DESC
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {

            while (rs.next()) {

                Item item = new Item();

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
                        rs.getBigDecimal("purchase_price")
                );

                item.setSellingPrice(
                        rs.getBigDecimal("selling_price")
                );

                item.setReorderLevel(
                        rs.getInt("reorder_level")
                );

                item.setStatus(
                        rs.getString("status")
                );


                InventoryTransaction transaction =
                        new InventoryTransaction();

                transaction.setId(
                        rs.getInt("id")
                );

                transaction.setItem(item);

                transaction.setType(
                        rs.getString("type")
                );

                transaction.setQuantity(
                        rs.getInt("quantity")
                );

                Timestamp timestamp =
                        rs.getTimestamp("transactionDate");

                if (timestamp != null) {
                    transaction.setTransactionDate(
                            timestamp.toLocalDateTime()
                    );
                }

                transactions.add(transaction);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching inventory transactions",
                    e
            );
        }

        return transactions;
    }
}