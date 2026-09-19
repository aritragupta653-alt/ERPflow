package com.erpflow.controller;

import com.erpflow.model.Customer;
import com.erpflow.model.Item;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.service.CustomerService;
import com.erpflow.service.ItemService;
import com.erpflow.service.SalesOrderService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.ServletException;
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

@WebServlet("/api/sales-orders/*")
public class SalesOrderServlet extends HttpServlet {

    private final SalesOrderService salesOrderService =
            new SalesOrderService();

    private final CustomerService customerService =
            new CustomerService();

    private final ItemService itemService =
            new ItemService();

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(
                            new JavaTimeModule()
                    );


    // =========================================================
    // GET
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    )
            throws ServletException, IOException {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );


        try {

            String path =
                    request.getPathInfo();


            // =================================================
            // GET /api/sales-orders/{id}/items
            // =================================================

            if (
                    path != null &&
                    !path.equals("/")
            ) {

                String[] parts =
                        path.split("/");


                if (
                        parts.length == 3 &&
                        "items".equalsIgnoreCase(
                                parts[2]
                        )
                ) {

                    int salesOrderId =
                            Integer.parseInt(
                                    parts[1]
                            );


                    SalesOrder order =
                            salesOrderService
                                    .getSalesOrderById(
                                            salesOrderId
                                    );


                    if (order == null) {

                        sendError(
                                response,
                                HttpServletResponse.SC_NOT_FOUND,
                                "Sales order not found"
                        );

                        return;
                    }


                    List<SalesOrderItem>
                            orderItems =
                            salesOrderService
                                    .getSalesOrderItems(
                                            order
                                    );


                    /*
                     * Return simplified objects
                     * to avoid Jackson recursion.
                     */

                    List<Map<String, Object>>
                            result =
                            new ArrayList<>();


                    for (
                            SalesOrderItem orderItem :
                            orderItems
                    ) {

                        Map<String, Object>
                                item =
                                new HashMap<>();


                        item.put(
                                "itemId",
                                orderItem
                                        .getItem()
                                        .getId()
                        );

                        item.put(
                                "name",
                                orderItem
                                        .getItem()
                                        .getName()
                        );

                        item.put(
                                "sku",
                                orderItem
                                        .getItem()
                                        .getSku()
                        );

                        item.put(
                                "quantity",
                                orderItem
                                        .getQuantity()
                        );

                        item.put(
                                "sellingPrice",
                                orderItem
                                        .getSellingPrice()
                        );


                        result.add(item);
                    }


                    objectMapper.writeValue(
                            response.getWriter(),
                            result
                    );

                    return;
                }


                // =============================================
                // GET /api/sales-orders/{id}
                // =============================================

                if (parts.length == 2) {

                    int salesOrderId =
                            Integer.parseInt(
                                    parts[1]
                            );


                    SalesOrder order =
                            salesOrderService
                                    .getSalesOrderById(
                                            salesOrderId
                                    );


                    if (order == null) {

                        sendError(
                                response,
                                HttpServletResponse.SC_NOT_FOUND,
                                "Sales order not found"
                        );

                        return;
                    }


                    List<SalesOrderItem>
                            orderItems =
                            salesOrderService
                                    .getSalesOrderItems(
                                            order
                                    );


                    /*
                     * Return a flattened response.
                     *
                     * This is what the current
                     * salesOrderDetails.js expects.
                     */

                    Map<String, Object>
                            result =
                            new HashMap<>();


                    result.put(
                            "id",
                            order.getId()
                    );

                    result.put(
                            "orderDate",
                            order.getOrderDate()
                    );

                    result.put(
                            "status",
                            order.getStatus()
                    );

                    result.put(
                            "customer",
                            order.getCustomer()
                    );

                    result.put(
                            "items",
                            orderItems
                    );


                    // TAX

                    result.put(
                            "taxRate",
                            order.getTaxRate()
                    );

                    result.put(
                            "subtotal",
                            order.getSubtotal()
                    );

                    result.put(
                            "taxAmount",
                            order.getTaxAmount()
                    );

                    result.put(
                            "totalAmount",
                            order.getTotalAmount()
                    );


                    objectMapper.writeValue(
                            response.getWriter(),
                            result
                    );

                    return;
                }
            }


            // =================================================
            // GET /api/sales-orders
            // =================================================

            List<SalesOrder> orders =
                    salesOrderService
                            .getAllSalesOrders();


            objectMapper.writeValue(
                    response.getWriter(),
                    orders
            );


        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid sales order ID"
            );


        } catch (Exception e) {

            e.printStackTrace();


            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );
        }
    }


    // =========================================================
    // POST
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    )
            throws ServletException, IOException {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );


        try {

            JsonNode root =
                    objectMapper.readTree(
                            request.getInputStream()
                    );


            // =================================================
            // CUSTOMER
            // =================================================

            JsonNode customerNode =
                    root.get("customerId");


            if (
                    customerNode == null ||
                    customerNode.isNull()
            ) {

                throw new RuntimeException(
                        "Customer is required"
                );
            }


            int customerId =
                    customerNode.asInt();


            Customer customer =
                    customerService
                            .getCustomerById(
                                    customerId
                            );


            if (customer == null) {

                throw new RuntimeException(
                        "Customer not found"
                );
            }


            // =================================================
            // TAX RATE
            // =================================================

            BigDecimal taxRate =
                    BigDecimal.ZERO;


            JsonNode taxNode =
                    root.get("taxRate");


            if (
                    taxNode != null &&
                    !taxNode.isNull()
            ) {

                taxRate =
                        taxNode.decimalValue();
            }


            // =================================================
            // CREATE ORDER
            // =================================================

            SalesOrder salesOrder =
                    new SalesOrder();


            salesOrder.setCustomer(
                    customer
            );


            salesOrder.setOrderDate(
                    LocalDateTime.now()
            );


            salesOrder.setStatus(
                    "CREATED"
            );


            salesOrder.setTaxRate(
                    taxRate
            );


            // =================================================
            // ITEMS
            // =================================================

            JsonNode itemsNode =
                    root.get("items");


            if (
                    itemsNode == null ||
                    !itemsNode.isArray() ||
                    itemsNode.isEmpty()
            ) {

                throw new RuntimeException(
                        "Sales Order must contain at least one item"
                );
            }


            List<SalesOrderItem>
                    salesOrderItems =
                    new ArrayList<>();


            for (
                    JsonNode itemNode :
                    itemsNode
            ) {

                JsonNode itemIdNode =
                        itemNode.get("itemId");


                JsonNode quantityNode =
                        itemNode.get("quantity");


                JsonNode priceNode =
                        itemNode.get("sellingPrice");


                if (
                        itemIdNode == null ||
                        quantityNode == null ||
                        priceNode == null
                ) {

                    throw new RuntimeException(
                            "Each order item must contain itemId, quantity and sellingPrice"
                    );
                }


                int itemId =
                        itemIdNode.asInt();


                int quantity =
                        quantityNode.asInt();


                BigDecimal sellingPrice =
                        priceNode.decimalValue();


                Item item =
                        itemService
                                .getItemById(
                                        itemId
                                );


                if (item == null) {

                    throw new RuntimeException(
                            "Item not found: "
                                    + itemId
                    );
                }


                SalesOrderItem orderItem =
                        new SalesOrderItem();


                orderItem.setItem(item);

                orderItem.setQuantity(
                        quantity
                );

                orderItem.setSellingPrice(
                        sellingPrice
                );


                salesOrderItems.add(
                        orderItem
                );
            }


            // =================================================
            // CREATE
            // =================================================

            salesOrderService.createSalesOrder(
                    salesOrder,
                    salesOrderItems
            );


            // =================================================
            // RESPONSE
            // =================================================

            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );


            Map<String, Object> result =
                    new HashMap<>();


            result.put(
                    "message",
                    "Sales order created successfully"
            );

            result.put(
                    "salesOrderId",
                    salesOrder.getId()
            );

            result.put(
                    "subtotal",
                    salesOrder.getSubtotal()
            );

            result.put(
                    "taxRate",
                    salesOrder.getTaxRate()
            );

            result.put(
                    "taxAmount",
                    salesOrder.getTaxAmount()
            );

            result.put(
                    "totalAmount",
                    salesOrder.getTotalAmount()
            );


            objectMapper.writeValue(
                    response.getWriter(),
                    result
            );


        } catch (Exception e) {

            e.printStackTrace();


            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );


            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        }
    }


    // =========================================================
    // ERROR RESPONSE
    // =========================================================

    private void sendError(
            HttpServletResponse response,
            int status,
            String message
    )
            throws IOException {

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