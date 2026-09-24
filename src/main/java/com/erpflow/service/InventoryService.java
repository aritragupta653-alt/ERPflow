package com.erpflow.service;

import com.erpflow.dao.InventoryDAO;
import com.erpflow.model.Inventory;
import com.erpflow.model.InventoryTransaction;
import com.erpflow.model.Item;
import com.erpflow.dao.ItemDAO;
import java.time.LocalDateTime;
import java.util.List;
import com.erpflow.dao.ItemDAO;

public class InventoryService {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

   
   private final ItemDAO itemDAO = new ItemDAO();


private final InventoryTransactionService inventoryTransactionService =
        new InventoryTransactionService();

        private final InventoryAlertService inventoryAlertService =
        new InventoryAlertService();


    


    // Stock In

    public void stockIn(Item item, int quantity) {

    if (item == null) {
        throw new RuntimeException("Item is required");
    }

    if (!item.isTrackInventory()) {
        throw new RuntimeException(
                "Inventory operations are not allowed for this item"
        );
    }

    if (quantity <= 0) {
        throw new RuntimeException(
                "Quantity must be greater than zero"
        );
    }

    Item existingItem = itemDAO.findById(item.getId());

    if (existingItem == null) {
        throw new RuntimeException("Item not found");
    }

    existingItem.setInHandQuantity(
            existingItem.getInHandQuantity() + quantity
    );

    itemDAO.update(existingItem);

    InventoryTransaction transaction = new InventoryTransaction();
    transaction.setItem(existingItem);
    transaction.setType("STOCK_IN");
    transaction.setQuantity(quantity);
    transaction.setTransactionDate(LocalDateTime.now());

    inventoryTransactionService.addTransaction(transaction);
    Item updatedItem = itemDAO.findById(item.getId());

    if (updatedItem != null) {
        inventoryAlertService.checkAlerts(updatedItem);
    }
}

    // Stock Out

    public void stockOut(Item item, int quantity) {

    if (item == null) {
        throw new RuntimeException("Item is required");
    }

    if (!item.isTrackInventory()) {
        throw new RuntimeException(
                "Inventory operations are not allowed for this item"
        );
    }

    if (quantity <= 0) {
        throw new RuntimeException(
                "Quantity must be greater than zero"
        );
    }

    Item existingItem = itemDAO.findById(item.getId());

    if (existingItem == null) {
        throw new RuntimeException("Item not found");
    }

    int availableStock =
            existingItem.getInHandQuantity()
                    - existingItem.getCommittedQuantity();

    if (availableStock < quantity) {
        throw new RuntimeException("Insufficient available stock");
    }

    existingItem.setInHandQuantity(
            existingItem.getInHandQuantity() - quantity
    );

    itemDAO.update(existingItem);

    InventoryTransaction transaction = new InventoryTransaction();
    transaction.setItem(existingItem);
    transaction.setType("STOCK_OUT");
    transaction.setQuantity(quantity);
    transaction.setTransactionDate(LocalDateTime.now());

    inventoryTransactionService.addTransaction(transaction);
    Item updatedItem = itemDAO.findById(item.getId());

    if (updatedItem != null) {
        inventoryAlertService.checkAlerts(updatedItem);
    }
}
    // SHIP RESERVED STOCK

// Ship Reserved Stock

public void shipReservedStock(Item item, int quantity) {

    if (item == null) {
        throw new RuntimeException("Item is required");
    }

    if (!item.isTrackInventory()) {
        throw new RuntimeException(
                "Inventory operations are not allowed for this item"
        );
    }

    if (quantity <= 0) {
        throw new RuntimeException(
                "Quantity must be greater than zero"
        );
    }

    Item existingItem = itemDAO.findById(item.getId());

    if (existingItem == null) {
        throw new RuntimeException("Item not found");
    }

    // The stock being shipped must already be committed

    if (existingItem.getCommittedQuantity() < quantity) {
        throw new RuntimeException(
                "Insufficient committed stock"
        );
    }

    // Physical stock goes down

    if (existingItem.getInHandQuantity() < quantity) {
        throw new RuntimeException(
                "Insufficient physical stock"
        );
    }

    existingItem.setInHandQuantity(
            existingItem.getInHandQuantity() - quantity
    );

    // Committed stock is released because it has now been shipped

    existingItem.setCommittedQuantity(
            existingItem.getCommittedQuantity() - quantity
    );

    itemDAO.update(existingItem);

    // Record physical stock movement

    InventoryTransaction transaction = new InventoryTransaction();

    transaction.setItem(existingItem);
    transaction.setType("STOCK_OUT");
    transaction.setQuantity(quantity);
    transaction.setTransactionDate(LocalDateTime.now());

    inventoryTransactionService.addTransaction(transaction);
    Item updatedItem = itemDAO.findById(item.getId());

    if (updatedItem != null) {
        inventoryAlertService.checkAlerts(updatedItem);
    }
}


    // Reserve Stock

   // Reserve Stock

public void reserveStock(Item item, int quantity) {

    if (item == null) {
        throw new RuntimeException("Item is required");
    }

    if (!item.isTrackInventory()) {
        throw new RuntimeException(
                "Inventory operations are not allowed for this item"
        );
    }

    if (quantity <= 0) {
        throw new RuntimeException(
                "Quantity must be greater than zero"
        );
    }

    Item existingItem = itemDAO.findById(item.getId());

    if (existingItem == null) {
        throw new RuntimeException("Item not found");
    }

    int availableStock =
            existingItem.getInHandQuantity()
                    - existingItem.getCommittedQuantity();

    if (availableStock < quantity) {
        throw new RuntimeException(
                "Insufficient available stock"
        );
    }

    existingItem.setCommittedQuantity(
            existingItem.getCommittedQuantity() + quantity
    );

    itemDAO.update(existingItem);
    Item updatedItem = itemDAO.findById(item.getId());

    if (updatedItem != null) {
        inventoryAlertService.checkAlerts(updatedItem);
    }
}
    // Release Committed Stock

    // Release Committed Stock

public void releaseStock(Item item, int quantity) {

    if (item == null) {
        throw new RuntimeException("Item is required");
    }

    if (!item.isTrackInventory()) {
        throw new RuntimeException(
                "Inventory operations are not allowed for this item"
        );
    }

    if (quantity <= 0) {
        throw new RuntimeException(
                "Quantity must be greater than zero"
        );
    }

    Item existingItem = itemDAO.findById(item.getId());

    if (existingItem == null) {
        throw new RuntimeException("Item not found");
    }

    if (existingItem.getCommittedQuantity() < quantity) {
        throw new RuntimeException(
                "Cannot release more stock than committed"
        );
    }

    existingItem.setCommittedQuantity(
            existingItem.getCommittedQuantity() - quantity
    );

    itemDAO.update(existingItem);
    Item updatedItem = itemDAO.findById(item.getId());

    if (updatedItem != null) {
        inventoryAlertService.checkAlerts(updatedItem);
    }
}

    // Get Available Stock

    // Get Available Stock

public int getAvailableStock(Item item) {

    if (item == null) {
        throw new RuntimeException("Item is required");
    }

    if (!item.isTrackInventory()) {
        throw new RuntimeException(
                "Inventory operations are not allowed for this item"
        );
    }

    Item existingItem = itemDAO.findById(item.getId());

    if (existingItem == null) {
        return 0;
    }

    return existingItem.getInHandQuantity()
            - existingItem.getCommittedQuantity();
}
}