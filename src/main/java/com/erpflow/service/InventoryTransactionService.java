package com.erpflow.service;

import com.erpflow.dao.InventoryTransactionDAO;
import com.erpflow.model.InventoryTransaction;

import java.util.List;


public class InventoryTransactionService {

    private final InventoryTransactionDAO
            inventoryTransactionDAO =
            new InventoryTransactionDAO();


    // SAVE INVENTORY TRANSACTION

    public void addTransaction(
            InventoryTransaction inventoryTransaction
    ) {

        inventoryTransactionDAO.save(
                inventoryTransaction
        );
    }


    // GET ALL INVENTORY TRANSACTIONS

    public List<InventoryTransaction>
    getAllTransactions() {

        return inventoryTransactionDAO.findAll();
    }
}