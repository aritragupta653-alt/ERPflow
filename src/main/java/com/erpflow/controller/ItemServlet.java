package com.erpflow.controller;


import com.erpflow.model.Item;

import com.erpflow.service.ItemService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/items")
public class ItemServlet extends HttpServlet {

    private final ItemService itemService =
            new ItemService();


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String action =
                request.getParameter("action");


        // EDIT ITEM
        if ("edit".equals(action)) {

            int id = Integer.parseInt(
                    request.getParameter("id")
            );

            Item item =
                    itemService.getItemById(id);

            request.setAttribute(
                    "item",
                    item
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/editItem.jsp"
            ).forward(request, response);

            return;
        }


        // DISPLAY ALL ITEMS

        List<Item> items =
                itemService.getAllItems();

        request.setAttribute(
                "items",
                items
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/items.jsp"
        ).forward(request, response);
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String action =
                request.getParameter("action");


        // =========================
        // DELETE ITEM
        // =========================

        if ("delete".equals(action)) {

            int id = Integer.parseInt(
                    request.getParameter("id")
            );

            itemService.deleteItem(id);

            response.sendRedirect(
                    request.getContextPath()
                            + "/items"
            );

            return;
        }


        // =========================
        // UPDATE ITEM
        // =========================

        if ("update".equals(action)) {

            int id = Integer.parseInt(
                    request.getParameter("id")
            );


            Item item = itemService.getItemById(id);

            item.setId(id);

            item.setName(
                    request.getParameter("name")
            );

            item.setSku(
                    request.getParameter("sku")
            );

            item.setDescription(
                    request.getParameter("description")
            );

            item.setPurchasePrice(
                    new BigDecimal(
                            request.getParameter(
                                    "purchasePrice"
                            )
                    )
            );

            item.setSellingPrice(
                    new BigDecimal(
                            request.getParameter(
                                    "sellingPrice"
                            )
                    )
            );

            item.setReorderLevel(
                    Integer.parseInt(
                            request.getParameter(
                                    "reorderLevel"
                            )
                    )
            );


            itemService.updateItem(item);

       


            response.sendRedirect(
                    request.getContextPath()
                            + "/items"
            );

            return;
        }


        // =========================
        // ADD ITEM
        // =========================

        Item item = new Item();


        item.setName(
                request.getParameter("name")
        );

        item.setSku(
                request.getParameter("sku")
        );

        item.setDescription(
                request.getParameter("description")
        );

        item.setPurchasePrice(
                new BigDecimal(
                        request.getParameter(
                                "purchasePrice"
                        )
                )
        );

        item.setSellingPrice(
                new BigDecimal(
                        request.getParameter(
                                "sellingPrice"
                        )
                )
        );

        item.setReorderLevel(
                Integer.parseInt(
                        request.getParameter(
                                "reorderLevel"
                        )
                )
        );
        item.setStatus("ACTIVE");


        itemService.addItem(item);
    


        response.sendRedirect(
                request.getContextPath()
                        + "/items"
        );
    }
}