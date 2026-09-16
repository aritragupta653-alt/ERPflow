package com.erpflow.util;

import com.erpflow.model.Item;
import com.erpflow.service.ItemService;

import java.util.List;

public class TestItemRead {

    public static void main(String[] args) {

        ItemService itemService =
                new ItemService();

        List<Item> items =
                itemService.getAllItems();

        for (Item item : items) {

            System.out.println(
                    item.getId()
                    + " | "
                    + item.getName()
                    + " | "
                    + item.getSku()
            );
        }
    }
}