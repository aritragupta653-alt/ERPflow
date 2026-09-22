
package com.erpflow.controller;

import com.erpflow.model.Item;
import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.model.SalesOrder;
import com.erpflow.service.ItemService;
import com.erpflow.service.PackageService;
import com.erpflow.service.SalesOrderService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/api/packages/*")
public class PackageServlet extends HttpServlet {

    private final PackageService packageService = new PackageService();
    private final SalesOrderService salesOrderService = new SalesOrderService();
    private final ItemService itemService = new ItemService();

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    // =====================================================
    // GET
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        setJsonResponse(response);

        try {
            String path = request.getPathInfo();
            String salesOrderIdParam = request.getParameter("salesOrderId");

            if (salesOrderIdParam != null && !salesOrderIdParam.isBlank()) {
                int salesOrderId = parsePositiveId(salesOrderIdParam);

                SalesOrder salesOrder =
                        salesOrderService.getSalesOrderById(salesOrderId);

                if (salesOrder == null) {
                    sendError(response, 404, "Sales Order not found");
                    return;
                }

                objectMapper.writeValue(
                        response.getWriter(),
                        packagesWithItems(
                                packageService.getPackagesBySalesOrder(salesOrderId)
                        )
                );
                return;
            }

            if (hasPathId(path)) {
                int packageId = parsePathId(path);
                Package pkg = packageService.getPackageById(packageId);

                if (pkg == null) {
                    sendError(response, 404, "Package not found");
                    return;
                }

                objectMapper.writeValue(
                        response.getWriter(),
                        Map.of(
                                "package", pkg,
                                "items", packageService.getPackageItems(pkg)
                        )
                );
                return;
            }

            objectMapper.writeValue(
                    response.getWriter(),
                    packagesWithItems(packageService.getAllPackages())
            );

        } catch (NumberFormatException e) {
            sendError(response, 400, "Invalid ID");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, 500, safeMessage(e, "Failed to retrieve packages"));
        }
    }

    // =====================================================
    // POST - CREATE PACKAGE
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        setJsonResponse(response);

        try {
            JsonNode root = objectMapper.readTree(request.getInputStream());

            if (root == null || !root.isObject()) {
                sendError(response, 400, "A valid JSON object is required");
                return;
            }

            if (!isPositiveInteger(root.get("salesOrderId"))) {
                sendError(response, 400, "A valid salesOrderId is required");
                return;
            }

            int salesOrderId = root.get("salesOrderId").asInt();
            SalesOrder salesOrder =
                    salesOrderService.getSalesOrderById(salesOrderId);

            if (salesOrder == null) {
                sendError(response, 404, "Sales Order not found");
                return;
            }

            if (!hasValidDimensions(root)) {
                sendError(
                        response,
                        400,
                        "Valid positive weight, length, width and height are required"
                );
                return;
            }

            List<PackageItem> packageItems = parseItems(root.get("items"));

            Package pkg = packageService.createPackage(
                    salesOrder,
                    packageItems,
                    root.get("weight").asDouble(),
                    root.get("length").asDouble(),
                    root.get("width").asDouble(),
                    root.get("height").asDouble()
            );

            response.setStatus(HttpServletResponse.SC_CREATED);

            objectMapper.writeValue(
                    response.getWriter(),
                    Map.of(
                            "message", "Package created successfully",
                            "packageId", pkg.getId(),
                            "packageNumber", pkg.getPackageNumber(),
                            "status", pkg.getStatus()
                    )
            );

        } catch (IllegalArgumentException e) {
            sendError(response, 400, e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            sendError(response, 400, safeMessage(e, "Invalid package data"));
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, 500, "Failed to create package");
        }
    }

    // =====================================================
    // PUT - EDIT PACKAGE
    // PUT /api/packages/{id}
    // =====================================================

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        setJsonResponse(response);

        try {
            String path = request.getPathInfo();

            if (!hasPathId(path)) {
                sendError(response, 400, "Package ID is required in the URL");
                return;
            }

            int packageId = parsePathId(path);

            Package existingPackage = packageService.getPackageById(packageId);

            if (existingPackage == null) {
                sendError(response, 404, "Package not found");
                return;
            }

            JsonNode root = objectMapper.readTree(request.getInputStream());

            if (root == null || !root.isObject()) {
                sendError(response, 400, "A valid JSON object is required");
                return;
            }

            // Editing must not change the Sales Order associated with a package.
            if (root.has("salesOrderId")) {
                JsonNode orderIdNode = root.get("salesOrderId");

                if (!isPositiveInteger(orderIdNode)) {
                    sendError(response, 400, "Invalid salesOrderId");
                    return;
                }

                int submittedOrderId = orderIdNode.asInt();
                int existingOrderId = existingPackage.getSalesOrder() == null
                        ? -1
                        : existingPackage.getSalesOrder().getId();

                if (submittedOrderId != existingOrderId) {
                    sendError(
                            response,
                            400,
                            "A package cannot be reassigned to another Sales Order"
                    );
                    return;
                }
            }

            if (!hasValidDimensions(root)) {
                sendError(
                        response,
                        400,
                        "Valid positive weight, length, width and height are required"
                );
                return;
            }

            List<PackageItem> updatedItems = parseItems(root.get("items"));

            Package updatedPackage = packageService.editPackage(
                    packageId,
                    updatedItems,
                    root.get("weight").asDouble(),
                    root.get("length").asDouble(),
                    root.get("width").asDouble(),
                    root.get("height").asDouble()
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    Map.of(
                            "message", "Package updated successfully",
                            "packageId", updatedPackage.getId(),
                            "packageNumber", updatedPackage.getPackageNumber(),
                            "status", updatedPackage.getStatus()
                    )
            );

        } catch (IllegalArgumentException e) {
            sendError(response, 400, e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            sendError(response, 400, safeMessage(e, "Invalid package data"));
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, 500, "Failed to update package");
        }
    }

    // =====================================================
    // PARSE PACKAGE ITEMS
    // =====================================================

    private List<PackageItem> parseItems(JsonNode itemsNode) {
        if (itemsNode == null || !itemsNode.isArray() || itemsNode.isEmpty()) {
            throw new IllegalArgumentException(
                    "Package must contain at least one item"
            );
        }

        List<PackageItem> packageItems = new ArrayList<>();

        for (JsonNode itemNode : itemsNode) {
            if (itemNode == null || !itemNode.isObject()) {
                throw new IllegalArgumentException(
                        "Each package item must be a JSON object"
                );
            }

            if (!isPositiveInteger(itemNode.get("itemId"))
                    || !isPositiveInteger(itemNode.get("salesOrderItemId"))
                    || !isPositiveInteger(itemNode.get("quantity"))) {
                throw new IllegalArgumentException(
                        "Each package item requires positive integer itemId, "
                                + "salesOrderItemId and quantity"
                );
            }

            int itemId = itemNode.get("itemId").asInt();
            int salesOrderItemId = itemNode.get("salesOrderItemId").asInt();
            int quantity = itemNode.get("quantity").asInt();

            Item item = itemService.getItemById(itemId);

            if (item == null) {
                throw new IllegalArgumentException(
                        "Item not found: " + itemId
                );
            }

            PackageItem packageItem = new PackageItem();
            packageItem.setItem(item);
            packageItem.setSalesOrderItemId(salesOrderItemId);
            packageItem.setQuantity(quantity);

            packageItems.add(packageItem);
        }

        return packageItems;
    }

    // =====================================================
    // PACKAGES WITH ITEMS
    // =====================================================

    private List<ObjectNode> packagesWithItems(List<Package> packages) {
        List<ObjectNode> result = new ArrayList<>();

        if (packages == null) {
            return result;
        }

        for (Package pkg : packages) {
            ObjectNode packageNode = objectMapper.valueToTree(pkg);

            ArrayNode itemsNode =
                    objectMapper.valueToTree(packageService.getPackageItems(pkg));

            packageNode.set("items", itemsNode);
            result.add(packageNode);
        }

        return result;
    }

    // =====================================================
    // VALIDATION HELPERS
    // =====================================================

    private boolean hasValidDimensions(JsonNode root) {
        return isPositiveNumber(root.get("weight"))
                && isPositiveNumber(root.get("length"))
                && isPositiveNumber(root.get("width"))
                && isPositiveNumber(root.get("height"));
    }

    private boolean isPositiveNumber(JsonNode node) {
        return node != null
                && node.isNumber()
                && Double.isFinite(node.asDouble())
                && node.asDouble() > 0;
    }

    private boolean isPositiveInteger(JsonNode node) {
        return node != null
                && node.isIntegralNumber()
                && node.canConvertToInt()
                && node.asInt() > 0;
    }

    private boolean hasPathId(String path) {
        return path != null
                && !path.isBlank()
                && !path.equals("/")
                && path.matches("/\\d+");
    }

    private int parsePathId(String path) {
        if (!hasPathId(path)) {
            throw new NumberFormatException("Invalid package ID");
        }

        return parsePositiveId(path.substring(1));
    }

    private int parsePositiveId(String value) {
        int id = Integer.parseInt(value);

        if (id <= 0) {
            throw new NumberFormatException("ID must be positive");
        }

        return id;
    }

    private String safeMessage(Exception e, String fallback) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? fallback : message;
    }

    // =====================================================
    // RESPONSE HELPERS
    // =====================================================

    private void setJsonResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

        response.setStatus(status);

        ObjectNode error = objectMapper.createObjectNode();
        error.put(
                "error",
                message == null || message.isBlank()
                        ? "Unknown error"
                        : message
        );

        objectMapper.writeValue(response.getWriter(), error);
    }
}