package com.erpflow.dao;

import com.erpflow.model.Item;
import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.model.enums.ItemStatus;
import com.erpflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackageItemDAO {

    public void save(PackageItem packageItem) {
        String sql = """
                INSERT INTO package_items
                    (quantity, item_id, package_id, sales_order_item_id)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, packageItem.getQuantity());
            statement.setInt(2, packageItem.getItem().getId());
            statement.setInt(3, packageItem.getPackageEntity().getId());

            if (packageItem.getSalesOrderItemId() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, packageItem.getSalesOrderItemId());
            }

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    packageItem.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save package item", e);
        }
    }

    public void saveBatch(List<PackageItem> packageItems) {
        if (packageItems == null || packageItems.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO package_items
                    (quantity, item_id, package_id, sales_order_item_id)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            for (PackageItem item : packageItems) {
                statement.setInt(1, item.getQuantity());
                statement.setInt(2, item.getItem().getId());
                statement.setInt(3, item.getPackageEntity().getId());

                if (item.getSalesOrderItemId() == null) {
                    statement.setNull(4, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(4, item.getSalesOrderItemId());
                }
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save package items", e);
        }
    }

    public List<PackageItem> findByPackageId(int packageId) {
        return findByPackageIds(List.of(packageId));
    }

    /** Bulk lookup avoids N+1 package-item queries during shipment operations. */
    public List<PackageItem> findByPackageIds(List<Integer> packageIds) {
        if (packageIds == null || packageIds.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders = String.join(",", Collections.nCopies(packageIds.size(), "?"));
        String sql = """
                SELECT
                    pi.id,
                    pi.quantity,
                    pi.package_id,
                    pi.sales_order_item_id,
                    i.item_id,
                    i.name,
                    i.description,
                    i.sku,
                    i.purchase_price,
                    i.selling_price,
                    i.reorder_level, 
                    i.status,
                    i.item_type,
                    i.track_inventory,
                    i.length,
                    i.width,
                    i.height,
                    i.weight
                FROM package_items pi
                JOIN items i ON pi.item_id = i.item_id
                WHERE pi.package_id IN (%s)
                ORDER BY pi.package_id, pi.id
                """.formatted(placeholders);

        List<PackageItem> result = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < packageIds.size(); i++) {
                statement.setInt(i + 1, packageIds.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    PackageItem packageItem = new PackageItem();
                    packageItem.setId(rs.getInt("id"));
                    packageItem.setQuantity(rs.getInt("quantity"));

                    int lineId = rs.getInt("sales_order_item_id");
                    packageItem.setSalesOrderItemId(rs.wasNull() ? null : lineId);

                    Package pkg = new Package();
                    pkg.setId(rs.getInt("package_id"));
                    packageItem.setPackageEntity(pkg);

                    Item item = new Item();
                    item.setId(rs.getInt("item_id"));
                    item.setName(rs.getString("name"));
                    item.setDescription(rs.getString("description"));
                    item.setSku(rs.getString("sku"));
                    item.setPurchasePrice(rs.getBigDecimal("purchase_price"));
                    item.setSellingPrice(rs.getBigDecimal("selling_price"));
                    item.setReorderLevel(rs.getInt("reorder_level"));

                    String status = rs.getString("status");
                    item.setStatus(status == null ? null : ItemStatus.valueOf(status));
                    item.setItemType(rs.getString("item_type"));
                    item.setTrackInventory(rs.getBoolean("track_inventory"));
                    item.setLength(rs.getDouble("length"));
                    item.setWidth(rs.getDouble("width"));
                    item.setHeight(rs.getDouble("height"));
                    item.setWeight(rs.getDouble("weight"));

                    packageItem.setItem(item);
                    result.add(packageItem);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch package items", e);
        }

        return result;
    }

    public void deleteByPackageId(int packageId) {
        if (packageId <= 0) {
            throw new IllegalArgumentException("Invalid package ID");
        }

        String sql = "DELETE FROM package_items WHERE package_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, packageId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to delete package items for package " + packageId, e);
        }
    }
}
