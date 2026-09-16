package com.erpflow.controller;

import com.erpflow.model.Shipment;
import com.erpflow.service.ShipmentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/shipments")
public class ShipmentServlet extends HttpServlet {

    private final ShipmentService shipmentService =
            new ShipmentService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String action =
                request.getParameter("action");

        if ("view".equals(action)) {

            int shipmentId =
                    Integer.parseInt(
                            request.getParameter("id")
                    );

            Shipment shipment =
                    shipmentService.getShipmentById(
                            shipmentId
                    );

            if (shipment == null) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Shipment not found"
                );

                return;
            }

            request.setAttribute(
                    "shipment",
                    shipment
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/shipmentDetails.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }

        List<Shipment> shipments =
                shipmentService.getAllShipments();

        request.setAttribute(
                "shipments",
                shipments
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/shipments.jsp"
        ).forward(
                request,
                response
        );
    }
}