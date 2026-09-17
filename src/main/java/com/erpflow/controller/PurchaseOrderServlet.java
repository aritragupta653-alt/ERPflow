package com.erpflow.controller;

import com.erpflow.model.PurchaseOrder;
import com.erpflow.model.PurchaseOrderItem;
import com.erpflow.model.Supplier;
import com.erpflow.model.Item;

import com.erpflow.service.PurchaseOrderService;
import com.erpflow.service.SupplierService;
import com.erpflow.service.ItemService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/purchase-orders/*")
public class PurchaseOrderServlet extends HttpServlet {

    private final PurchaseOrderService purchaseOrderService =
            new PurchaseOrderService();

    private final SupplierService supplierService =
            new SupplierService();

    private final ItemService itemService =
            new ItemService();

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());


    // ========================================
    // GET
    // ========================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );


        String pathInfo =
                request.getPathInfo();


        try {

            // GET /api/purchase-orders

            if (pathInfo == null ||
                    pathInfo.equals("/")) {

                List<PurchaseOrder> orders =
                        purchaseOrderService
                                .getAllPurchaseOrders();

                List<Map<String, Object>> result =
                        new ArrayList<>();


                for (PurchaseOrder order : orders) {

                    result.add(
                            convertOrder(order)
                    );
                }


                objectMapper.writeValue(
                        response.getWriter(),
                        result
                );

                return;
            }


            // GET /api/purchase-orders/{id}

            int id =
                    Integer.parseInt(
                            pathInfo.substring(1)
                    );


            PurchaseOrder order =
                    purchaseOrderService
                            .getPurchaseOrderById(id);


            if (order == null) {

                sendError(
                        response,
                        404,
                        "Purchase Order not found"
                );

                return;
            }


            Map<String, Object> result =
                    convertOrder(order);


            List<PurchaseOrderItem> items =
                    purchaseOrderService
                            .getPurchaseOrderItems(
                                    order
                            );


            List<Map<String, Object>> itemList =
                    new ArrayList<>();


            for (PurchaseOrderItem orderItem
                    : items) {

                Map<String, Object> itemMap =
                        new HashMap<>();

                itemMap.put(
                        "id",
                        orderItem.getId()
                );

                itemMap.put(
                        "quantity",
                        orderItem.getQuantity()
                );

                itemMap.put(
                        "purchasePrice",
                        orderItem.getPurchasePrice()
                );

                if (orderItem.getItem() != null) {

                    Map<String, Object> item =
                            new HashMap<>();

                    item.put(
                            "id",
                            orderItem.getItem().getId()
                    );

                    item.put(
                            "name",
                            orderItem.getItem().getName()
                    );

                    item.put(
                            "sku",
                            orderItem.getItem().getSku()
                    );

                    itemMap.put(
                            "item",
                            item
                    );
                }


                itemList.add(itemMap);
            }


            result.put(
                    "items",
                    itemList
            );


            objectMapper.writeValue(
                    response.getWriter(),
                    result
            );


        } catch (NumberFormatException e) {

            sendError(
                    response,
                    400,
                    "Invalid purchase order ID"
            );

        } catch (RuntimeException e) {

            e.printStackTrace();

            sendError(
                    response,
                    400,
                    e.getMessage()
            );
        }
    }


    // ========================================
    // POST
    // ========================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );


        try {

            String pathInfo =
                    request.getPathInfo();


            // RECEIVE

            if (pathInfo != null &&
                    pathInfo.matches("/\\d+/receive")) {

                int id =
                        Integer.parseInt(
                                pathInfo
                                        .split("/")[1]
                        );


                purchaseOrderService
                        .receivePurchaseOrder(id);


                response.setStatus(
                        HttpServletResponse.SC_OK
                );


                Map<String, Object> result =
                        new HashMap<>();

                result.put(
                        "message",
                        "Purchase Order received successfully"
                );

                result.put(
                        "purchaseOrderId",
                        id
                );


                objectMapper.writeValue(
                        response.getWriter(),
                        result
                );

                return;
            }


            // CREATE

            Map<String, Object> body =
                    objectMapper.readValue(
                            request.getReader(),
                            Map.class
                    );


            Number supplierIdNumber =
                    (Number) body.get(
                            "supplierId"
                    );


            if (supplierIdNumber == null) {

                sendError(
                        response,
                        400,
                        "Supplier ID is required"
                );

                return;
            }


            int supplierId =
                    supplierIdNumber.intValue();


            Supplier supplier =
                    supplierService
                            .getSupplierById(
                                    supplierId
                            );


            if (supplier == null) {

                sendError(
                        response,
                        404,
                        "Supplier not found"
                );

                return;
            }


            PurchaseOrder purchaseOrder =
                    new PurchaseOrder();


            purchaseOrder.setSupplier(
                    supplier
            );

            purchaseOrder.setStatus(
                    "CREATED"
            );

            purchaseOrder.setOrderDate(
                    LocalDateTime.now()
            );


            List<Map<String, Object>> items =
                    (List<Map<String, Object>>)
                            body.get("items");


            if (items == null ||
                    items.isEmpty()) {

                sendError(
                        response,
                        400,
                        "At least one item is required"
                );

                return;
            }


            List<PurchaseOrderItem>
                    purchaseOrderItems =
                    new ArrayList<>();


            for (
                    Map<String, Object> itemData
                    : items
            ) {

                Number itemIdNumber =
                        (Number) itemData.get(
                                "itemId"
                        );

                Number quantityNumber =
                        (Number) itemData.get(
                                "quantity"
                        );


                if (itemIdNumber == null ||
                        quantityNumber == null) {

                    throw new RuntimeException(
                            "Item ID and quantity are required"
                    );
                }


                Item item =
                        itemService.getItemById(
                                itemIdNumber.intValue()
                        );


                if (item == null) {

                    throw new RuntimeException(
                            "Item not found"
                    );
                }


                PurchaseOrderItem
                        purchaseOrderItem =
                        new PurchaseOrderItem();


                purchaseOrderItem.setItem(
                        item
                );


                purchaseOrderItem.setQuantity(
                        quantityNumber.intValue()
                );


                Object price =
                        itemData.get(
                                "purchasePrice"
                        );


                if (price == null) {

                    purchaseOrderItem
                            .setPurchasePrice(
                                    item.getPurchasePrice()
                            );

                } else {

                    purchaseOrderItem
                            .setPurchasePrice(
                                    new BigDecimal(
                                            price.toString()
                                    )
                            );
                }


                purchaseOrderItems.add(
                        purchaseOrderItem
                );
            }


            purchaseOrderService
                    .createPurchaseOrder(
                            purchaseOrder,
                            purchaseOrderItems
                    );


            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );


            objectMapper.writeValue(
                    response.getWriter(),
                    convertOrder(purchaseOrder)
            );


        } catch (RuntimeException e) {

            e.printStackTrace();

            sendError(
                    response,
                    400,
                    e.getMessage()
            );
        }
    }


    // ========================================
    // CONVERT ORDER TO JSON
    // ========================================

    private Map<String, Object> convertOrder(
            PurchaseOrder order
    ) {

        Map<String, Object> result =
                new HashMap<>();


        result.put(
                "id",
                order.getId()
        );

        result.put(
                "status",
                order.getStatus()
        );

        result.put(
                "orderDate",
                order.getOrderDate()
        );


        if (order.getSupplier() != null) {

            Map<String, Object> supplier =
                    new HashMap<>();

            supplier.put(
                    "id",
                    order.getSupplier().getId()
            );

            supplier.put(
                    "name",
                    order.getSupplier().getName()
            );

            supplier.put(
                    "contactPerson",
                    order.getSupplier()
                            .getContactPerson()
            );

            supplier.put(
                    "phone",
                    order.getSupplier().getPhone()
            );

            supplier.put(
                    "email",
                    order.getSupplier().getEmail()
            );

            supplier.put(
                    "address",
                    order.getSupplier().getAddress()
            );


            result.put(
                    "supplier",
                    supplier
            );
        }


        return result;
    }


    // ========================================
    // ERROR
    // ========================================

    private void sendError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        response.setStatus(status);

        Map<String, String> error =
                new HashMap<>();

        error.put(
                "error",
                message != null
                        ? message
                        : "Unknown error"
        );


        objectMapper.writeValue(
                response.getWriter(),
                error
        );
    }
}