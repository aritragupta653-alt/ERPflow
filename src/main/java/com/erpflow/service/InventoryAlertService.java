
package com.erpflow.service;

import com.erpflow.dao.InventoryNotificationDAO;
import com.erpflow.dao.InventoryAlertSettingsDAO;
import com.erpflow.model.InventoryAlertSettings;
import com.erpflow.model.InventoryNotification;
import com.erpflow.model.Item;

public class InventoryAlertService {

    private final InventoryAlertSettingsDAO settingsDAO =
            new InventoryAlertSettingsDAO();

    private final InventoryNotificationDAO notificationDAO =
            new InventoryNotificationDAO();

    public void checkAlerts(Item item) {

        if (item == null || !item.isTrackInventory()) {
            return;
        }

        InventoryAlertSettings settings = settingsDAO.findSettings();

        if (settings == null) {
            return;
        }

        int inHand = item.getInHandQuantity();
        int committed = item.getCommittedQuantity();
        int available = inHand - committed;

        // Out-of-stock alert
        if (settings.isOutOfStockEnabled() && available <= 0) {
            createNotification(
                    item,
                    "OUT_OF_STOCK",
                    item.getName() + " is out of stock."
            );
        }

        // Low-stock alert
        else if (settings.isLowStockEnabled()
                && available <= item.getReorderLevel()) {
            createNotification(
                    item,
                    "LOW_STOCK",
                    item.getName() + " is running low on stock. Available: "
                            + available
            );
        }

        // Replenishment reminder
        if (settings.isReplenishmentEnabled()
                && available <= item.getReorderLevel()) {
            createNotification(
                    item,
                    "REPLENISHMENT",
                    "Replenishment required for " + item.getName()
                            + ". Available: " + available
            );
        }

        // Overstock alert
        Integer maxStock = item.getMaxStockQuantity();

        if (settings.isOverstockEnabled()
                && maxStock != null
                && inHand > maxStock) {
            createNotification(
                    item,
                    "OVERSTOCK",
                    item.getName() + " exceeds maximum stock. In hand: "
                            + inHand + ", maximum: " + maxStock
            );
        }
    }

    private void createNotification(
            Item item,
            String alertType,
            String message
    ) {
        InventoryNotification notification =
                new InventoryNotification();

        notification.setItemId(item.getId());
        notification.setAlertType(alertType);
        notification.setMessage(message);

        notificationDAO.save(notification);
    }
}