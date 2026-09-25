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
import java.time.LocalDate;

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
import com.erpflow.model.enums.SalesOrderStatus;

@WebServlet("/api/sales-orders/*")
public class SalesOrderServlet extends HttpServlet {

    private final SalesOrderService salesOrderService = new SalesOrderService();

    private final CustomerService customerService = new CustomerService();

    private final ItemService itemService = new ItemService();

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // =========================================================
    // GET
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        setJsonResponse(response);

        try {
            String path = request.getPathInfo();

            if (path != null && !path.equals("/")) {
                String[] parts = path.split("/");

                // GET /api/sales-orders/{id}/items
                if (parts.length == 3 &&
                        "items".equalsIgnoreCase(parts[2])) {

                    int id = Integer.parseInt(parts[1]);

                    SalesOrder order = salesOrderService.getSalesOrderById(id);

                    if (order == null) {
                        sendError(
                                response,
                                HttpServletResponse.SC_NOT_FOUND,
                                "Sales order not found");
                        return;
                    }

                    List<Map<String, Object>> result = new ArrayList<>();

                    for (SalesOrderItem orderItem : salesOrderService.getSalesOrderItems(order)) {

                        Map<String, Object> item = new HashMap<>();
                        item.put("itemId", orderItem.getItem().getId());
                        item.put("name", orderItem.getItem().getName());
                        item.put("sku", orderItem.getItem().getSku());
                        item.put("quantity", orderItem.getQuantity());
                        item.put("sellingPrice", orderItem.getSellingPrice());

                        result.add(item);
                    }

                    objectMapper.writeValue(response.getWriter(), result);
                    return;
                }

                // GET /api/sales-orders/{id}
                if (parts.length == 2) {
                    int id = Integer.parseInt(parts[1]);

                    SalesOrder order = salesOrderService.getSalesOrderById(id);

                    if (order == null) {
                        sendError(
                                response,
                                HttpServletResponse.SC_NOT_FOUND,
                                "Sales order not found");
                        return;
                    }

                    Map<String, Object> result = new HashMap<>();
                    result.put("id", order.getId());
                    result.put("orderDate", order.getOrderDate());
                    result.put("status", order.getStatus());
                    result.put("customer", order.getCustomer());
                    result.put(
                            "items",
                            salesOrderService.getSalesOrderItems(order));
                    result.put("taxRate", order.getTaxRate());
                    result.put("subtotal", order.getSubtotal());
                    result.put("taxAmount", order.getTaxAmount());
                    result.put("totalAmount", order.getTotalAmount());
                    result.put("expectedDeliveryDate",order.getExpectedDeliveryDate());

                    objectMapper.writeValue(response.getWriter(), result);
                    return;
                }
            }

            // GET /api/sales-orders
            // GET /api/sales-orders?status=CREATED
            String status = request.getParameter("status");

            if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {

                objectMapper.writeValue(
                        response.getWriter(),
                        salesOrderService.getAllSalesOrders());

            } else {

                SalesOrderStatus orderStatus;

                try {
                    orderStatus = SalesOrderStatus.valueOf(
                            status.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    sendError(
                            response,
                            HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid sales order status");
                    return;
                }

                objectMapper.writeValue(
                        response.getWriter(),
                        salesOrderService.getSalesOrdersByStatus(orderStatus.name()));
            }

        } catch (NumberFormatException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid sales order ID");

        } catch (Exception e) {
            e.printStackTrace();
            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }
    }

    // =========================================================
    // POST - CREATE SALES ORDER
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        setJsonResponse(response);

        try {
            JsonNode root = objectMapper.readTree(request.getInputStream());

            SalesOrder order = new SalesOrder();

            Customer customer = getCustomer(root);
            order.setCustomer(customer);
            order.setOrderDate(
                    getOrderDate(root));
            order.setExpectedDeliveryDate(
                    getExpectedDeliveryDate(root));
            order.setStatus(SalesOrderStatus.valueOf("CREATED"));
            order.setTaxRate(getTaxRate(root));

            List<SalesOrderItem> items = parseItems(root);

            salesOrderService.createSalesOrder(order, items);

            response.setStatus(HttpServletResponse.SC_CREATED);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Sales order created successfully");
            result.put("salesOrderId", order.getId());
            result.put("subtotal", order.getSubtotal());
            result.put("taxRate", order.getTaxRate());
            result.put("taxAmount", order.getTaxAmount());
            result.put("totalAmount", order.getTotalAmount());

            objectMapper.writeValue(response.getWriter(), result);

        } catch (Exception e) {
            e.printStackTrace();
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());
        }
    }

    // =========================================================
    // PUT - UPDATE SALES ORDER
    // PUT /api/sales-orders/{id}
    // =========================================================

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        setJsonResponse(response);

        try {
            String path = request.getPathInfo();

            if (path == null || path.equals("/") ||
                    path.split("/").length != 2) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Sales order ID is required");
                return;
            }

            int salesOrderId = Integer.parseInt(path.split("/")[1]);

            SalesOrder existingOrder = salesOrderService.getSalesOrderById(salesOrderId);

            if (existingOrder == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Sales order not found");
                return;
            }

            if (!"CREATED".equalsIgnoreCase(existingOrder.getStatus().name())) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Only CREATED sales orders can be edited");
                return;
            }

            JsonNode root = objectMapper.readTree(request.getInputStream());

            SalesOrder updatedOrder = new SalesOrder();

            updatedOrder.setId(salesOrderId);

            updatedOrder.setCustomer(
                    getCustomer(root));

            updatedOrder.setOrderDate(
                    getOrderDate(root));

            updatedOrder.setExpectedDeliveryDate(
                    getExpectedDeliveryDate(root));

            updatedOrder.setStatus(
                    existingOrder.getStatus());

            updatedOrder.setTaxRate(
                    getTaxRate(root));

            List<SalesOrderItem> updatedItems = parseItems(root);

            salesOrderService.updateSalesOrder(
                    salesOrderId,
                    updatedOrder,
                    updatedItems);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Sales order updated successfully");
            result.put("salesOrderId", salesOrderId);
            result.put("subtotal", updatedOrder.getSubtotal());
            result.put("taxRate", updatedOrder.getTaxRate());
            result.put("taxAmount", updatedOrder.getTaxAmount());
            result.put("totalAmount", updatedOrder.getTotalAmount());

            objectMapper.writeValue(response.getWriter(), result);

        } catch (NumberFormatException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid sales order ID");

        } catch (Exception e) {
            e.printStackTrace();
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());
        }
    }

    // =========================================================
    // CUSTOMER PARSING
    // =========================================================

    private Customer getCustomer(JsonNode root) {

        JsonNode customerNode = root.get("customerId");

        if (customerNode == null || customerNode.isNull() ||
                !customerNode.canConvertToInt()) {

            throw new RuntimeException("Customer is required");
        }

        int customerId = customerNode.asInt();

        Customer customer = customerService.getCustomerById(customerId);

        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }

        return customer;
    }

    // =========================================================
    // TAX RATE PARSING
    // =========================================================

    private BigDecimal getTaxRate(JsonNode root) {

        JsonNode taxNode = root.get("taxRate");

        if (taxNode == null || taxNode.isNull()) {
            return BigDecimal.ZERO;
        }

        if (!taxNode.isNumber()) {
            throw new RuntimeException("Tax rate must be a number");
        }

        BigDecimal taxRate = taxNode.decimalValue();

        if (taxRate.compareTo(BigDecimal.ZERO) < 0 ||
                taxRate.compareTo(new BigDecimal("100")) > 0) {

            throw new RuntimeException(
                    "Tax rate must be between 0 and 100");
        }

        return taxRate;
    }

    private LocalDateTime getOrderDate(
            JsonNode root) {

        JsonNode dateNode = root.get("orderDate");

        if (dateNode == null ||
                dateNode.isNull() ||
                !dateNode.isTextual()) {
            throw new RuntimeException(
                    "Order date is required");
        }

        try {

            LocalDate date = LocalDate.parse(
                    dateNode.asText());

            return date.atStartOfDay();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Order date must be in YYYY-MM-DD format");
        }
    }

    private LocalDate getExpectedDeliveryDate(
            JsonNode root) {

        JsonNode dateNode = root.get("expectedDeliveryDate");

        if (dateNode == null ||
                dateNode.isNull() ||
                !dateNode.isTextual()) {
            throw new RuntimeException(
                    "Expected delivery date is required");
        }

        try {

            return LocalDate.parse(
                    dateNode.asText());

        } catch (Exception e) {

            throw new RuntimeException(
                    "Expected delivery date must be in YYYY-MM-DD format");
        }
    }

    // =========================================================
    // ITEM PARSING
    // =========================================================

    private List<SalesOrderItem> parseItems(JsonNode root) {

        JsonNode itemsNode = root.get("items");

        if (itemsNode == null || !itemsNode.isArray() ||
                itemsNode.isEmpty()) {

            throw new RuntimeException(
                    "Sales Order must contain at least one item");
        }

        List<SalesOrderItem> items = new ArrayList<>();

        for (JsonNode itemNode : itemsNode) {

            JsonNode itemIdNode = itemNode.get("itemId");
            JsonNode quantityNode = itemNode.get("quantity");
            JsonNode priceNode = itemNode.get("sellingPrice");

            if (itemIdNode == null || itemIdNode.isNull() ||
                    !itemIdNode.canConvertToInt()) {

                throw new RuntimeException("Valid itemId is required");
            }

            if (priceNode == null || priceNode.isNull() ||
                    !priceNode.isNumber()) {

                throw new RuntimeException(
                        "Selling price is required and must be a number");
            }

            int itemId = itemIdNode.asInt();

            Item item = itemService.getItemById(itemId);

            if (item == null) {
                throw new RuntimeException("Item not found: " + itemId);
            }

            int quantity;

            if ("SERVICE".equalsIgnoreCase(item.getItemType())) {
                quantity = 1;

            } else {
                if (quantityNode == null || quantityNode.isNull() ||
                        !quantityNode.canConvertToInt()) {

                    throw new RuntimeException(
                            "Quantity is required for goods");
                }

                quantity = quantityNode.asInt();

                if (quantity <= 0) {
                    throw new RuntimeException(
                            "Goods quantity must be greater than zero");
                }
            }

            BigDecimal sellingPrice = priceNode.decimalValue();

            if (sellingPrice.signum() < 0) {
                throw new RuntimeException(
                        "Selling price cannot be negative");
            }

            SalesOrderItem orderItem = new SalesOrderItem();
            orderItem.setItem(item);
            orderItem.setQuantity(quantity);
            orderItem.setSellingPrice(sellingPrice);

            items.add(orderItem);
        }

        return items;
    }

    // =========================================================
    // JSON RESPONSE HELPERS
    // =========================================================

    private void setJsonResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

        response.setStatus(status);

        Map<String, String> error = new HashMap<>();
        error.put("error", message != null ? message : "Unknown error");

        objectMapper.writeValue(response.getWriter(), error);
    }
}