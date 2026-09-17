package com.erpflow.dao;

import com.erpflow.model.Inventory;
import com.erpflow.model.Item;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public void save(Inventory inventory) {

        String sql = """
                INSERT INTO inventory
                (item_id, quantity, committedquantity)
                VALUES (?, ?, ?)
                """;

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setInt(
                    1,
                    inventory.getItem().getId()
            );

            statement.setInt(
                    2,
                    inventory.getQuantity()
            );

            statement.setInt(
                    3,
                    inventory.getCommittedQuantity()
            );

            statement.executeUpdate();

            try (ResultSet resultSet =
                         statement.getGeneratedKeys()) {

                if (resultSet.next()) {

                    inventory.setId(
                            resultSet.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error saving inventory",
                    e
            );
        }
    }


    public Inventory findByItemId(int itemId) {

        String sql = """
                SELECT
                    i.inventory_id,
                    i.item_id,
                    i.quantity,
                    i.committedquantity,
                    it.name,
                    it.sku,
                    it.description,
                    it.purchase_price,
                    it.selling_price,
                    it.reorder_level,
                    it.status
                FROM inventory i
                JOIN items it
                    ON i.item_id = it.item_id
                WHERE i.item_id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, itemId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapRowToInventory(
                            resultSet
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching inventory",
                    e
            );
        }

        return null;
    }


    public List<Inventory> findAll() {

        String sql = """
                SELECT
                    i.inventory_id,
                    i.item_id,
                    i.quantity,
                    i.committedquantity,
                    it.name,
                    it.sku,
                    it.description,
                    it.purchase_price,
                    it.selling_price,
                    it.reorder_level,
                    it.status
                FROM inventory i
                JOIN items it
                    ON i.item_id = it.item_id
                ORDER BY i.inventory_id
                """;

        List<Inventory> inventories =
                new ArrayList<>();

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                inventories.add(
                        mapRowToInventory(resultSet)
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching inventory",
                    e
            );
        }

        return inventories;
    }


    public void update(Inventory inventory) {

        String sql = """
                UPDATE inventory
                SET quantity = ?,
                    committedquantity = ?
                WHERE inventory_id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    inventory.getQuantity()
            );

            statement.setInt(
                    2,
                    inventory.getCommittedQuantity()
            );

            statement.setInt(
                    3,
                    inventory.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error updating inventory",
                    e
            );
        }
    }


    public void deleteByItemId(int itemId) {

        String sql = """
                DELETE FROM inventory
                WHERE item_id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, itemId);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error deleting inventory",
                    e
            );
        }
    }


    private Inventory mapRowToInventory(
            ResultSet resultSet)
            throws SQLException {

        Item item = new Item();

        item.setId(
                resultSet.getInt("item_id")
        );

        item.setName(
                resultSet.getString("name")
        );

        item.setSku(
                resultSet.getString("sku")
        );

        item.setDescription(
                resultSet.getString("description")
        );

        item.setPurchasePrice(
                resultSet.getBigDecimal(
                        "purchase_price"
                )
        );

        item.setSellingPrice(
                resultSet.getBigDecimal(
                        "selling_price"
                )
        );

        item.setReorderLevel(
                resultSet.getInt(
                        "reorder_level"
                )
        );

        item.setStatus(
                resultSet.getString("status")
        );

        Inventory inventory =
                new Inventory();

        inventory.setId(
                resultSet.getInt("inventory_id")
        );

        inventory.setItem(item);

        inventory.setQuantity(
                resultSet.getInt("quantity")
        );

        inventory.setCommittedQuantity(
                resultSet.getInt(
                        "committedquantity"
                )
        );

        return inventory;
    }
}