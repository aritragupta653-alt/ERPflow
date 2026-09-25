package com.erpflow.controller;

import com.erpflow.model.OrderFulfillmentReport;
import com.erpflow.service.OrderFulfillmentReportService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/reports/order-fulfillment/*")
public class OrderFulfillmentReportServlet
        extends HttpServlet {

    private final OrderFulfillmentReportService reportService = new OrderFulfillmentReportService();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            String path = request.getPathInfo();

            String fromDate = request.getParameter("fromDate");

            String toDate = request.getParameter("toDate");

            String status = request.getParameter("status");

            // NEW: Order Fulfillment Summary
            if ("/summary".equals(path)) {

                List<OrderFulfillmentReport> report = reportService.getSummaryReport(
                        fromDate,
                        toDate,
                        status);

                objectMapper.writeValue(
                        response.getWriter(),
                        report);

                return;
            }
            if ("/by-item".equals(path)) {

                List<OrderFulfillmentReport> report = reportService.getItemReport(
                        fromDate,
                        toDate,
                        status);

                objectMapper.writeValue(
                        response.getWriter(),
                        report);

                return;
            }

            // EXISTING: Order Fulfillment Report
            List<OrderFulfillmentReport> report = reportService.getReport(
                    fromDate,
                    toDate,
                    status);

            objectMapper.writeValue(
                    response.getWriter(),
                    report);

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Map<String, String> error = new HashMap<>();

            error.put(
                    "error",
                    e.getMessage());

            objectMapper.writeValue(
                    response.getWriter(),
                    error);
        }
    }
}