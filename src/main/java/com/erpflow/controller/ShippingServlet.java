package com.erpflow.controller;

import com.erpflow.model.Shipment;
import com.erpflow.service.ShippingRateService;
import com.erpflow.service.ShippingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/shipping/*")
public class ShippingServlet extends HttpServlet {

    private final ShippingService shippingService =
            new ShippingService();

    private final ShippingRateService shippingRateService =
            new ShippingRateService();
private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(
                            new JavaTimeModule()
                    );


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            String path = request.getPathInfo();

            JsonNode root =
                    objectMapper.readTree(request.getReader());

            // =====================================================
            // CALCULATE SHIPPING RATE
            // POST /api/shipping/rate
            // =====================================================

            if ("/rate".equals(path)) {

                JsonNode packageIdsNode =
                        root.get("packageIds");

                if (packageIdsNode == null ||
                        !packageIdsNode.isArray() ||
                        packageIdsNode.isEmpty()) {

                    throw new RuntimeException(
                            "packageIds must be a non-empty array");
                }

                List<Integer> packageIds =
                        new ArrayList<>();

                for (JsonNode node : packageIdsNode) {
                    packageIds.add(node.asInt());
                }

                JsonNode carrierServiceNode =
                        root.get("carrierServiceId");

                if (carrierServiceNode == null ||
                        carrierServiceNode.asInt() <= 0) {

                    throw new RuntimeException(
                            "Valid carrierServiceId is required");
                }

                int carrierServiceId =
                        carrierServiceNode.asInt();

                ShippingRateService.ShippingRate rate =
                        shippingRateService.calculateRate(
                                packageIds,
                                carrierServiceId
                        );

                objectMapper.writeValue(
                        response.getWriter(),
                        rate
                );

                return;
            }

            // =====================================================
            // CREATE SHIPMENT
            // POST /api/shipping
            // =====================================================

            if (!"/".equals(path) && path != null) {
                throw new RuntimeException(
                        "Invalid shipping endpoint");
            }

            // -----------------------------
            // Sales Order
            // -----------------------------

            JsonNode salesOrderNode =
                    root.get("salesOrderId");

            if (salesOrderNode == null ||
                    salesOrderNode.asInt() <= 0) {

                throw new RuntimeException(
                        "Valid salesOrderId is required");
            }

            int salesOrderId =
                    salesOrderNode.asInt();

            // -----------------------------
            // Packages
            // -----------------------------

            JsonNode packageIdsNode =
                    root.get("packageIds");

            if (packageIdsNode == null ||
                    !packageIdsNode.isArray() ||
                    packageIdsNode.isEmpty()) {

                throw new RuntimeException(
                        "packageIds must be a non-empty array");
            }

            List<Integer> packageIds =
                    new ArrayList<>();

            for (JsonNode node : packageIdsNode) {
                int packageId = node.asInt();

                if (packageId <= 0) {
                    throw new RuntimeException(
                            "Invalid packageId: " + packageId);
                }

                packageIds.add(packageId);
            }

            // -----------------------------
            // Carrier Service
            // -----------------------------

            JsonNode carrierServiceNode =
                    root.get("carrierServiceId");

            if (carrierServiceNode == null ||
                    carrierServiceNode.asInt() <= 0) {

                throw new RuntimeException(
                        "Valid carrierServiceId is required");
            }

            int carrierServiceId =
                    carrierServiceNode.asInt();

            // -----------------------------
            // Create Shipment object
            // -----------------------------

            Shipment shipment =
                    new Shipment();

            if (root.has("shippingMethod")) {

                shipment.setShippingMethod(
                        root.get("shippingMethod").asText()
                );
            }

            if (root.has("trackingNumber")) {

                shipment.setTrackingNumber(
                        root.get("trackingNumber").asText()
                );
            }

            if (root.has("trackingUrl")) {

                shipment.setTrackingUrl(
                        root.get("trackingUrl").asText()
                );
            }

            if (root.has("shippingCharge")) {

                shipment.setShippingCharge(
                        root.get("shippingCharge").asDouble()
                );
            }

            if (root.has("dispatchAddress")) {

                shipment.setDispatchAddress(
                        root.get("dispatchAddress").asText()
                );
            }

            if (root.has("destinationAddress")) {

                shipment.setDestinationAddress(
                        root.get("destinationAddress").asText()
                );
            }

            if (root.has("notes")) {

                shipment.setNotes(
                        root.get("notes").asText()
                );
            }

            // =====================================================
            // CREATE SHIPMENT THROUGH SERVICE
            // =====================================================

            Shipment createdShipment =
                    shippingService.shipPackages(
                            salesOrderId,
                            packageIds,
                            carrierServiceId,
                            shipment
                    );

            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    createdShipment
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(
                            e.getMessage()
                    )
            );
        }
    }

    // =====================================================
    // ERROR RESPONSE
    // =====================================================

    public static class ErrorResponse {

        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}