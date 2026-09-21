package com.erpflow.dao;

import com.erpflow.model.Item;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public void save(Item item) {

        String sql = """
                INSERT INTO items
                (name, sku, description, purchase_price,
                 selling_price, reorder_level, status, item_type , track_inventory)
                VALUES (?, ?, ?, ?, ?, ?, ? ,? , ?)
                """;

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(1, item.getName());
            statement.setString(2, item.getSku());
            statement.setString(3, item.getDescription());
            statement.setBigDecimal(4, item.getPurchasePrice());
            statement.setBigDecimal(5, item.getSellingPrice());
            statement.setInt(6, item.getReorderLevel());
            statement.setString(7, item.getStatus());
            statement.setString(8, item.getItemType());
            statement.setBoolean(9, item.isTrackInventory());

            statement.executeUpdate();

            try (ResultSet resultSet =
                         statement.getGeneratedKeys()) {

                if (resultSet.next()) {
                    item.setId(resultSet.getInt(1));
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error saving item",
                    e
            );
        }
    }


    public List<Item> findAll() {

    String sql = """
            SELECT item_id, name, sku, description,
                   purchase_price, selling_price,
                   reorder_level, status,
                   item_type, track_inventory
            FROM items
            WHERE status = 'ACTIVE'
            ORDER BY item_id
            """;

    List<Item> items = new ArrayList<>();

    try (Connection connection =
                 DBConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql);
         ResultSet resultSet =
                 statement.executeQuery()) {

        while (resultSet.next()) {
            items.add(mapRowToItem(resultSet));
        }

    } catch (SQLException e) {
        throw new RuntimeException(
                "Error fetching items",
                e
        );
    }

    return items;
}

    public Item findById(int id) {

    String sql = """
            SELECT item_id, name, sku, description,
                   purchase_price, selling_price,
                   reorder_level, status,
                   item_type, track_inventory
            FROM items
            WHERE item_id = ?
            """;

    try (Connection connection =
                 DBConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, id);

        try (ResultSet resultSet =
                     statement.executeQuery()) {

            if (resultSet.next()) {
                return mapRowToItem(resultSet);
            }
        }

    } catch (SQLException e) {
        throw new RuntimeException(
                "Error fetching item",
                e
        );
    }

    return null;
}


    public void update(Item item) {

    String sql = """
            UPDATE items
            SET name = ?,
                sku = ?,
                description = ?,
                purchase_price = ?,
                selling_price = ?,
                reorder_level = ?,
                item_type = ?,
                track_inventory = ?
            WHERE item_id = ?
            """;

    try (Connection connection =
                 DBConnection.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setString(1, item.getName());
        statement.setString(2, item.getSku());
        statement.setString(3, item.getDescription());
        statement.setBigDecimal(4, item.getPurchasePrice());
        statement.setBigDecimal(5, item.getSellingPrice());
        statement.setInt(6, item.getReorderLevel());
        statement.setString(7, item.getItemType());
        statement.setBoolean(8, item.isTrackInventory());
        statement.setInt(9, item.getId());

        statement.executeUpdate();

    } catch (SQLException e) {
        throw new RuntimeException(
                "Error updating item",
                e
        );
    }
}


    public void delete(int id) {

        String sql = """
                UPDATE items
                SET status = 'INACTIVE'
                WHERE item_id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error deleting item",
                    e
            );
        }
    }


    private Item mapRowToItem(ResultSet resultSet)
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
                resultSet.getBigDecimal("purchase_price")
        );

        item.setSellingPrice(
                resultSet.getBigDecimal("selling_price")
        );

        item.setReorderLevel(
                resultSet.getInt("reorder_level")
        );

        item.setStatus(
                resultSet.getString("status")
        );
        item.setItemType(
        resultSet.getString("item_type")
);

item.setTrackInventory(
        resultSet.getBoolean("track_inventory")
);

        return item;
    }
}