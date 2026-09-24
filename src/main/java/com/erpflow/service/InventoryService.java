package com.erpflow.service;

import com.erpflow.dao.InventoryDAO;
import com.erpflow.model.Inventory;
import com.erpflow.model.InventoryTransaction;
import com.erpflow.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public class InventoryService {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    private final InventoryTransactionService inventoryTransactionService =
            new InventoryTransactionService();


    public void addInventory(Inventory inventory) {
        inventoryDAO.save(inventory);
    }


    public Inventory getInventoryByItemId(int itemId) {
        return inventoryDAO.findByItemId(itemId);
    }


    public List<Inventory> getAllInventory() {
        return inventoryDAO.findAll();
    }


    // Stock In

    public void stockIn(Item item, int quantity) {
        if (!item.isTrackInventory()) {
    throw new RuntimeException(
        "Inventory operations are not allowed for this item"
    );
}

        Inventory inventory =
                inventoryDAO.findByItemId(item.getId());

        if (inventory == null) {

            inventory = new Inventory();

            inventory.setItem(item);

            inventory.setQuantity(quantity);

            inventory.setCommittedQuantity(0);

            inventoryDAO.save(inventory);

        } else {

            inventory.setQuantity(
                    inventory.getQuantity() + quantity
            );

            inventoryDAO.update(inventory);
        }


        InventoryTransaction transaction =
                new InventoryTransaction();

        transaction.setItem(item);

        transaction.setType("STOCK_IN");

        transaction.setQuantity(quantity);

        transaction.setTransactionDate(
                LocalDateTime.now()
        );

        inventoryTransactionService.addTransaction(
                transaction
        );
    }


    // Stock Out

    public void stockOut(Item item, int quantity) {
        if (!item.isTrackInventory()) {
    throw new RuntimeException(
        "Inventory operations are not allowed for this item"
    );
}

        Inventory inventory =
                inventoryDAO.findByItemId(item.getId());

        if (inventory == null && item.getItemType().equals("GOODS")) {

            throw new RuntimeException(
                    "No inventory available for this item"
            );
        }


        int availableStock =
                inventory.getQuantity()
                        - inventory.getCommittedQuantity();


        if (availableStock < quantity) {

            throw new RuntimeException(
                    "Insufficient available stock"
            );
        }


        inventory.setQuantity(
                inventory.getQuantity() - quantity
        );

        inventoryDAO.update(inventory);


        InventoryTransaction transaction =
                new InventoryTransaction();

        transaction.setItem(item);

        transaction.setType("STOCK_OUT");

        transaction.setQuantity(quantity);

        transaction.setTransactionDate(
                LocalDateTime.now()
        );

        inventoryTransactionService.addTransaction(
                transaction
        );
    }
    // SHIP RESERVED STOCK

public void shipReservedStock(
        Item item,
        int quantity) {
                if (!item.isTrackInventory()) {
    throw new RuntimeException(
        "Inventory operations are not allowed for this item"
    );
}

    Inventory inventory =
            inventoryDAO.findByItemId(
                    item.getId()
            );

    if ( inventory == null && item.getItemType().equals("GOODS")) {

        throw new RuntimeException(
                "No inventory available for this item"
        );
    }


    // The stock being shipped must
    // already be committed

    if (inventory.getCommittedQuantity()
            < quantity) {

        throw new RuntimeException(
                "Insufficient committed stock"
        );
    }


    // Physical stock goes down

    if (inventory.getQuantity()
            < quantity) {

        throw new RuntimeException(
                "Insufficient physical stock"
        );
    }


    inventory.setQuantity(
            inventory.getQuantity()
                    - quantity
    );


    // Committed stock is released
    // because it has now been shipped

    inventory.setCommittedQuantity(
            inventory.getCommittedQuantity()
                    - quantity
    );


    inventoryDAO.update(
            inventory
    );


    // Record physical stock movement

    InventoryTransaction transaction =
            new InventoryTransaction();

    transaction.setItem(item);

    transaction.setType(
            "STOCK_OUT"
    );

    transaction.setQuantity(
            quantity
    );

    transaction.setTransactionDate(
            java.time.LocalDateTime.now()
    );


    inventoryTransactionService.addTransaction(
            transaction
    );
}


    // Reserve Stock

    public void reserveStock(Item item, int quantity) {
        if (!item.isTrackInventory()) {
    throw new RuntimeException(
        "Inventory operations are not allowed for this item"
    );
}

        Inventory inventory =
                inventoryDAO.findByItemId(item.getId());

        if (inventory == null && item.getItemType().equals("GOODS")) {

            throw new RuntimeException(
                    "No inventory available for this item"
            );
        }


        int availableStock =
                inventory.getQuantity()
                        - inventory.getCommittedQuantity();


        if (availableStock < quantity) {

            throw new RuntimeException(
                    "Insufficient available stock"
            );
        }


        inventory.setCommittedQuantity(
                inventory.getCommittedQuantity()
                        + quantity
        );

        inventoryDAO.update(inventory);
    }


    // Release Committed Stock

    public void releaseStock(Item item, int quantity) {
        if (!item.isTrackInventory()) {
    throw new RuntimeException(
        "Inventory operations are not allowed for this item"
    );
}

        Inventory inventory =
                inventoryDAO.findByItemId(item.getId());

        if (inventory == null && item.getItemType().equals("GOODS")) {

            throw new RuntimeException(
                    "No inventory available for this item"
            );
        }


        if (inventory.getCommittedQuantity() < quantity) {

            throw new RuntimeException(
                    "Cannot release more stock than committed"
            );
        }


        inventory.setCommittedQuantity(
                inventory.getCommittedQuantity()
                        - quantity
        );

        inventoryDAO.update(inventory);
    }


    // Get Available Stock

    public int getAvailableStock(Item item) {
        if (!item.isTrackInventory()) {
    throw new RuntimeException(
        "Inventory operations are not allowed for this item"
    );
}

        Inventory inventory =
                inventoryDAO.findByItemId(item.getId());

        if (inventory == null) {
            return 0;
        }


        return inventory.getQuantity()
                - inventory.getCommittedQuantity();
    }


    // Delete Inventory

    public void deleteInventoryByItemId(int itemId) {
        inventoryDAO.deleteByItemId(itemId);
    }
}