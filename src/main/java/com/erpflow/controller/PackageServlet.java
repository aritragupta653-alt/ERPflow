
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

    private final PackageService packageService =
            new PackageService();

    private final SalesOrderService salesOrderService =
            new SalesOrderService();

    private final ItemService itemService =
            new ItemService();

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule());


    // =====================================================
    // GET
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        setJsonResponse(response);

        try {

            String path = request.getPathInfo();

            String salesOrderIdParam =
                    request.getParameter("salesOrderId");


            // GET /api/packages?salesOrderId=1
            if (salesOrderIdParam != null &&
                    !salesOrderIdParam.isBlank()) {

                int salesOrderId =
                        Integer.parseInt(salesOrderIdParam);

                SalesOrder salesOrder =
                        salesOrderService.getSalesOrderById(
                                salesOrderId
                        );

                if (salesOrder == null) {
                    sendError(
                            response,
                            404,
                            "Sales Order not found"
                    );
                    return;
                }

                List<Package> packages =
                        packageService.getPackagesBySalesOrder(
                                salesOrderId
                        );

                // NEW: Include each package's items.
                List<ObjectNode> result =
                        packagesWithItems(packages);

                objectMapper.writeValue(
                        response.getWriter(),
                        result
                );

                return;
            }


            // GET /api/packages/{id}
            if (path != null &&
                    !path.equals("/") &&
                    !path.isBlank()) {

                int packageId =
                        Integer.parseInt(path.substring(1));

                Package pkg =
                        packageService.getPackageById(packageId);

                if (pkg == null) {
                    sendError(
                            response,
                            404,
                            "Package not found"
                    );
                    return;
                }

                List<PackageItem> items =
                        packageService.getPackageItems(pkg);

                objectMapper.writeValue(
                        response.getWriter(),
                        Map.of(
                                "package", pkg,
                                "items", items
                        )
                );

                return;
            }


            // GET /api/packages
            List<Package> packages =
                    packageService.getAllPackages();

            // NEW: Include items in the general package list too.
            List<ObjectNode> result =
                    packagesWithItems(packages);

            objectMapper.writeValue(
                    response.getWriter(),
                    result
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    400,
                    "Invalid ID"
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendError(
                    response,
                    500,
                    e.getMessage()
            );
        }
    }


    // =====================================================
    // HELPER: PACKAGES WITH ITEMS
    // =====================================================

    private List<ObjectNode> packagesWithItems(
            List<Package> packages) {

        List<ObjectNode> result =
                new ArrayList<>();

        if (packages == null) {
            return result;
        }

        for (Package pkg : packages) {

            // Serialize the package's existing properties.
            ObjectNode packageNode =
                    objectMapper.valueToTree(pkg);

            // Fetch all items belonging to this package.
            List<PackageItem> packageItems =
                    packageService.getPackageItems(pkg);

            // Serialize the items, including salesOrderItemId.
            ArrayNode itemsNode =
                    objectMapper.valueToTree(packageItems);

            // Attach items without changing the package's
            // existing JSON properties.
            packageNode.set("items", itemsNode);

            result.add(packageNode);
        }

        return result;
    }


    // =====================================================
    // POST
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        setJsonResponse(response);

        try {

            JsonNode root =
                    objectMapper.readTree(
                            request.getInputStream()
                    );


            // SALES ORDER
            if (!root.has("salesOrderId") ||
                    !root.get("salesOrderId").canConvertToInt()) {

                sendError(
                        response,
                        400,
                        "A valid salesOrderId is required"
                );
                return;
            }

            int salesOrderId =
                    root.get("salesOrderId").asInt();

            SalesOrder salesOrder =
                    salesOrderService.getSalesOrderById(
                            salesOrderId
                    );

            if (salesOrder == null) {
                sendError(
                        response,
                        404,
                        "Sales Order not found"
                );
                return;
            }


            // WEIGHT
            if (!root.has("weight") ||
                    !root.get("weight").isNumber()) {

                sendError(
                        response,
                        400,
                        "A valid weight is required"
                );
                return;
            }

            double weight =
                    root.get("weight").asDouble();


            // DIMENSIONS
            if (!root.has("length") ||
                    !root.has("width") ||
                    !root.has("height") ||
                    !root.get("length").isNumber() ||
                    !root.get("width").isNumber() ||
                    !root.get("height").isNumber()) {

                sendError(
                        response,
                        400,
                        "Valid length, width and height are required"
                );
                return;
            }

            double length =
                    root.get("length").asDouble();

            double width =
                    root.get("width").asDouble();

            double height =
                    root.get("height").asDouble();


            // ITEMS
            if (!root.has("items") ||
                    !root.get("items").isArray() ||
                    root.get("items").isEmpty()) {

                sendError(
                        response,
                        400,
                        "Package must contain at least one item"
                );
                return;
            }

            List<PackageItem> packageItems =
                    new ArrayList<>();

            for (JsonNode itemNode : root.get("items")) {

                if (!itemNode.has("itemId") ||
                        !itemNode.get("itemId").canConvertToInt() ||
                        !itemNode.has("salesOrderItemId") ||
                        !itemNode.get("salesOrderItemId").canConvertToInt() ||
                        !itemNode.has("quantity") ||
                        !itemNode.get("quantity").canConvertToInt()) {

                    sendError(
                            response,
                            400,
                            "Each package item requires valid itemId, salesOrderItemId and quantity"
                    );
                    return;
                }

                int itemId =
                        itemNode.get("itemId").asInt();

                int salesOrderItemIdForLine =
                        itemNode.get("salesOrderItemId").asInt();

                int quantity =
                        itemNode.get("quantity").asInt();


                if (quantity <= 0) {
                    sendError(
                            response,
                            400,
                            "Package quantity must be greater than zero"
                    );
                    return;
                }


                Item item =
                        itemService.getItemById(itemId);

                if (item == null) {
                    sendError(
                            response,
                            404,
                            "Item not found: " + itemId
                    );
                    return;
                }


                PackageItem packageItem =
                        new PackageItem();

                packageItem.setItem(item);
                packageItem.setQuantity(quantity);

                packageItem.setSalesOrderItemId(
                        salesOrderItemIdForLine
                );

                packageItems.add(packageItem);
            }


            // CREATE PACKAGE
            Package pkg =
                    packageService.createPackage(
                            salesOrder,
                            packageItems,
                            weight,
                            length,
                            width,
                            height
                    );


            // RESPONSE
            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    Map.of(
                            "message",
                            "Package created successfully",
                            "packageId",
                            pkg.getId(),
                            "packageNumber",
                            pkg.getPackageNumber(),
                            "status",
                            pkg.getStatus()
                    )
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    400,
                    "Invalid numeric value"
            );

        } catch (RuntimeException e) {

            e.printStackTrace();

            sendError(
                    response,
                    400,
                    e.getMessage()
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendError(
                    response,
                    500,
                    "Failed to create package"
            );
        }
    }


    // =====================================================
    // JSON RESPONSE
    // =====================================================

    private void setJsonResponse(
            HttpServletResponse response) {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }


    // =====================================================
    // ERROR
    // =====================================================

    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        objectMapper.writeValue(
                response.getWriter(),
                Map.of(
                        "error",
                        message == null
                                ? "Unknown error"
                                : message
                )
        );
    }
}