
package com.erpflow.service;

import com.erpflow.dao.InventoryAlertSettingsDAO;
import com.erpflow.model.InventoryAlertSettings;

public class InventoryAlertSettingsService {

    private final InventoryAlertSettingsDAO settingsDAO =
            new InventoryAlertSettingsDAO();

    public InventoryAlertSettings getSettings() {
        InventoryAlertSettings settings =
                settingsDAO.findSettings();

        if (settings == null) {
            throw new RuntimeException(
                    "Inventory alert settings not found. " +
                    "Ensure the settings table contains row id = 1."
            );
        }

        return settings;
    }

    public InventoryAlertSettings updateSettings(
        InventoryAlertSettings settings) {

    if (settings == null) {
        throw new IllegalArgumentException(
                "Inventory alert settings cannot be null."
        );
    }

    if (settings.getFrequency() == null
            || settings.getFrequency().isBlank()) {

        throw new IllegalArgumentException(
                "Frequency is required."
        );
    }

    if (!"IMMEDIATE".equalsIgnoreCase(
            settings.getFrequency().trim())) {

        throw new IllegalArgumentException(
                "Invalid frequency. Only IMMEDIATE is supported."
        );
    }

    if (settings.getNotificationChannel() == null
            || settings.getNotificationChannel().isBlank()) {

        throw new IllegalArgumentException(
                "Notification channel is required."
        );
    }

    if (!"IN_APP".equalsIgnoreCase(
            settings.getNotificationChannel().trim())) {

        throw new IllegalArgumentException(
                "Invalid notification channel. Only IN_APP is supported."
        );
    }

    settings.setFrequency("IMMEDIATE");
    settings.setNotificationChannel("IN_APP");

    boolean updated =
            settingsDAO.updateSettings(settings);

    if (!updated) {
        throw new RuntimeException(
                "Failed to update inventory alert settings."
        );
    }

    return getSettings();
}
}