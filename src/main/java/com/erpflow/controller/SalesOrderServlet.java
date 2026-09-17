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
                            new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()
                    );

    // =========================
    // GET
    // =========================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            String path = request.getPathInfo();

            // ==========================================
            // GET /api/sales-orders/{id}/items
            // ==========================================

            if (path != null && !path.equals("/")) {

                String[] parts = path.split("/");

                if (parts.length == 3 &&
                        "items".equalsIgnoreCase(parts[2])) {

                    int salesOrderId =
                            Integer.parseInt(parts[1]);

                    SalesOrder order =
                            salesOrderService.getSalesOrderById(
                                    salesOrderId
                            );

                    if (order == null) {

                        response.setStatus(
                                HttpServletResponse.SC_NOT_FOUND
                        );

                        Map<String, String> error =
                                new HashMap<>();

                        error.put(
                                "error",
                                "Sales order not found"
                        );

                        objectMapper.writeValue(
                                response.getWriter(),
                                error
                        );

                        return;
                    }

                    List<SalesOrderItem> orderItems =
                            salesOrderService.getSalesOrderItems(
                                    order
                            );

                    /*
                     * Return only the information needed
                     * by the Package page.
                     *
                     * This also avoids Jackson recursion.
                     */

                    List<Map<String, Object>> result =
                            new ArrayList<>();

                    for (SalesOrderItem orderItem : orderItems) {

                        Map<String, Object> item =
                                new HashMap<>();

                        item.put(
                                "itemId",
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

                        item.put(
                                "quantity",
                                orderItem.getQuantity()
                        );

                        item.put(
                                "sellingPrice",
                                orderItem.getSellingPrice()
                        );

                        result.add(item);
                    }

                    objectMapper.writeValue(
                            response.getWriter(),
                            result
                    );

                    return;
                }
            }

            // ==========================================
            // GET /api/sales-orders
            // ==========================================

            String idParameter =
                    request.getParameter("id");

            if (idParameter == null ||
                    idParameter.isBlank()) {

                List<SalesOrder> orders =
                        salesOrderService
                                .getAllSalesOrders();

                objectMapper.writeValue(
                        response.getWriter(),
                        orders
                );

                return;
            }

            // ==========================================
            // GET /api/sales-orders?id=1
            // ==========================================

            int id =
                    Integer.parseInt(idParameter);

            SalesOrder order =
                    salesOrderService
                            .getSalesOrderById(id);

            if (order == null) {

                response.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                Map<String, String> error =
                        new HashMap<>();

                error.put(
                        "error",
                        "Sales order not found"
                );

                objectMapper.writeValue(
                        response.getWriter(),
                        error
                );

                return;
            }

            List<SalesOrderItem> orderItems =
                    salesOrderService
                            .getSalesOrderItems(order);

            Map<String, Object> result =
                    new HashMap<>();

            result.put("order", order);
            result.put("items", orderItems);

            objectMapper.writeValue(
                    response.getWriter(),
                    result
            );

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            Map<String, String> error =
                    new HashMap<>();

            error.put(
                    "error",
                    "Invalid sales order ID"
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    error
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            Map<String, String> error =
                    new HashMap<>();

            error.put(
                    "error",
                    e.getMessage()
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    error
            );
        }
    }

    // =========================
    // POST
    // =========================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            JsonNode root =
                    objectMapper.readTree(
                            request.getInputStream()
                    );

            int customerId =
                    root.get("customerId").asInt();

            Customer customer =
                    customerService
                            .getCustomerById(customerId);

            if (customer == null) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                Map<String, String> error =
                        new HashMap<>();

                error.put(
                        "error",
                        "Customer not found"
                );

                objectMapper.writeValue(
                        response.getWriter(),
                        error
                );

                return;
            }

            SalesOrder salesOrder =
                    new SalesOrder();

            salesOrder.setCustomer(customer);

            salesOrder.setOrderDate(
                    LocalDateTime.now()
            );

            salesOrder.setStatus("CREATED");

            List<SalesOrderItem> salesOrderItems =
                    new ArrayList<>();

            JsonNode itemsNode =
                    root.get("items");

            if (itemsNode == null ||
                    !itemsNode.isArray() ||
                    itemsNode.isEmpty()) {

                throw new RuntimeException(
                        "Sales Order must contain at least one item"
                );
            }

            for (JsonNode itemNode : itemsNode) {

                int itemId =
                        itemNode
                                .get("itemId")
                                .asInt();

                int quantity =
                        itemNode
                                .get("quantity")
                                .asInt();

                BigDecimal sellingPrice =
                        itemNode
                                .get("sellingPrice")
                                .decimalValue();

                Item item =
                        itemService
                                .getItemById(itemId);

                if (item == null) {

                    throw new RuntimeException(
                            "Item not found: " + itemId
                    );
                }

                SalesOrderItem orderItem =
                        new SalesOrderItem();

                orderItem.setItem(item);

                orderItem.setQuantity(quantity);

                orderItem.setSellingPrice(
                        sellingPrice
                );

                salesOrderItems.add(orderItem);
            }

            salesOrderService.createSalesOrder(
                    salesOrder,
                    salesOrderItems
            );

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

            objectMapper.writeValue(
                    response.getWriter(),
                    result
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            Map<String, String> error =
                    new HashMap<>();

            error.put(
                    "error",
                    e.getMessage()
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    error
            );
        }
    }
}