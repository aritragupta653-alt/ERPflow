package com.erpflow.util;

import com.erpflow.model.Item;
import com.erpflow.service.ItemService;

import java.math.BigDecimal;

public class TestItemSave {

    public static void main(String[] args) {

        Item item = new Item();

        item.setName("Wireless Mouse");

        item.setSku("WM-001");

        item.setDescription(
                "Wireless Bluetooth Mouse"
        );

        item.setPurchasePrice(
                new BigDecimal("500.00")
        );

        item.setSellingPrice(
                new BigDecimal("799.00")
        );

        item.setReorderLevel(10);


        ItemService itemService =
                new ItemService();

        itemService.addItem(item);


        System.out.println(
                "Item saved successfully!"
        );
    }
}