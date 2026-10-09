package com.erpflow.controller;

import com.erpflow.dao.ReportsDAO;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.erpflow.model.OrderFulfillmentReport;;

@WebServlet(urlPatterns = {
                "/api/reports/inventory",
                "/api/reports/sales-by-item",
                "/api/reports/sales-by-customer",
                "/api/reports/sales-order-summary",
                "/api/reports/order-fulfillment",
                "/api/reports/order-fulfillment/summary",
                "/api/reports/order-fulfillment/by-item"

})
public class ReportsServlet extends HttpServlet {

        private final ReportsDAO reportsDAO = new ReportsDAO();

        private final ObjectMapper mapper = new ObjectMapper();

        // =========================================================
        // GET REPORT
        // =========================================================

        @Override
        protected void doGet(
                        HttpServletRequest request,
                        HttpServletResponse response)
                        throws IOException {

                response.setContentType(
                                "application/json;charset=UTF-8");

                response.setHeader(
                                "Cache-Control",
                                "no-store");

                String path = request.getServletPath();

                String search = parameter(
                                request,
                                "search");

                String from = parameter(
                                request,
                                "from");

                String to = parameter(
                                request,
                                "to");
                String sortBy = parameter(request, "sortBy");

                String sortDirection = parameter(request, "sortDirection");

                try {

                        List<Map<String, Object>> rows=null;
                        List<OrderFulfillmentReport> fulfillment = null;

                        // =================================================
                        // SALES BY ITEM
                        // =================================================
                        
                        if (path.endsWith(
                                        "/sales-by-item")) {

                                rows = reportsDAO.getSalesByItemReport(
                                                search,
                                                from,
                                                to,
                                        sortBy,
                                sortDirection);
                        }

                        // =================================================
                        // SALES BY CUSTOMER
                        // =================================================

                        else if (path.endsWith(
                                        "/sales-by-customer")) {

                                 rows = reportsDAO.getSalesByCustomerReport(
                                                search,
                                                from,
                                                to,
                                        sortBy,
                                sortDirection);
                        }

                        // =================================================
                        // SALES ORDER SUMMARY
                        // =================================================

                        else if (path.endsWith(
                                        "/sales-order-summary")) {

                                rows = reportsDAO.getSalesOrderSummaryReport(
                                                search,
                                                from,
                                                to,
                                        sortBy,
                                sortDirection);
                        }

                        else if (path.endsWith(
                                        "/order-fulfillment")) {

                                fulfillment = reportsDAO.getFulfillmentReport(
                                                search,
                                                from,
                                                to,
                                        sortBy,
                                sortDirection);
                        }
                        else if (path.endsWith(
                                        "/order-fulfillment/summary")) {

                                fulfillment = reportsDAO.getFulfillmentSummaryReport(
                                                search,
                                                from,
                                                to,
                                        sortBy,
                                sortDirection);
                        }
                        else if (path.endsWith(
                                        "/order-fulfillment/by-item")) {

                                fulfillment = reportsDAO.getItemFulfillmentReport(
                                                search,
                                                from,
                                                to,
                                        sortBy,
                                sortDirection);
                        }
                        

                        // =================================================
                        // INVENTORY STOCK SUMMARY
                        // =================================================

                        else {

                                String stock = parameter(
                                                request,
                                                "stock");

                                if (stock.isBlank()) {
                                        stock = "all";
                                }

                                rows = reportsDAO.getInventoryReport(
                                                search,
                                                stock,
                                                from,
                                                to,
                                                sortBy,
                                                sortDirection);
                        }

                        if(path.endsWith(
                                        "/sales-by-item") || path.endsWith(
                                        "/sales-by-customer") || path.endsWith(
                                        "/sales-order-summary") || path.endsWith(
                                        "/inventory")){mapper.writeValue(
                                        response.getWriter(),
                                        rows);
                                        }
                        else {
                                mapper.writeValue(response.getWriter(),fulfillment);
                        }

                } catch (IllegalArgumentException e) {

                        response.setStatus(
                                        HttpServletResponse.SC_BAD_REQUEST);

                        writeError(
                                        response,
                                        e.getMessage());

                } catch (Exception e) {

                        e.printStackTrace();

                        response.setStatus(
                                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                        writeError(
                                        response,
                                        "Unable to load report: "
                                                        + e.getMessage());
                }
        }

        // =========================================================
        // GET REQUEST PARAMETER
        // =========================================================

        private String parameter(
                        HttpServletRequest request,
                        String name) {

                String value = request.getParameter(name);

                return value == null
                                ? ""
                                : value.trim();
        }

        // =========================================================
        // ERROR RESPONSE
        // =========================================================

        private void writeError(
                        HttpServletResponse response,
                        String message)
                        throws IOException {

                Map<String, String> error = new LinkedHashMap<>();

                error.put(
                                "message",
                                message);

                mapper.writeValue(
                                response.getWriter(),
                                error);
        }
}