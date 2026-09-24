
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

        settingsDAO.updateSettings(settings);

        return getSettings();
    }
}