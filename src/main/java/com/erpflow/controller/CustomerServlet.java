package com.erpflow.controller;

import com.erpflow.model.Customer;
import com.erpflow.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/customers/*")
public class CustomerServlet extends HttpServlet {

    private final CustomerService customerService = new CustomerService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        setJsonResponse(response);

        String pathInfo = request.getPathInfo();

        // GET /api/customers
        if (pathInfo == null || pathInfo.equals("/")) {
            List<Customer> customers = customerService.getAllCustomers();
            objectMapper.writeValue(response.getWriter(), customers);
            return;
        }

        // GET /api/customers/{id}
        try {
            int id = Integer.parseInt(pathInfo.substring(1));

            Customer customer = customerService.getCustomerById(id);

            if (customer == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Customer not found"
                );
                return;
            }

            objectMapper.writeValue(response.getWriter(), customer);

        } catch (NumberFormatException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid customer ID"
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        setJsonResponse(response);

        try {
            Customer customer = objectMapper.readValue(
                    request.getReader(),
                    Customer.class
            );

            customerService.createCustomer(customer);

            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getWriter(), customer);

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
            HttpServletResponse response
    ) throws ServletException, IOException {

        setJsonResponse(response);

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Customer ID is required"
            );
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));

            Customer customer = objectMapper.readValue(
                    request.getReader(),
                    Customer.class
            );

            customer.setId(id);
            customerService.updateCustomer(customer);

            objectMapper.writeValue(response.getWriter(), customer);

        } catch (NumberFormatException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid customer ID"
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
            HttpServletResponse response
    ) throws ServletException, IOException {

        setJsonResponse(response);

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Customer ID is required"
            );
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));

            customerService.deleteCustomer(id);

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (NumberFormatException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid customer ID"
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
            String message
    ) throws IOException {

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