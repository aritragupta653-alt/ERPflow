package com.erpflow.controller;

import com.erpflow.model.Shipment;
import com.erpflow.service.ShipmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.erpflow.model.enums.ShipmentStatus;

@WebServlet("/api/shipments/*")
public class ShipmentServlet extends HttpServlet {

    private final ShipmentService shipmentService =
            new ShipmentService();

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        setJsonHeaders(response);

        String pathInfo = request.getPathInfo();

        String status = request.getParameter("status");

        try {
            // GET /api/shipments
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Shipment> shipments =
                        shipmentService.getAllShipments(status);

                objectMapper.writeValue(
                        response.getWriter(),
                        shipments
                );
                return;
            }

            // GET /api/shipments/{id}
            int id = parseShipmentId(pathInfo);

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

    /*
     * PUT /api/shipments/{id}
     * Updates shipment details.
     *
     * PUT /api/shipments/{id}/packages
     * Updates package assignments for a shipment.
     *
     * Delivered shipments cannot be edited or have
     * their package assignments changed.
     */
    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        setJsonHeaders(response);

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Shipment ID is required"
                );
                return;
            }

            String[] parts = pathInfo.split("/");

            if (parts.length < 2 || parts[1].isBlank()) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid shipment endpoint"
                );
                return;
            }

            int id = Integer.parseInt(parts[1]);

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

            // Prevent all PUT modifications after delivery.
            if ("DELIVERED".equalsIgnoreCase(existing.getStatus().name())) {
                sendError(
                        response,
                        HttpServletResponse.SC_CONFLICT,
                        "Delivered shipments cannot be edited or have their packages changed."
                );
                return;
            }

            // PUT /api/shipments/{id}/packages
            if (parts.length == 3 && parts[2].equals("packages")) {

                PackageUpdateRequest packageRequest =
                        objectMapper.readValue(
                                request.getInputStream(),
                                PackageUpdateRequest.class
                        );

                if (packageRequest.getPackageIds() == null) {
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

            // PUT /api/shipments/{id}
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

            shipmentService.updateShipment(updatedShipment);

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
            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );
        }
    }

    /*
     * POST /api/shipments/{id}/deliver
     * Marks a shipment as delivered.
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        setJsonHeaders(response);

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid shipment endpoint"
                );
                return;
            }

            String[] parts = pathInfo.split("/");

            if (parts.length != 3 || !parts[2].equals("deliver")) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid shipment endpoint"
                );
                return;
            }

            int id = Integer.parseInt(parts[1]);

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

            if ("DELIVERED".equalsIgnoreCase(existing.getStatus().name())) {
                sendError(
                        response,
                        HttpServletResponse.SC_CONFLICT,
                        "Shipment is already delivered."
                );
                return;
            }

            shipmentService.markShipmentDelivered(id);

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
            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );
        }
    }

    private int parseShipmentId(String pathInfo) {
        String[] parts = pathInfo.split("/");

        if (parts.length != 2 || parts[1].isBlank()) {
            throw new NumberFormatException("Invalid shipment ID");
        }

        return Integer.parseInt(parts[1]);
    }

    private void setJsonHeaders(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        response.setStatus(status);

        objectMapper.writeValue(
                response.getWriter(),
                Map.of(
                        "message",
                        message == null ? "An error occurred" : message
                )
        );
    }

    public static class PackageUpdateRequest {

        private List<Integer> packageIds;

        public List<Integer> getPackageIds() {
            return packageIds;
        }

        public void setPackageIds(List<Integer> packageIds) {
            this.packageIds = packageIds;
        }
    }
}