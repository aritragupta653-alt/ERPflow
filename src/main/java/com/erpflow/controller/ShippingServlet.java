package com.erpflow.controller;

import com.erpflow.service.ShippingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/shipping/*")
public class ShippingServlet extends HttpServlet {

    private final ShippingService shippingService = new ShippingService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            String path = request.getPathInfo();

            if (path == null || path.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(
                        response.getWriter(),
                        Map.of("error", "Package ID is required")
                );
                return;
            }

            int packageId =
                    Integer.parseInt(path.substring(1));

            shippingService.shipPackage(packageId);

            response.setStatus(HttpServletResponse.SC_OK);

            objectMapper.writeValue(
                    response.getWriter(),
                    Map.of(
                            "message",
                            "Package shipped successfully",
                            "packageId",
                            packageId
                    )
            );

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    Map.of("error", "Invalid package ID")
            );

        } catch (RuntimeException e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    Map.of("error", e.getMessage())
            );
        }
    }
}