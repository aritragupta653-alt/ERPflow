package com.erpflow.controller;

import com.erpflow.model.Item;
import com.erpflow.service.ItemService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/items/*")
public class ItemServlet extends HttpServlet {

    private final ItemService itemService = new ItemService();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);

        String pathInfo = request.getPathInfo();

        // GET /api/items
        if (pathInfo == null || pathInfo.equals("/")) {

            String status = request.getParameter("status");

    try {
        List<Item> items = itemService.getItemsByStatus(status);

        objectMapper.writeValue(response.getWriter(), items);

    } catch (RuntimeException e) {
        sendError(
                response,
                HttpServletResponse.SC_BAD_REQUEST,
                e.getMessage()
        );
    }

    return;
        }

        // GET /api/items/{id}
        try {

            int id = Integer.parseInt(pathInfo.substring(1));

            Item item = itemService.getItemById(id);

            if (item == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Item not found"
                );

                return;
            }

            objectMapper.writeValue(response.getWriter(), item);

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid item ID"
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);

        try {

            Item item = objectMapper.readValue(
                    request.getReader(),
                    Item.class
            );

            itemService.createItem(item);

            response.setStatus(HttpServletResponse.SC_CREATED);

            objectMapper.writeValue(response.getWriter(), item);

        } catch (RuntimeException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Item ID is required"
            );

            return;
        }

        try {

            int id = Integer.parseInt(pathInfo.substring(1));

            Item item = objectMapper.readValue(
                    request.getReader(),
                    Item.class
            );

            item.setId(id);

            itemService.updateItem(item);

            objectMapper.writeValue(response.getWriter(), item);

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid item ID"
            );

        } catch (RuntimeException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        setJsonResponse(response);

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Item ID is required"
            );

            return;
        }

        try {

            int id = Integer.parseInt(pathInfo.substring(1));

            itemService.deleteItem(id);

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid item ID"
            );

        } catch (RuntimeException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    e.getMessage()
            );
        }
    }

    private void setJsonResponse(HttpServletResponse response) {

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