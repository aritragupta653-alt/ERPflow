
package com.erpflow.controller;

import com.erpflow.model.InventoryAlertSettings;
import com.erpflow.service.InventoryAlertSettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/inventory-alert-settings")
public class InventoryAlertSettingsServlet extends HttpServlet {

    private final InventoryAlertSettingsService settingsService =
            new InventoryAlertSettingsService();

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(
                            new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()
                    );

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            InventoryAlertSettings settings =
                    settingsService.getSettings();

            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), settings);

        } catch (Exception e) {
            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    java.util.Map.of("error", e.getMessage())
            );
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            InventoryAlertSettings settings =
                    objectMapper.readValue(
                            request.getInputStream(),
                            InventoryAlertSettings.class
                    );

            InventoryAlertSettings updatedSettings =
                    settingsService.updateSettings(settings);

            response.setStatus(HttpServletResponse.SC_OK);

            objectMapper.writeValue(
                    response.getWriter(),
                    updatedSettings
            );

        } catch (Exception e) {
            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    java.util.Map.of("error", e.getMessage())
            );
        }
    }
}