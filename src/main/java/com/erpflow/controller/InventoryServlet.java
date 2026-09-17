package com.erpflow.controller;

import com.erpflow.model.Inventory;
import com.erpflow.model.Item;
import com.erpflow.service.InventoryService;
import com.erpflow.service.ItemService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/inventory/*")
public class InventoryServlet extends HttpServlet {

    private final InventoryService inventoryService =
            new InventoryService();

    private final ItemService itemService =
            new ItemService();

    private final ObjectMapper objectMapper =
            new ObjectMapper();


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);

        String pathInfo =
                request.getPathInfo();


        // GET /api/inventory
        if (pathInfo == null ||
                pathInfo.equals("/")) {

            List<Inventory> inventories =
                    inventoryService.getAllInventory();

            objectMapper.writeValue(
                    response.getWriter(),
                    inventories
            );

            return;
        }


        // GET /api/inventory/{itemId}
        try {

            int itemId =
                    Integer.parseInt(
                            pathInfo.substring(1)
                    );

            Inventory inventory =
                    inventoryService
                            .getInventoryByItemId(
                                    itemId
                            );

            if (inventory == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Inventory not found"
                );

                return;
            }

            objectMapper.writeValue(
                    response.getWriter(),
                    inventory
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid item ID"
            );
        }
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);

        String pathInfo =
                request.getPathInfo();


        if (pathInfo == null ||
                pathInfo.equals("/")) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Inventory action is required"
            );

            return;
        }


        try {

            String action =
                    pathInfo.substring(1);

            Map<String, Object> requestData =
                    objectMapper.readValue(
                            request.getReader(),
                            Map.class
                    );

            Object itemIdObject =
                    requestData.get("itemId");

            Object quantityObject =
                    requestData.get("quantity");


            if (itemIdObject == null ||
                    quantityObject == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "itemId and quantity are required"
                );

                return;
            }


            int itemId =
                    ((Number) itemIdObject)
                            .intValue();

            int quantity =
                    ((Number) quantityObject)
                            .intValue();


            if (quantity <= 0) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Quantity must be greater than zero"
                );

                return;
            }


            Item item =
                    itemService.getItemById(
                            itemId
                    );

            if (item == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Item not found"
                );

                return;
            }


            switch (action) {

                case "stock-in":

                    inventoryService.stockIn(
                            item,
                            quantity
                    );

                    break;


                case "stock-out":

                    inventoryService.stockOut(
                            item,
                            quantity
                    );

                    break;


                case "reserve":

                    inventoryService.reserveStock(
                            item,
                            quantity
                    );

                    break;


                case "release":

                    inventoryService.releaseStock(
                            item,
                            quantity
                    );

                    break;


                case "ship":

                    inventoryService
                            .shipReservedStock(
                                    item,
                                    quantity
                            );

                    break;


                default:

                    sendError(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Unknown inventory action"
                    );

                    return;
            }


            Inventory inventory =
                    inventoryService
                            .getInventoryByItemId(
                                    itemId
                            );

            response.setStatus(
                    HttpServletResponse.SC_OK
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    inventory
            );

        } catch (RuntimeException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        }
    }


    private void setJsonResponse(
            HttpServletResponse response) {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );
    }


    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        objectMapper.writeValue(
                response.getWriter(),
                new ErrorResponse(message)
        );
    }


    private static class ErrorResponse {

        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}