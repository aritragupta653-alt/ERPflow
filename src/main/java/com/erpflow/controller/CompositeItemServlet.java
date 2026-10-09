
package com.erpflow.controller;

import com.erpflow.dao.ItemDAO;
import com.erpflow.model.Item;
import com.erpflow.model.ItemComponent;
import com.erpflow.service.AssemblyService;
import com.erpflow.service.CompositeItemService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/api/composite-items/*")
public class CompositeItemServlet extends HttpServlet {

    private final CompositeItemService compositeItemService =
            new CompositeItemService();

    private final AssemblyService assemblyService =
            new AssemblyService();

    private final ItemDAO itemDAO = new ItemDAO();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    // =========================================================
    // GET
    //
    // GET /api/composite-items
    // GET /api/composite-items/components
    // GET /api/composite-items/{id}/components
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);

        try {
            String path = normalizePath(request.getPathInfo());

            // List active composite items.
            if (path.isEmpty()) {

                List<Item> composites = itemDAO.findAll("ACTIVE")
                        .stream()
                        .filter(item ->
                                "COMPOSITE".equalsIgnoreCase(
                                        item.getItemType()
                                ))
                        .collect(Collectors.toList());

                objectMapper.writeValue(response.getWriter(), composites);
                return;
            }

            // List eligible GOODS and SERVICE components.
            if ("/components".equals(path)) {

                List<ItemComponent> components =
                        compositeItemService.getEligibleComponents();

                objectMapper.writeValue(response.getWriter(), components);
                return;
            }

            // Get component details for a specific composite.
            String[] parts = path.substring(1).split("/");

            if (parts.length == 2
                    && "components".equals(parts[1])) {

                int compositeItemId = parsePositiveId(parts[0]);

                List<ItemComponent> components =
                        compositeItemService.getComponents(
                                compositeItemId
                        );

                objectMapper.writeValue(response.getWriter(), components);
                return;
            }

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Composite item endpoint not found"
            );

        } catch (IllegalArgumentException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (Exception e) {
            getServletContext().log(
                    "Composite item GET request failed", e
            );

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to retrieve composite item data"
            );
        }
    }

    // =========================================================
    // POST
    //
    // POST /api/composite-items
    // POST /api/composite-items/assembly
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);

        try {
            String path = normalizePath(request.getPathInfo());

            JsonNode body = objectMapper.readTree(request.getInputStream());

            if (body == null || !body.isObject()) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "A valid JSON request body is required"
                );
                return;
            }

            // Execute assembly.
            if ("/assembly".equals(path)) {

                int compositeItemId = requiredPositiveInt(
                        body,
                        "compositeItemId"
                );

                int productionQuantity = requiredPositiveInt(
                        body,
                        "productionQuantity"
                );

                assemblyService.assemble(
                        compositeItemId,
                        productionQuantity
                );

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("success", true);
                result.put("message", "Assembly completed successfully");
                result.put("compositeItemId", compositeItemId);
                result.put("productionQuantity", productionQuantity);

                response.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(response.getWriter(), result);
                return;
            }

            // Create a composite item.
            if (path.isEmpty()) {

                JsonNode itemNode = body.get("item");
                JsonNode componentsNode = body.get("components");

                if (itemNode == null || !itemNode.isObject()) {
                    sendError(
                            response,
                            HttpServletResponse.SC_BAD_REQUEST,
                            "The item object is required"
                    );
                    return;
                }

                if (componentsNode == null
                        || !componentsNode.isArray()) {

                    sendError(
                            response,
                            HttpServletResponse.SC_BAD_REQUEST,
                            "The components array is required"
                    );
                    return;
                }

                Item composite = objectMapper.treeToValue(
                        itemNode,
                        Item.class
                );

                List<ItemComponent> components =
                        objectMapper.readerForListOf(ItemComponent.class)
                                .readValue(componentsNode);

                Item created = compositeItemService.createCompositeItem(
                        composite,
                        components
                );

                response.setStatus(
                        HttpServletResponse.SC_CREATED
                );

                objectMapper.writeValue(response.getWriter(), created);
                return;
            }

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Composite item endpoint not found"
            );

        } catch (IllegalArgumentException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (IllegalStateException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_CONFLICT,
                    e.getMessage()
            );

        } catch (Exception e) {
            getServletContext().log(
                    "Composite item POST request failed", e
            );

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to process composite item request"
            );
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void setJsonResponse(HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
    }

    private String normalizePath(String pathInfo) {
        if (pathInfo == null
                || pathInfo.isBlank()
                || "/".equals(pathInfo)) {
            return "";
        }

        return pathInfo.endsWith("/")
                ? pathInfo.substring(0, pathInfo.length() - 1)
                : pathInfo;
    }

    private int parsePositiveId(String value) {

        try {
            int id = Integer.parseInt(value);

            if (id <= 0) {
                throw new IllegalArgumentException(
                        "ID must be greater than zero"
                );
            }

            return id;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID");
        }
    }

    private int requiredPositiveInt(JsonNode body, String field) {

        JsonNode value = body.get(field);

        if (value == null || !value.canConvertToInt()) {
            throw new IllegalArgumentException(
                    field + " must be an integer"
            );
        }

        int number = value.asInt();

        if (number <= 0) {
            throw new IllegalArgumentException(
                    field + " must be greater than zero"
            );
        }

        return number;
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

        response.setStatus(status);

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("message", message);

        objectMapper.writeValue(response.getWriter(), error);
    }
}
