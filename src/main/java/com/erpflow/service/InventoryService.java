package com.erpflow.service;

import com.erpflow.dao.InventoryDAO;
import com.erpflow.model.Inventory;
import com.erpflow.model.InventoryTransaction;
import com.erpflow.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public class InventoryService {

    private final InventoryDAO inventoryDAO =
            new InventoryDAO();

    private final InventoryTransactionService
            inventoryTransactionService =
            new InventoryTransactionService();


    public void addInventory(
            Inventory inventory
    ) {

        inventoryDAO.save(inventory);
    }


    public Inventory getInventoryByItemId(
            int itemId
    ) {

        return inventoryDAO.findByItemId(itemId);
    }


    public List<Inventory> getAllInventory() {

        return inventoryDAO.findAll();
    }


    // STOCK IN

    public void stockIn(
            Item item,
            int quantity
    ) {

        Inventory inventory =
                inventoryDAO.findByItemId(
                        item.getId()
                );


        if (inventory == null) {

            inventory = new Inventory();

            inventory.setItem(item);

            inventory.setQuantity(quantity);

            inventoryDAO.save(inventory);

        } else {

            inventory.setQuantity(
                    inventory.getQuantity()
                            + quantity
            );

            inventoryDAO.update(inventory);
        }


        // CREATE INVENTORY TRANSACTION

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


    // STOCK OUT

    public void stockOut(
            Item item,
            int quantity
    ) {

        Inventory inventory =
                inventoryDAO.findByItemId(
                        item.getId()
                );


        if (inventory == null) {

            throw new RuntimeException(
                    "No inventory available for this item"
            );
        }


        if (inventory.getQuantity() < quantity) {

            throw new RuntimeException(
                    "Insufficient stock"
            );
        }


        // REDUCE STOCK

        inventory.setQuantity(
                inventory.getQuantity()
                        - quantity
        );


        inventoryDAO.update(inventory);


        // CREATE INVENTORY TRANSACTION

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
    }public void deleteInventoryByItemId(
        int itemId
) {

    inventoryDAO.deleteByItemId(itemId);
}

}