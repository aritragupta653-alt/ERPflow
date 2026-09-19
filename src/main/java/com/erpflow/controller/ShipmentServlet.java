package com.erpflow.controller;

import com.erpflow.model.Shipment;
import com.erpflow.service.ShipmentService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {

            // GET /api/shipments
            if (pathInfo == null || pathInfo.equals("/")) {

                List<Shipment> shipments =
                        shipmentService.getAllShipments();

                objectMapper.writeValue(
                        response.getWriter(),
                        shipments
                );

                return;
            }

            // GET /api/shipments/{id}
            String idPart = pathInfo.substring(1);

            int id = Integer.parseInt(idPart);

            Shipment shipment =
                    shipmentService.getShipmentById(id);

            if (shipment == null) {

                response.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                objectMapper.writeValue(
                        response.getWriter(),
                        new ErrorResponse("Shipment not found")
                );

                return;
            }

            objectMapper.writeValue(
                    response.getWriter(),
                    shipment
            );

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse("Invalid shipment ID")
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(e.getMessage())
            );
        }
    }

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