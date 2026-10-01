package com.erpflow.controller;

import com.erpflow.model.InventoryAlertSettings;
import com.erpflow.service.InventoryAlertSettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

@WebServlet("/api/inventory-alerts/config")
public class InventoryAlertSettingsServlet extends HttpServlet {

    private final InventoryAlertSettingsService service =
            new InventoryAlertSettingsService();

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());


    // =====================================================
    // GET CONFIGURATION
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            InventoryAlertSettings settings =
                    service.getSettings();

            objectMapper.writeValue(
                    response.getWriter(),
                    settings
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
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
    // UPDATE CONFIGURATION
    // =====================================================

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            InventoryAlertSettings settings =
                    objectMapper.readValue(
                            request.getReader(),
                            InventoryAlertSettings.class
                    );


            InventoryAlertSettings updated =
                    service.updateSettings(
                            settings
                    );


            objectMapper.writeValue(
                    response.getWriter(),
                    updated
            );

        } catch (IllegalArgumentException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(
                            e.getMessage()
                    )
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
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

        public ErrorResponse(
                String message) {

            this.message =
                    message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(
                String message) {

            this.message =
                    message;
        }
    }
}