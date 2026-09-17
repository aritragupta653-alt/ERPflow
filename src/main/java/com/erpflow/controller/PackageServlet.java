package com.erpflow.controller;

import com.erpflow.model.Item;
import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.service.ItemService;
import com.erpflow.service.PackageService;
import com.erpflow.service.SalesOrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/packages")
public class PackageServlet extends HttpServlet {

        private final PackageService packageService = new PackageService();

        private final SalesOrderService salesOrderService = new SalesOrderService();

        private final ItemService itemService = new ItemService();

        private final ObjectMapper objectMapper = new ObjectMapper()
                        .registerModule(
                                        new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        // =========================
        // GET
        // =========================

        @Override
        protected void doGet(
                        HttpServletRequest request,
                        HttpServletResponse response)
                        throws ServletException, IOException {

                setJsonResponse(response);

                try {

                        String idParameter = request.getParameter("id");

                        String salesOrderIdParameter = request.getParameter("salesOrderId");

                        // GET /api/packages?id=1
                        if (idParameter != null &&
                                        !idParameter.isBlank()) {

                                int packageId = Integer.parseInt(idParameter);

                                Package packageEntity = packageService.getPackageById(
                                                packageId);

                                if (packageEntity == null) {

                                        sendError(
                                                        response,
                                                        HttpServletResponse.SC_NOT_FOUND,
                                                        "Package not found");

                                        return;
                                }

                                List<PackageItem> packageItems = packageService.getPackageItems(
                                                packageEntity);

                                Map<String, Object> result = new HashMap<>();

                                result.put(
                                                "package",
                                                packageEntity);

                                result.put(
                                                "items",
                                                packageItems);

                                objectMapper.writeValue(
                                                response.getWriter(),
                                                result);

                                return;
                        }

                        // GET /api/packages?salesOrderId=1
                        if (salesOrderIdParameter != null &&
                                        !salesOrderIdParameter.isBlank()) {

                                int salesOrderId = Integer.parseInt(
                                                salesOrderIdParameter);

                                SalesOrder salesOrder = salesOrderService
                                                .getSalesOrderById(
                                                                salesOrderId);

                                if (salesOrder == null) {

                                        sendError(
                                                        response,
                                                        HttpServletResponse.SC_NOT_FOUND,
                                                        "Sales Order not found");

                                        return;
                                }

                                List<Package> packages = packageService
                                                .getPackagesBySalesOrder(
                                                                salesOrder);

                                objectMapper.writeValue(
                                                response.getWriter(),
                                                packages);

                                return;
                        }

                        // GET /api/packages
                        List<Package> packages = packageService.getAllPackages();

                        objectMapper.writeValue(
                                        response.getWriter(),
                                        packages);

                } catch (NumberFormatException e) {

                        sendError(
                                        response,
                                        HttpServletResponse.SC_BAD_REQUEST,
                                        "Invalid ID");

                } catch (Exception e) {

                        sendError(
                                        response,
                                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                        e.getMessage());
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

                setJsonResponse(response);

                try {

                        JsonNode root = objectMapper.readTree(
                                        request.getInputStream());

                        // Sales Order ID
                        if (!root.has("salesOrderId")) {

                                sendError(
                                                response,
                                                HttpServletResponse.SC_BAD_REQUEST,
                                                "salesOrderId is required");

                                return;
                        }

                        int salesOrderId = root.get("salesOrderId")
                                        .asInt();

                        // Find Sales Order
                        SalesOrder salesOrder = salesOrderService
                                        .getSalesOrderById(
                                                        salesOrderId);

                        if (salesOrder == null) {

                                sendError(
                                                response,
                                                HttpServletResponse.SC_NOT_FOUND,
                                                "Sales Order not found");

                                return;
                        }

                        // Items
                        JsonNode itemsNode = root.get("items");

                        if (itemsNode == null ||
                                        !itemsNode.isArray() ||
                                        itemsNode.isEmpty()) {

                                sendError(
                                                response,
                                                HttpServletResponse.SC_BAD_REQUEST,
                                                "Package must contain at least one item");

                                return;
                        }

                        List<SalesOrderItem> salesOrderItems = salesOrderService
                                        .getSalesOrderItems(
                                                        salesOrder);

                        List<PackageItem> packageItems = new ArrayList<>();

                        // Build Package Items
                        for (JsonNode itemNode : itemsNode) {

                                int itemId = itemNode
                                                .get("itemId")
                                                .asInt();

                                int quantity = itemNode
                                                .get("quantity")
                                                .asInt();

                                if (quantity <= 0) {

                                        throw new RuntimeException(
                                                        "Package quantity must be greater than zero");
                                }

                                // Check item exists
                                Item item = itemService
                                                .getItemById(itemId);

                                if (item == null) {

                                        throw new RuntimeException(
                                                        "Item not found: " + itemId);
                                }

                                // Check item belongs to Sales Order
                                SalesOrderItem matchingOrderItem = null;

                                for (SalesOrderItem orderItem : salesOrderItems) {

                                        if (orderItem.getItem().getId() == itemId) {

                                                matchingOrderItem = orderItem;

                                                break;
                                        }
                                }

                                if (matchingOrderItem == null) {

                                        throw new RuntimeException(
                                                        "Item does not belong to Sales Order");
                                }

                                // Create Package Item
                                PackageItem packageItem = new PackageItem();

                                packageItem.setItem(item);

                                packageItem.setQuantity(
                                                quantity);

                                packageItems.add(
                                                packageItem);
                        }

                        // Weight
                        if (!root.has("weight")) {

                                sendError(
                                                response,
                                                HttpServletResponse.SC_BAD_REQUEST,
                                                "weight is required");

                                return;
                        }

                        double weight = root.get("weight")
                                        .asDouble();

                        // Create Package
                        packageService.createPackage(
                                        salesOrder,
                                        packageItems,
                                        weight);

                        // Response
                        response.setStatus(
                                        HttpServletResponse.SC_CREATED);

                        Map<String, Object> result = new HashMap<>();

                        result.put(
                                        "message",
                                        "Package created successfully");

                        result.put(
                                        "packageId",
                                        packageItems.get(0)
                                                        .getPackageEntity());

                        // Better response using the newly
                        // created package ID
                        Package latestPackage = null;

                        List<Package> packages = packageService
                                        .getPackagesBySalesOrder(
                                                        salesOrder);

                        if (!packages.isEmpty()) {

                                latestPackage = packages.get(0);
                        }

                        if (latestPackage != null) {

                                result.put(
                                                "packageId",
                                                latestPackage.getId());

                                result.put(
                                                "status",
                                                latestPackage.getStatus());
                        }

                        objectMapper.writeValue(
                                        response.getWriter(),
                                        result);

                } catch (Exception e) {

                        sendError(
                                        response,
                                        HttpServletResponse.SC_BAD_REQUEST,
                                        e.getMessage());
                }
        }

        // =========================
        // HELPERS
        // =========================

        private void setJsonResponse(
                        HttpServletResponse response) {

                response.setContentType(
                                "application/json");

                response.setCharacterEncoding(
                                "UTF-8");
        }

        private void sendError(
                        HttpServletResponse response,
                        int status,
                        String message)
                        throws IOException {

                response.setStatus(status);

                Map<String, String> error = new HashMap<>();

                error.put(
                                "error",
                                message);

                objectMapper.writeValue(
                                response.getWriter(),
                                error);
        }
}