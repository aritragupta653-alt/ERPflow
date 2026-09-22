
package com.erpflow.dao;

import com.erpflow.model.Item;
import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PackageItemDAO {

    // =====================================================
    // SAVE PACKAGE ITEM
    // =====================================================

    public void save(PackageItem packageItem) {

        String sql = """
                INSERT INTO package_items
                (
                    quantity,
                    item_id,
                    package_id,
                    sales_order_item_id
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

            statement.setInt(1, packageItem.getQuantity());
            statement.setInt(2, packageItem.getItem().getId());
            statement.setInt(
                    3,
                    packageItem.getPackageEntity().getId()
            );

            if (packageItem.getSalesOrderItemId() != null) {
                statement.setInt(
                        4,
                        packageItem.getSalesOrderItemId()
                );
            } else {
                statement.setNull(4, java.sql.Types.INTEGER);
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

    // =====================================================
    // FIND ITEMS BY PACKAGE
    // =====================================================

    public List<PackageItem> findByPackage(Package packageEntity) {

        String sql = """
                SELECT
                    pi.id,
                    pi.quantity,
                    pi.sales_order_item_id,
                    i.item_id,
                    i.name,
                    i.description,
                    i.sku,
                    i.purchase_price,
                    i.selling_price,
                    i.reorder_level,
                    i.status
                FROM package_items pi
                JOIN items i ON pi.item_id = i.item_id
                WHERE pi.package_id = ?
                ORDER BY pi.id
                """;

        List<PackageItem> packageItems = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, packageEntity.getId());

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {

                    PackageItem packageItem = new PackageItem();

                    packageItem.setId(rs.getInt("id"));
                    packageItem.setQuantity(rs.getInt("quantity"));

                    int salesOrderItemId =
                            rs.getInt("sales_order_item_id");

                    if (rs.wasNull()) {
                        packageItem.setSalesOrderItemId(null);
                    } else {
                        packageItem.setSalesOrderItemId(salesOrderItemId);
                    }

                    Item item = new Item();

                    item.setId(rs.getInt("item_id"));
                    item.setName(rs.getString("name"));
                    item.setDescription(rs.getString("description"));
                    item.setSku(rs.getString("sku"));
                    item.setPurchasePrice(rs.getBigDecimal("purchase_price"));
                    item.setSellingPrice(rs.getBigDecimal("selling_price"));
                    item.setReorderLevel(rs.getInt("reorder_level"));
                    item.setStatus(rs.getString("status"));

                    packageItem.setItem(item);
                    packageItem.setPackageEntity(packageEntity);

                    packageItems.add(packageItem);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch package items", e);
        }

        return packageItems;
    }

    // =====================================================
    // DELETE ALL ITEMS FOR A PACKAGE
    // Used by the package edit operation after validation.
    // =====================================================

    public void deleteByPackageId(int packageId) {

        if (packageId <= 0) {
            throw new IllegalArgumentException("Invalid package ID");
        }

        String sql = "DELETE FROM package_items WHERE package_id = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, packageId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to delete package items for package " + packageId,
                    e
            );
        }
    }
}