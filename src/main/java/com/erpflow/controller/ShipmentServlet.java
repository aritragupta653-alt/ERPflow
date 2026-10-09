package com.erpflow.controller;

import com.erpflow.model.Shipment;
import com.erpflow.model.enums.ShipmentStatus;
import com.erpflow.service.ShipmentService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet("/api/shipments/*")
public class ShipmentServlet extends HttpServlet {

    private final ShipmentService shipmentService =
            new ShipmentService();

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(
                            new JavaTimeModule()
                    );


    // =====================================================
    // GET
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setJsonHeaders(response);

        String pathInfo =
                request.getPathInfo();

        try {

            // ---------------------------------------------
            // GET /api/shipments
            // ---------------------------------------------

            if (pathInfo == null ||
                    pathInfo.equals("/")) {

                List<Shipment> shipments =
                        shipmentService.getAllShipments();

                objectMapper.writeValue(
                        response.getWriter(),
                        shipments
                );

                return;
            }

            // ---------------------------------------------
            // GET /api/shipments/{id}
            // ---------------------------------------------

            int id =
                    parseShipmentId(pathInfo);

            Shipment shipment =
                    shipmentService.getShipmentById(id);

            if (shipment == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Shipment not found"
                );

                return;
            }

            objectMapper.writeValue(
                    response.getWriter(),
                    shipment
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid shipment ID"
            );

        } catch (Exception e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );
        }
    }


    // =====================================================
    // PUT
    // =====================================================

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setJsonHeaders(response);

        String pathInfo =
                request.getPathInfo();

        try {

            if (pathInfo == null ||
                    pathInfo.equals("/")) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Shipment ID is required"
                );

                return;
            }

            String[] parts =
                    pathInfo.split("/");

            if (parts.length < 2 ||
                    parts[1].isBlank()) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid shipment endpoint"
                );

                return;
            }

            int id =
                    Integer.parseInt(parts[1]);

            Shipment existing =
                    shipmentService.getShipmentById(id);

            if (existing == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Shipment not found"
                );

                return;
            }

            // ---------------------------------------------
            // Delivered = completely locked
            // ---------------------------------------------

            if (existing.getStatus() ==
                    ShipmentStatus.DELIVERED) {

                sendError(
                        response,
                        HttpServletResponse.SC_CONFLICT,
                        "Delivered shipments cannot be edited or have their packages changed."
                );

                return;
            }

            // ---------------------------------------------
            // PUT /api/shipments/{id}/packages
            // ---------------------------------------------

            if (parts.length == 3 &&
                    "packages".equals(parts[2])) {

                PackageUpdateRequest packageRequest =
                        objectMapper.readValue(
                                request.getInputStream(),
                                PackageUpdateRequest.class
                        );

                if (packageRequest.getPackageIds()
                        == null) {

                    sendError(
                            response,
                            HttpServletResponse.SC_BAD_REQUEST,
                            "packageIds is required"
                    );

                    return;
                }

                shipmentService.updateShipmentPackages(
                        id,
                        packageRequest.getPackageIds()
                );

                Shipment updated =
                        shipmentService.getShipmentById(id);

                objectMapper.writeValue(
                        response.getWriter(),
                        updated
                );

                return;
            }

            // ---------------------------------------------
            // PUT /api/shipments/{id}
            // ---------------------------------------------

            if (parts.length != 2) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid shipment endpoint"
                );

                return;
            }

            Shipment updatedShipment =
                    objectMapper.readValue(
                            request.getInputStream(),
                            Shipment.class
                    );

            updatedShipment.setId(id);

            shipmentService.updateShipment(
                    updatedShipment
            );

            Shipment saved =
                    shipmentService.getShipmentById(id);

            objectMapper.writeValue(
                    response.getWriter(),
                    saved
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid shipment ID"
            );

        } catch (IllegalStateException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_CONFLICT,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
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


    // =====================================================
    // POST
    // =====================================================

    /*
     * POST /api/shipments/{id}/deliver
     *
     * Request:
     *
     * {
     *     "actualDeliveryDate": "2026-09-30"
     * }
     */

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setJsonHeaders(response);

        String pathInfo =
                request.getPathInfo();

        try {

            if (pathInfo == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid shipment endpoint"
                );

                return;
            }

            String[] parts =
                    pathInfo.split("/");

            if (parts.length != 3 ||
                    !"deliver".equals(parts[2])) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid shipment endpoint"
                );

                return;
            }

            int id =
                    Integer.parseInt(parts[1]);

            // ---------------------------------------------
            // Find shipment
            // ---------------------------------------------

            Shipment existing =
                    shipmentService.getShipmentById(id);

            if (existing == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Shipment not found"
                );

                return;
            }

            // ---------------------------------------------
            // Already delivered
            // ---------------------------------------------

            if (existing.getStatus() ==
                    ShipmentStatus.DELIVERED) {

                sendError(
                        response,
                        HttpServletResponse.SC_CONFLICT,
                        "Shipment is already delivered."
                );

                return;
            }

            // ---------------------------------------------
            // Read request body
            // ---------------------------------------------

            JsonNode root =
                    objectMapper.readTree(
                            request.getReader()
                    );

            if (root == null ||
                    !root.isObject()) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Request body is required"
                );

                return;
            }

            JsonNode dateNode =
                    root.get(
                            "actualDeliveryDate"
                    );

            if (dateNode == null ||
                    dateNode.isNull() ||
                    dateNode.asText().isBlank()) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Actual delivery date is required"
                );

                return;
            }

            // ---------------------------------------------
            // Parse LocalDate
            // ---------------------------------------------

            LocalDate actualDeliveryDate;

            try {

                actualDeliveryDate =
                        LocalDate.parse(
                                dateNode.asText()
                        );

            } catch (Exception e) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Actual delivery date must use YYYY-MM-DD format"
                );

                return;
            }

            // ---------------------------------------------
            // Mark delivered
            // ---------------------------------------------

            shipmentService.markShipmentDelivered(
                    id,
                    actualDeliveryDate
            );

            // ---------------------------------------------
            // Return updated shipment
            // ---------------------------------------------

            Shipment updated =
                    shipmentService.getShipmentById(id);

            objectMapper.writeValue(
                    response.getWriter(),
                    updated
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid shipment ID"
            );

        } catch (IllegalStateException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_CONFLICT,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
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


    // =====================================================
    // PARSE ID
    // =====================================================

    private int parseShipmentId(
            String pathInfo) {

        String[] parts =
                pathInfo.split("/");

        if (parts.length != 2 ||
                parts[1].isBlank()) {

            throw new NumberFormatException(
                    "Invalid shipment ID"
            );
        }

        return Integer.parseInt(
                parts[1]
        );
    }


    // =====================================================
    // JSON HEADERS
    // =====================================================

    private void setJsonHeaders(
            HttpServletResponse response) {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );
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
                        "message",
                        message == null
                                ? "An error occurred"
                                : message
                )
        );
    }


    // =====================================================
    // PACKAGE REQUEST
    // =====================================================

    public static class PackageUpdateRequest {

        private List<Integer> packageIds;

        public List<Integer> getPackageIds() {
            return packageIds;
        }

        public void setPackageIds(
                List<Integer> packageIds) {

            this.packageIds =
                    packageIds;
        }
    }
}