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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/shipments/*")
public class ShipmentServlet extends HttpServlet {

    private final ShipmentService shipmentService = new ShipmentService();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ShipmentServlet() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
                

        setJsonResponse(response);

        String pathInfo = request.getPathInfo();

        String servletPath = request.getServletPath();

if ("/shipments".equals(servletPath)) {

    String action = request.getParameter("action");

    if ("view".equals(action)) {

        int id = Integer.parseInt(
                request.getParameter("id")
        );

        Shipment shipment =
                shipmentService.getShipmentById(id);

        if (shipment == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Shipment not found"
            );
            return;
        }

        request.setAttribute("shipment", shipment);

        request.getRequestDispatcher(
                "/WEB-INF/views/shipmentDetails.jsp"
        ).forward(request, response);

        return;
    }
}

        try {

            // GET /api/shipments
            if (pathInfo == null || pathInfo.equals("/")) {

                List<Shipment> shipments =
                        shipmentService.getAllShipments();

                List<Map<String, Object>> result =
                        new ArrayList<>();

                for (Shipment shipment : shipments) {

                    Map<String, Object> data =
                            new HashMap<>();

                    data.put("id", shipment.getId());

                    data.put(
                            "shipmentDate",
                            shipment.getShipmentDate()
                                    .toString()
                    );

                    data.put(
                            "status",
                            shipment.getStatus()
                    );

                    if (shipment.getPackageEntity() != null) {

                        data.put(
                                "packageId",
                                shipment.getPackageEntity().getId()
                        );

                        if (shipment.getPackageEntity()
                                .getSalesOrder() != null) {

                            data.put(
                                    "salesOrderId",
                                    shipment.getPackageEntity()
                                            .getSalesOrder()
                                            .getId()
                            );
                        }
                    }

                    result.add(data);
                }

                objectMapper.writeValue(
                        response.getWriter(),
                        result
                );

                return;
            }

            // GET /api/shipments/{id}

            int id = Integer.parseInt(
                    pathInfo.substring(1)
            );

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

            Map<String, Object> data =
                    new HashMap<>();

            data.put("id", shipment.getId());

            data.put(
                    "shipmentDate",
                    shipment.getShipmentDate().toString()
            );

            data.put(
                    "status",
                    shipment.getStatus()
            );

            if (shipment.getPackageEntity() != null) {

                data.put(
                        "packageId",
                        shipment.getPackageEntity().getId()
                );

                if (shipment.getPackageEntity()
                        .getSalesOrder() != null) {

                    data.put(
                            "salesOrderId",
                            shipment.getPackageEntity()
                                    .getSalesOrder()
                                    .getId()
                    );
                }
            }

            objectMapper.writeValue(
                    response.getWriter(),
                    data
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid shipment ID"
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

    private void setJsonResponse(
            HttpServletResponse response) {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        objectMapper.writeValue(
                response.getWriter(),
                new ErrorResponse(message)
        );
    }

    private static class ErrorResponse {

        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}