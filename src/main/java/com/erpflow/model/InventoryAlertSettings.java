
package com.erpflow.model;

import java.time.LocalDateTime;

public class InventoryAlertSettings {

    private int id;
    private boolean lowStockEnabled;
    private boolean outOfStockEnabled;
    private boolean replenishmentEnabled;
    private boolean overstockEnabled;
    private String frequency;
    private String notificationChannel;
    private LocalDateTime updatedAt;

    public InventoryAlertSettings() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isLowStockEnabled() {
        return lowStockEnabled;
    }

    public void setLowStockEnabled(boolean lowStockEnabled) {
        this.lowStockEnabled = lowStockEnabled;
    }

    public boolean isOutOfStockEnabled() {
        return outOfStockEnabled;
    }

    public void setOutOfStockEnabled(boolean outOfStockEnabled) {
        this.outOfStockEnabled = outOfStockEnabled;
    }

    public boolean isReplenishmentEnabled() {
        return replenishmentEnabled;
    }

    public void setReplenishmentEnabled(boolean replenishmentEnabled) {
        this.replenishmentEnabled = replenishmentEnabled;
    }

    public boolean isOverstockEnabled() {
        return overstockEnabled;
    }

    public void setOverstockEnabled(boolean overstockEnabled) {
        this.overstockEnabled = overstockEnabled;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getNotificationChannel() {
        return notificationChannel;
    }

    public void setNotificationChannel(String notificationChannel) {
        this.notificationChannel = notificationChannel;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}