
package com.erpflow.dao;

import com.erpflow.model.InventoryNotification;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryNotificationDAO {

    // Save a newly generated notification
    public void save(InventoryNotification notification) {

        String sql = """
                INSERT INTO inventory_notifications
                (item_id, alert_type, message)
                VALUES (?, ?, ?)
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {
            statement.setInt(1, notification.getItemId());
            statement.setString(2, notification.getAlertType());
            statement.setString(3, notification.getMessage());

            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    notification.setId(rs.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error saving inventory notification", e
            );
        }
    }

    // Retrieve all notifications, newest first
    public List<InventoryNotification> findAll() {

        List<InventoryNotification> notifications = new ArrayList<>();

        String sql = """
                SELECT id, item_id, alert_type, message,
                       is_read, created_at
                FROM inventory_notifications
                ORDER BY created_at DESC
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            while (rs.next()) {
                notifications.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching inventory notifications", e
            );
        }

        return notifications;
    }

    // Retrieve unread notifications
    public List<InventoryNotification> findUnread() {

        List<InventoryNotification> notifications = new ArrayList<>();

        String sql = """
                SELECT id, item_id, alert_type, message,
                       is_read, created_at
                FROM inventory_notifications
                WHERE is_read = FALSE
                ORDER BY created_at DESC
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            while (rs.next()) {
                notifications.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching unread notifications", e
            );
        }

        return notifications;
    }

    // Mark one notification as read
    public boolean markAsRead(long id) {

        String sql = """
                UPDATE inventory_notifications
                SET is_read = TRUE
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error marking notification as read", e
            );
        }
    }

    // Convert a database row into the Java model
    private InventoryNotification mapRow(ResultSet rs)
            throws SQLException {

        InventoryNotification notification =
                new InventoryNotification();

        notification.setId(rs.getLong("id"));
        notification.setItemId(rs.getInt("item_id"));
        notification.setAlertType(rs.getString("alert_type"));
        notification.setMessage(rs.getString("message"));
        notification.setRead(rs.getBoolean("is_read"));

        Timestamp timestamp = rs.getTimestamp("created_at");

        if (timestamp != null) {
            notification.setCreatedAt(timestamp.toLocalDateTime());
        }

        return notification;
    }
}