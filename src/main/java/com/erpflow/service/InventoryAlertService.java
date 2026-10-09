package com.erpflow.service;

import com.erpflow.dao.InventoryAlertSettingsDAO;
import com.erpflow.dao.InventoryNotificationDAO;
import com.erpflow.model.InventoryAlertSettings;
import com.erpflow.model.InventoryNotification;
import com.erpflow.model.Item;

public class InventoryAlertService {

    private final InventoryAlertSettingsDAO settingsDAO =
            new InventoryAlertSettingsDAO();

    private final InventoryNotificationDAO notificationDAO =
            new InventoryNotificationDAO();


    // =====================================================
    // MAIN ALERT CHECK
    // =====================================================

    public void checkAlerts(Item item) {

        if (item == null || !item.isTrackInventory()) {
            return;
        }

        InventoryAlertSettings settings =
                settingsDAO.findSettings();

        if (settings == null) {
            return;
        }


        int inHand =
                item.getInHandQuantity();

        int committed =
                item.getCommittedQuantity();

        int available =
                inHand - committed;


        // =================================================
        // OUT OF STOCK
        // =================================================

        if (settings.isOutOfStockEnabled()
                && available <= 0) {

            createNotificationIfNotExists(
                    item,
                    "OUT_OF_STOCK",
                    item.getName()
                            + " is out of stock. "
                            + "Available: "
                            + available
            );
        }


        // =================================================
        // LOW STOCK
        // =================================================

        else if (settings.isLowStockEnabled()
                && available <= item.getReorderLevel()) {

            createNotificationIfNotExists(
                    item,
                    "LOW_STOCK",
                    item.getName()
                            + " is running low on stock. "
                            + "Available: "
                            + available
                            + ", Reorder level: "
                            + item.getReorderLevel()
            );
        }
        // =================================================
// REPLENISHMENT
// =================================================

if (settings.isReplenishmentEnabled()
        && available <= item.getReorderLevel()) {

    createNotificationIfNotExists(
            item,
            "REPLENISHMENT",
            "Replenishment required for "
                    + item.getName()
                    + ". Available: "
                    + available
                    + ", Reorder level: "
                    + item.getReorderLevel()
    );
}


        // =================================================
        // OVERSTOCK
        // =================================================

        Integer maxStock =
                item.getMaxStockQuantity();

        if (settings.isOverstockEnabled()
                && maxStock != null
                && inHand > maxStock) {

            createNotificationIfNotExists(
                    item,
                    "OVERSTOCK",
                    item.getName()
                            + " exceeds maximum stock. "
                            + "In hand: "
                            + inHand
                            + ", Maximum: "
                            + maxStock
            );
        }
    }


    // =====================================================
    // CREATE NOTIFICATION ONLY IF NOT ALREADY ACTIVE
    // =====================================================

    private void createNotificationIfNotExists(
            Item item,
            String alertType,
            String message) {

        boolean exists =
                notificationDAO.existsUnreadForItemAndType(
                        item.getId(),
                        alertType
                );

        if (exists) {
            return;
        }


        InventoryNotification notification =
                new InventoryNotification();

        notification.setItemId(
                item.getId()
        );

        notification.setAlertType(
                alertType
        );

        notification.setMessage(
                message
        );

        notificationDAO.save(
                notification
        );
    }
}