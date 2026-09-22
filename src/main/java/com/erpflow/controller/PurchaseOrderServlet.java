
package com.erpflow.controller;

import com.erpflow.model.Item;
import com.erpflow.model.PurchaseOrder;
import com.erpflow.model.PurchaseOrderItem;
import com.erpflow.model.Supplier;
import com.erpflow.service.ItemService;
import com.erpflow.service.PurchaseOrderService;
import com.erpflow.service.SupplierService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@WebServlet("/api/purchase-orders/*")
public class PurchaseOrderServlet extends HttpServlet {

    private final PurchaseOrderService purchaseOrderService =
            new PurchaseOrderService();

    private final SupplierService supplierService =
            new SupplierService();

    private final ItemService itemService =
            new ItemService();

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        prepareResponse(response);

        try {
            String path = request.getPathInfo();

            if (path == null || path.equals("/")) {
                List<Map<String, Object>> result = new ArrayList<>();

                for (PurchaseOrder order : purchaseOrderService.getAllPurchaseOrders()) {
                    result.add(convertOrder(order));
                }

                objectMapper.writeValue(response.getWriter(), result);
                return;
            }

            int id = parseOrderId(path);

            PurchaseOrder order = purchaseOrderService.getPurchaseOrderById(id);

            if (order == null) {
                sendError(response, 404, "Purchase order not found");
                return;
            }

            Map<String, Object> result = convertOrder(order);
            List<Map<String, Object>> itemList = new ArrayList<>();

            for (PurchaseOrderItem line :
                    purchaseOrderService.getPurchaseOrderItems(order)) {

                Map<String, Object> lineMap = new HashMap<>();
                lineMap.put("id", line.getId());
                lineMap.put("quantity", line.getQuantity());
                lineMap.put("purchasePrice", line.getPurchasePrice());
                lineMap.put("recievedQuantity", line.getRecievedQuantity());
                lineMap.put("remainingQuantity", line.getRemainingQuantity());

                if (line.getItem() != null) {
                    Item item = line.getItem();

                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("id", item.getId());
                    itemMap.put("name", item.getName());
                    itemMap.put("sku", item.getSku());
                    itemMap.put("itemType", item.getItemType());
                    itemMap.put("trackInventory", item.isTrackInventory());

                    lineMap.put("item", itemMap);
                }

                itemList.add(lineMap);
            }

            result.put("items", itemList);
            objectMapper.writeValue(response.getWriter(), result);

        } catch (NumberFormatException e) {
            sendError(response, 400, "Invalid purchase order ID");
        } catch (RuntimeException e) {
            e.printStackTrace();
            sendError(response, 400, e.getMessage());
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        prepareResponse(response);

        try {
            String path = request.getPathInfo();

            // POST /api/purchase-orders/{id}/receive
            if (path != null && path.matches("/\\d+/receive")) {
                int id = Integer.parseInt(path.split("/")[1]);

                Map<String, Object> body = readBody(request);

                Object linesObject = body.get("items");

                if (linesObject == null) {
                    // Backwards-compatible behavior: receive all remaining goods.
                    purchaseOrderService.receivePurchaseOrder(id);
                } else {
                    List<Map<String, Object>> receiveItems =
                            objectMapper.convertValue(
                                    linesObject,
                                    new TypeReference<List<Map<String, Object>>>() {}
                            );

                    List<PurchaseOrderService.ReceiveLine> lines = new ArrayList<>();

                    for (Map<String, Object> data : receiveItems) {
                        Number lineId = number(data.get("purchaseOrderItemId"));
                        Number quantity = number(data.get("quantity"));

                        if (lineId == null || quantity == null) {
                            throw new RuntimeException(
                                    "Each receive line requires purchaseOrderItemId and quantity"
                            );
                        }

                        lines.add(new PurchaseOrderService.ReceiveLine(
                                lineId.intValue(),
                                quantity.intValue()
                        ));
                    }

                    purchaseOrderService.receivePurchaseOrder(id, lines);
                }

                Map<String, Object> result = new HashMap<>();
                result.put("message", "Purchase order receiving processed");
                result.put("purchaseOrderId", id);

                objectMapper.writeValue(response.getWriter(), result);
                return;
            }

            // POST /api/purchase-orders
            Map<String, Object> body = readBody(request);
            PurchaseOrder order = createOrderFromBody(body);

            List<PurchaseOrderItem> lines = createLinesFromBody(body);

            purchaseOrderService.createPurchaseOrder(order, lines);

            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getWriter(), convertOrder(order));

        } catch (NumberFormatException e) {
            sendError(response, 400, "Invalid ID or quantity");
        } catch (RuntimeException e) {
            e.printStackTrace();
            sendError(response, 400, e.getMessage());
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        prepareResponse(response);

        try {
            String path = request.getPathInfo();

            if (path == null || !path.matches("/\\d+")) {
                sendError(response, 400, "Use PUT /api/purchase-orders/{id}");
                return;
            }

            int id = parseOrderId(path);

            Map<String, Object> body = readBody(request);

            PurchaseOrder updatedOrder = createOrderFromBody(body);
            List<PurchaseOrderItem> updatedLines = createLinesFromBody(body);

            purchaseOrderService.editPurchaseOrder(id, updatedOrder, updatedLines);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Purchase order updated successfully");
            result.put("purchaseOrderId", id);

            objectMapper.writeValue(response.getWriter(), result);

        } catch (NumberFormatException e) {
            sendError(response, 400, "Invalid purchase order ID");
        } catch (RuntimeException e) {
            e.printStackTrace();
            sendError(response, 400, e.getMessage());
        }
    }

    private PurchaseOrder createOrderFromBody(Map<String, Object> body) {
        Number supplierNumber = number(body.get("supplierId"));

        if (supplierNumber == null) {
            throw new RuntimeException("Supplier ID is required");
        }

        Supplier supplier = supplierService.getSupplierById(supplierNumber.intValue());

        if (supplier == null) {
            throw new RuntimeException("Supplier not found");
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(supplier);
        order.setStatus("CREATED");
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

    private List<PurchaseOrderItem> createLinesFromBody(Map<String, Object> body) {
        Object itemsObject = body.get("items");

        if (itemsObject == null) {
            throw new RuntimeException("At least one item is required");
        }

        List<Map<String, Object>> itemData =
                objectMapper.convertValue(
                        itemsObject,
                        new TypeReference<List<Map<String, Object>>>() {}
                );

        if (itemData.isEmpty()) {
            throw new RuntimeException("At least one item is required");
        }

        List<PurchaseOrderItem> result = new ArrayList<>();

        for (Map<String, Object> data : itemData) {
            Number itemIdNumber = number(data.get("itemId"));

            if (itemIdNumber == null) {
                throw new RuntimeException("Item ID is required");
            }

            Item item = itemService.getItemById(itemIdNumber.intValue());

            if (item == null) {
                throw new RuntimeException("Item not found: " + itemIdNumber);
            }

            Number quantityNumber = number(data.get("quantity"));
            int quantity;

            if (item.isTrackInventory()) {
                if (quantityNumber == null || quantityNumber.intValue() <= 0) {
                    throw new RuntimeException(
                            "A positive quantity is required for inventory-tracked goods"
                    );
                }

                quantity = quantityNumber.intValue();
            } else {
                // quantity is NOT NULL in the current table; use a placeholder
                // for services, whose quantity is not used for inventory.
                quantity = 1;
            }

            Object priceObject = data.get("purchasePrice");
            BigDecimal price = priceObject == null
                    ? item.getPurchasePrice()
                    : new BigDecimal(priceObject.toString());

            if (price == null || price.signum() < 0) {
                throw new RuntimeException("Invalid purchase price");
            }

            PurchaseOrderItem line = new PurchaseOrderItem();
            line.setItem(item);
            line.setQuantity(quantity);
            line.setPurchasePrice(price);
            line.setReceivedQuantity(0);

            result.add(line);
        }

        return result;
    }

    private Map<String, Object> convertOrder(PurchaseOrder order) {
        Map<String, Object> result = new HashMap<>();

        result.put("id", order.getId());
        result.put("status", order.getStatus());
        result.put("orderDate", order.getOrderDate());

        if (order.getSupplier() != null) {
            Supplier supplier = order.getSupplier();

            Map<String, Object> supplierMap = new HashMap<>();
            supplierMap.put("id", supplier.getId());
            supplierMap.put("name", supplier.getName());
            supplierMap.put("contactPerson", supplier.getContactPerson());
            supplierMap.put("phone", supplier.getPhone());
            supplierMap.put("email", supplier.getEmail());
            supplierMap.put("address", supplier.getAddress());

            result.put("supplier", supplierMap);
        }

        return result;
    }

    private Map<String, Object> readBody(HttpServletRequest request) throws IOException {
        return objectMapper.readValue(
                request.getReader(),
                new TypeReference<Map<String, Object>>() {}
        );
    }

    private Number number(Object value) {
        return value instanceof Number ? (Number) value : null;
    }

    private int parseOrderId(String path) {
        String value = path.startsWith("/") ? path.substring(1) : path;
        return Integer.parseInt(value);
    }

    private void prepareResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        response.setStatus(status);

        Map<String, String> error = new HashMap<>();
        error.put("error", message == null ? "Unknown error" : message);

        objectMapper.writeValue(response.getWriter(), error);
    }
}