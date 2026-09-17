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

    // SAVE PACKAGE ITEM
    public void save(PackageItem packageItem) {

        String sql = """
                INSERT INTO package_items
                (quantity, item_id, package_id)
                VALUES (?, ?, ?)
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
                    packageItem.getQuantity()
            );

            statement.setLong(
                    2,
                    packageItem.getItem().getId()
            );

            statement.setInt(
                    3,
                    packageItem.getPackageEntity().getId()
            );

            statement.executeUpdate();

            try (ResultSet rs =
                         statement.getGeneratedKeys()) {

                if (rs.next()) {

                    packageItem.setId(
                            rs.getInt(1)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error saving package item",
                    e
            );
        }
    }


    // GET ITEMS INSIDE PACKAGE
    public List<PackageItem> findByPackage(
            Package packageEntity) {

        String sql = """
                SELECT
                    pi.id,
                    pi.quantity,
                    pi.item_id,
                    pi.package_id,

                    i.name,
                    i.description,
                    i.purchase_price,
                    i.reorder_level,
                    i.selling_price,
                    i.sku,
                    i.status

                FROM package_items pi

                JOIN items i
                    ON pi.item_id = i.item_id

                WHERE pi.package_id = ?
                """;

        List<PackageItem> packageItems =
                new ArrayList<>();

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

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


                    // Create Item
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
                            rs.getInt("reorder_level")
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
                    "Error fetching package items",
                    e
            );
        }

        return packageItems;
    }
}