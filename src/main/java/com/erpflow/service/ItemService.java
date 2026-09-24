
package com.erpflow.service;

import com.erpflow.dao.ItemDAO;
import com.erpflow.model.Item;
import com.erpflow.model.enums.ItemStatus;

import java.util.List;

public class ItemService {

    private final ItemDAO itemDAO = new ItemDAO();

    // =========================================================
    // VALIDATE ITEM TYPE AND INVENTORY TRACKING
    // =========================================================

    private void validateItemType(Item item) {

        if (item.getItemType() == null ||
                item.getItemType().isBlank()) {

            throw new RuntimeException(
                    "Item type is required"
            );
        }

        String itemType = item.getItemType().trim();

        if (!itemType.equalsIgnoreCase("GOODS") &&
                !itemType.equalsIgnoreCase("SERVICE")) {

            throw new RuntimeException(
                    "Item type must be GOODS or SERVICE"
            );
        }

        // Store a consistent value in the database.
        item.setItemType(itemType.toUpperCase());

        // Services do not have physical stock.
        if ("SERVICE".equalsIgnoreCase(item.getItemType())) {
            item.setTrackInventory(false);
        }
    }

    // =========================================================
    // VALIDATE COMMON ITEM FIELDS
    // =========================================================

    private void validateItem(Item item) {

        if (item == null) {
            throw new RuntimeException(
                    "Item cannot be null"
            );
        }

        if (item.getName() == null ||
                item.getName().isBlank()) {

            throw new RuntimeException(
                    "Item name is required"
            );
        }

        if (item.getSku() == null ||
                item.getSku().isBlank()) {

            throw new RuntimeException(
                    "SKU is required"
            );
        }

        if (item.getPurchasePrice() == null ||
                item.getSellingPrice() == null) {

            throw new RuntimeException(
                    "Prices are required"
            );
        }

        if (item.getPurchasePrice().signum() < 0 ||
                item.getSellingPrice().signum() < 0) {

            throw new RuntimeException(
                    "Prices cannot be negative"
            );
        }

        if (item.getReorderLevel() < 0) {

            throw new RuntimeException(
                    "Reorder level cannot be negative"
            );
        }

        validateItemType(item);
    }

    // =========================================================
    // CREATE ITEM
    // =========================================================

    public void createItem(Item item) {

        validateItem(item);

        if (item.getStatus() == null) {
            item.setStatus(ItemStatus.ACTIVE);
        }

        itemDAO.save(item);
    }

    // =========================================================
    // GET ALL ITEMS
    // =========================================================

   public List<Item> getAllItems() {
    return itemDAO.findAll("ALL");
}

public List<Item> getItemsByStatus(String status) {

    if (status == null || status.isBlank()) {
        status = "ALL";
    }

    status = status.trim().toUpperCase();

    if (!status.equals("ALL")) {
        try {
            ItemStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Invalid item status. Use ACTIVE, INACTIVE, or ALL"
            );
        }
    }

    return itemDAO.findAll(status);
}

    // =========================================================
    // GET ITEM BY ID
    // =========================================================

    public Item getItemById(int id) {

        return itemDAO.findById(id);
    }

    // =========================================================
    // UPDATE ITEM
    // =========================================================

    public void updateItem(Item item) {

        if (item == null) {
            throw new RuntimeException(
                    "Item cannot be null"
            );
        }

        Item existing = itemDAO.findById(item.getId());

        if (existing == null) {
            throw new RuntimeException(
                    "Item not found"
            );
        }

        validateItem(item);

        itemDAO.update(item);
    }

    // =========================================================
    // DELETE ITEM
    // Soft delete: mark item as INACTIVE
    // =========================================================

    public void deleteItem(int id) {

        Item existing = itemDAO.findById(id);

        if (existing == null) {
            throw new RuntimeException(
                    "Item not found"
            );
        }

        itemDAO.delete(id);
    }
}