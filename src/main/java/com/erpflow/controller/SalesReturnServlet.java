
package com.erpflow.controller;

import com.erpflow.service.SalesReturnService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/sales-returns/*")
public class SalesReturnServlet extends HttpServlet {

    private final SalesReturnService service = new SalesReturnService();

    private final ObjectMapper mapper =
        new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        setJson(response);

        try {
            String path = request.getPathInfo();

            // GET /api/sales-returns/eligible-lines?salesOrderId=41
            if ("/eligible-lines".equals(path)) {
                String value = request.getParameter("salesOrderId");

                if (value == null || value.isBlank()) {
                    sendError(response, 400, "salesOrderId is required");
                    return;
                }

                int salesOrderId = parsePositiveId(value, "salesOrderId");

                mapper.writeValue(
                    response.getWriter(),
                    service.getEligibleLines(salesOrderId)
                );
                return;
            }

            // GET /api/sales-returns
            if (path == null || "/".equals(path) || path.isBlank()) {
                mapper.writeValue(response.getWriter(), service.getAllReturns());
                return;
            }

            // GET /api/sales-returns/{id}
            String[] parts = path.split("/");
            if (parts.length == 2 && !parts[1].isBlank()) {
                int id = parsePositiveId(parts[1], "salesReturnId");
                mapper.writeValue(response.getWriter(), service.getReturnById(id));
                return;
            }

            sendError(response, 404, "Sales return endpoint not found");

        } catch (NumberFormatException e) {
            sendError(response, 400, "Invalid numeric ID");
        } catch (IllegalArgumentException e) {
            sendError(response, 400, safeMessage(e));
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, 500, "Failed to retrieve sales return data");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        setJson(response);

        try {
            String path = request.getPathInfo();

            if (path != null && !"/".equals(path) && !path.isBlank()) {
                sendError(response, 404, "POST is supported only at /api/sales-returns");
                return;
            }

            Map<String, Object> body = mapper.readValue(
                request.getInputStream(),
                new TypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> created = service.createReturn(body);

            response.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(response.getWriter(), created);

        } catch (IllegalArgumentException e) {
            sendError(response, 400, safeMessage(e));
        } catch (IllegalStateException e) {
            sendError(response, 409, safeMessage(e));
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, 500, "Failed to create sales return");
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        setJson(response);

        try {
            String path = request.getPathInfo();

            // PUT /api/sales-returns/{id}/status
            if (path == null) {
                sendError(response, 400, "Sales return ID is required");
                return;
            }

            String[] parts = path.split("/");
            if (parts.length != 3 ||
                    parts[1].isBlank() ||
                    !"status".equalsIgnoreCase(parts[2])) {
                sendError(
                    response, 404,
                    "Expected PUT /api/sales-returns/{id}/status"
                );
                return;
            }

            int id = parsePositiveId(parts[1], "salesReturnId");

            JsonNode body = mapper.readTree(request.getInputStream());
            if (body == null || !body.isObject() ||
                    !body.hasNonNull("status") ||
                    body.get("status").asText().isBlank()) {
                sendError(response, 400, "status is required");
                return;
            }

            Map<String, Object> updated = service.updateStatus(
                id, body.get("status").asText()
            );

            mapper.writeValue(response.getWriter(), updated);

        } catch (NumberFormatException e) {
            sendError(response, 400, "Invalid sales return ID");
        } catch (IllegalArgumentException e) {
            sendError(response, 400, safeMessage(e));
        } catch (IllegalStateException e) {
            sendError(response, 409, safeMessage(e));
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, 500, "Failed to update sales return status");
        }
    }

    private int parsePositiveId(String value, String field) {
        int id = Integer.parseInt(value);

        if (id <= 0) {
            throw new IllegalArgumentException(field + " must be greater than zero");
        }

        return id;
    }

    private void setJson(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

        response.setStatus(status);
        mapper.writeValue(
            response.getWriter(),
            Map.of("error", message == null ? "Unknown error" : message)
        );
    }

    private String safeMessage(Exception e) {
        return e.getMessage() == null ? "Request failed" : e.getMessage();
    }
}
