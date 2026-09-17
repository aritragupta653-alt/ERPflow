package com.erpflow.service;

import com.erpflow.dao.ItemDAO;
import com.erpflow.model.Item;

import java.util.List;

public class ItemService {

    private final ItemDAO itemDAO =
            new ItemDAO();


    public void createItem(Item item) {

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

        if (item.getReorderLevel() < 0) {

            throw new RuntimeException(
                    "Reorder level cannot be negative"
            );
        }

        if (item.getStatus() == null) {
            item.setStatus("ACTIVE");
        }

        itemDAO.save(item);
    }


    public List<Item> getAllItems() {

        return itemDAO.findAll();
    }


    public Item getItemById(int id) {

        return itemDAO.findById(id);
    }


    public void updateItem(Item item) {

        if (item == null) {
            throw new RuntimeException(
                    "Item cannot be null"
            );
        }

        Item existing =
                itemDAO.findById(item.getId());

        if (existing == null) {
            throw new RuntimeException(
                    "Item not found"
            );
        }

        itemDAO.update(item);
    }


    public void deleteItem(int id) {

        Item existing =
                itemDAO.findById(id);

        if (existing == null) {
            throw new RuntimeException(
                    "Item not found"
            );
        }

        itemDAO.delete(id);
    }
}