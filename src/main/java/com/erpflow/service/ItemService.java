package com.erpflow.service;

import com.erpflow.dao.ItemDAO;
import com.erpflow.model.Inventory;
import com.erpflow.model.Item;
import java.util.*;


public class ItemService {
    private final InventoryService inventoryService= new InventoryService();

    private final ItemDAO itemDAO =
            new ItemDAO();

    public void addItem(Item item) {

        itemDAO.save(item);
       
        Inventory inventory = new Inventory();
        

        inventory.setItem(item);

        inventory.setQuantity(0);

        inventoryService.addInventory(inventory);

    }

    public List<Item> getAllItems() {

    return itemDAO.findAll();
    }

    public void deleteItem(int id) {
    

    itemDAO.delete(id);

}
public Item getItemById(int id) {

        return itemDAO.findById(id);
    }
    public void updateItem(Item item) {

    itemDAO.update(item);
}
}
