
package com.erpflow.controller;

import com.erpflow.model.Package;
import com.erpflow.model.Shipment;
import com.erpflow.service.AutoPackShipService;
import com.erpflow.service.AutoPackShipService.AutoPackShipResult;


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
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/auto-pack-ship/*")
public class AutoPackShipServlet extends HttpServlet {

    private final AutoPackShipService autoPackShipService =
            new AutoPackShipService();

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());


    // =====================================================
    // POST /api/auto-pack-ship/{salesOrderId}
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        setJsonResponse(response);

        try {
            String path = request.getPathInfo();

            if (path == null || path.equals("/")) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Sales Order ID is required"
                );
                return;
            }

            String[] pathParts = path.split("/");

            if (pathParts.length != 2 || pathParts[1].isBlank()) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid endpoint. Expected /api/auto-pack-ship/{salesOrderId}"
                );
                return;
            }

            int salesOrderId;

            try {
                salesOrderId = Integer.parseInt(pathParts[1]);
            } catch (NumberFormatException e) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid Sales Order ID"
                );
                return;
            }

            if (salesOrderId <= 0) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Sales Order ID must be greater than zero"
                );
                return;
            }


            // -------------------------------------------------
            // Read request body
            // -------------------------------------------------

            JsonNode body = objectMapper.readTree(request.getReader());

            if (body == null || !body.isObject()) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Request body must contain shipmentDate and deliveryStatus"
                );
                return;
            }

            JsonNode shipmentDateNode = body.get("shipmentDate");
            JsonNode deliveryStatusNode = body.get("deliveryStatus");

            if (shipmentDateNode == null ||
                    shipmentDateNode.isNull() ||
                    shipmentDateNode.asText().isBlank()) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Shipment date is required"
                );
                return;
            }

            if (deliveryStatusNode == null ||
                    deliveryStatusNode.isNull() ||
                    deliveryStatusNode.asText().isBlank()) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Delivery status is required"
                );
                return;
            }


            // -------------------------------------------------
            // Parse shipment date
            // -------------------------------------------------

            LocalDate shipmentDate;

            try {
                shipmentDate = LocalDate.parse(
                        shipmentDateNode.asText()
                );
            } catch (Exception e) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Shipment date must use YYYY-MM-DD format"
                );
                return;
            }

            String deliveryStatus =
                    deliveryStatusNode.asText().trim();


       

            AutoPackShipResult result =
                    autoPackShipService.packAndShip(
                            salesOrderId,
                            shipmentDate,
                            deliveryStatus
                    );

            Package pkg = result.getPackageEntity();
            Shipment shipment = result.getShipment();


            // -------------------------------------------------
            // Build response
            // -------------------------------------------------

            Map<String, Object> packageData =
                    new LinkedHashMap<>();

            packageData.put("id", pkg.getId());
            packageData.put("packageNumber", pkg.getPackageNumber());
            packageData.put("status", pkg.getStatus());


            Map<String, Object> shipmentData =
                    new LinkedHashMap<>();

            shipmentData.put("id", shipment.getId());
            shipmentData.put("shipmentNumber", shipment.getShipmentNumber());
            shipmentData.put("shipmentDate", shipment.getShipmentDate());
            shipmentData.put("status", shipment.getStatus());
            shipmentData.put("shippingMethod", shipment.getShippingMethod());

            shipmentData.put(
                    "carrier",
                    shipment.getCarrier() != null
                            ? shipment.getCarrier().getName()
                            : null
            );

            shipmentData.put(
                    "carrierService",
                    shipment.getCarrierService() != null
                            ? shipment.getCarrierService().getName()
                            : null
            );

            shipmentData.put(
                    "shippingCharge",
                    shipment.getShippingCharge()
            );

            shipmentData.put(
                    "estimatedDeliveryDate",
                    shipment.getEstimatedDeliveryDate()
            );

            shipmentData.put(
                    "trackingNumber",
                    shipment.getTrackingNumber()
            );

            shipmentData.put(
                    "dispatchAddress",
                    shipment.getDispatchAddress()
            );

            shipmentData.put(
                    "destinationAddress",
                    shipment.getDestinationAddress()
            );


            Map<String, Object> responseData =
                    new LinkedHashMap<>();

            responseData.put(
                    "message",
                    "Sales Order packed and shipped successfully"
            );

            responseData.put("salesOrderId", salesOrderId);
            responseData.put("package", packageData);
            responseData.put("shipment", shipmentData);

            response.setStatus(HttpServletResponse.SC_OK);

            objectMapper.writeValue(
                    response.getWriter(),
                    responseData
            );

        } catch (RuntimeException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Unable to pack and ship this Sales Order"
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while packing and shipping"
            );
        }
    }


    // =====================================================
    // REJECT UNSUPPORTED METHODS
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        setJsonResponse(response);

        sendError(
                response,
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Use POST to pack and ship a Sales Order"
        );
    }


    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        setJsonResponse(response);

        sendError(
                response,
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "PUT is not supported for this endpoint"
        );
    }


    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        setJsonResponse(response);

        sendError(
                response,
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "DELETE is not supported for this endpoint"
        );
    }


    // =====================================================
    // JSON HELPERS
    // =====================================================

    private void setJsonResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }


    private void sendError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        response.setStatus(status);

        Map<String, Object> error =
                new LinkedHashMap<>();

        error.put("error", message);

        objectMapper.writeValue(
                response.getWriter(),
                error
        );
    }
}