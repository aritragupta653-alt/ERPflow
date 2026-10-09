
package com.erpflow.dao;

import com.erpflow.model.InventoryAlertSettings;
import com.erpflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class InventoryAlertSettingsDAO {

    // Retrieve the global inventory alert settings
    public InventoryAlertSettings findSettings() {

        String sql = """
                SELECT id,
                       low_stock_enabled,
                       out_of_stock_enabled,
                       replenishment_enabled,
                       overstock_enabled,
                       frequency,
                       notification_channel,
                       updated_at
                FROM inventory_alert_settings
                WHERE id = 1
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            if (rs.next()) {
                InventoryAlertSettings settings =
                        new InventoryAlertSettings();

                settings.setId(rs.getInt("id"));
                settings.setLowStockEnabled(
                        rs.getBoolean("low_stock_enabled"));
                settings.setOutOfStockEnabled(
                        rs.getBoolean("out_of_stock_enabled"));
                settings.setReplenishmentEnabled(
                        rs.getBoolean("replenishment_enabled"));
                settings.setOverstockEnabled(
                        rs.getBoolean("overstock_enabled"));
                settings.setFrequency(rs.getString("frequency"));
                settings.setNotificationChannel(
                        rs.getString("notification_channel"));

                Timestamp timestamp = rs.getTimestamp("updated_at");

                if (timestamp != null) {
                    settings.setUpdatedAt(
                            timestamp.toLocalDateTime());
                }

                return settings;
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching inventory alert settings", e);
        }
    }

    // Update the global alert configuration
    public boolean updateSettings(InventoryAlertSettings settings) {

        String sql = """
                UPDATE inventory_alert_settings
                SET low_stock_enabled = ?,
                    out_of_stock_enabled = ?,
                    replenishment_enabled = ?,
                    overstock_enabled = ?
                WHERE id = 1
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setBoolean(1, settings.isLowStockEnabled());
            statement.setBoolean(2, settings.isOutOfStockEnabled());
            statement.setBoolean(3, settings.isReplenishmentEnabled());
            statement.setBoolean(4, settings.isOverstockEnabled());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error updating inventory alert settings", e);
        }
    }
}