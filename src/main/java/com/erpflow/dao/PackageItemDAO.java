package com.erpflow.dao;

import com.erpflow.model.Item;
import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PackageItemDAO {

    // =========================
    // SAVE
    // =========================

    public void save(PackageItem packageItem) {

        String sql = """
                INSERT INTO package_items
                (
                    quantity,
                    item_id,
                    package_id
                )
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
                    packageItem.getQuantity()
            );

            statement.setInt(
                    2,
                    packageItem.getItem().getId()
            );

            statement.setInt(
                    3,
                    packageItem.getPackageEntity().getId()
            );

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (keys.next()) {

                    packageItem.setId(
                            keys.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save package item",
                    e
            );
        }
    }


    // =========================
    // FIND ITEMS BY PACKAGE
    // =========================

    public List<PackageItem> findByPackage(
            Package packageEntity) {

        String sql = """
                SELECT
                    pi.id,
                    pi.quantity,
                    i.item_id,
                    i.name,
                    i.description,
                    i.sku,
                    i.purchase_price,
                    i.selling_price,
                    i.reorder_level,
                    i.status
                FROM package_items pi
                JOIN items i
                    ON pi.item_id = i.item_id
                WHERE pi.package_id = ?
                ORDER BY pi.id
                """;

        List<PackageItem> packageItems =
                new ArrayList<>();

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    packageEntity.getId()
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                while (rs.next()) {

                    PackageItem packageItem =
                            new PackageItem();

                    packageItem.setId(
                            rs.getInt("id")
                    );

                    packageItem.setQuantity(
                            rs.getInt("quantity")
                    );


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

                    item.setSku(
                            rs.getString("sku")
                    );

                    item.setPurchasePrice(
                            rs.getBigDecimal(
                                    "purchase_price"
                            )
                    );

                    item.setSellingPrice(
                            rs.getBigDecimal(
                                    "selling_price"
                            )
                    );

                    item.setReorderLevel(
                            rs.getInt("reorder_level")
                    );

                    item.setStatus(
                            rs.getString("status")
                    );


                    packageItem.setItem(item);

                    packageItem.setPackageEntity(
                            packageEntity
                    );

                    packageItems.add(
                            packageItem
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch package items",
                    e
            );
        }

        return packageItems;
    }
}