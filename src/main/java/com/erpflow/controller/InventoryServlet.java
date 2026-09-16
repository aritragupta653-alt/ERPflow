package com.erpflow.controller;

import com.erpflow.service.InventoryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import com.erpflow.model.Inventory;
import com.erpflow.model.Item;
import com.erpflow.service.ItemService;

import java.util.List;

@WebServlet("/inventory")
public class InventoryServlet extends HttpServlet {

    private final ItemService itemService = new ItemService();

    private final InventoryService inventoryService = new InventoryService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        List<Item> items = itemService.getAllItems();
        List<Inventory> inventories = inventoryService.getAllInventory();

        request.setAttribute(
                "inventories",
                inventories);

        request.setAttribute(
                "items",
                items);

        request.getRequestDispatcher(
                "/WEB-INF/views/inventory.jsp").forward(request, response);
    }
    @Override
protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
) throws ServletException, IOException {

    String action =
            request.getParameter("action");


    int itemId = Integer.parseInt(
            request.getParameter("itemId")
    );

    int quantity = Integer.parseInt(
            request.getParameter("quantity")
    );


    Item item =
            itemService.getItemById(itemId);


    if ("stockIn".equals(action)) {

        inventoryService.stockIn(
                item,
                quantity
        );
    }


    else if ("stockOut".equals(action)) {

        inventoryService.stockOut(
                item,
                quantity
        );
    }


    response.sendRedirect(
            request.getContextPath()
                    + "/inventory"
    );
}
}