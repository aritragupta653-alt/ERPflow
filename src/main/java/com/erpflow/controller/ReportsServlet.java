
package com.erpflow.controller;

import com.erpflow.dao.ReportsDAO;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet(urlPatterns = {
        "/api/reports/inventory",
        "/api/reports/sales-by-item",
        "/api/reports/sales-by-customer"
})
public class ReportsServlet extends HttpServlet {

    private final ReportsDAO reportsDAO = new ReportsDAO();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        String path = request.getServletPath();

        String search = parameter(request, "search");
        String fromDate = parameter(request, "from");
        String toDate = parameter(request, "to");

        try {

            List<Map<String, Object>> rows;

            if (path.endsWith("/sales-by-item")) {

                rows = reportsDAO.getSalesByItemReport(
                        search,
                        fromDate,
                        toDate
                );

            } else if (path.endsWith("/sales-by-customer")) {

                rows = reportsDAO.getSalesByCustomerReport(
                        search,
                        fromDate,
                        toDate
                );

            } else {

                String stockFilter = parameter(request, "stock");

                if (stockFilter.isBlank()) {
                    stockFilter = "all";
                }

                rows = reportsDAO.getInventoryReport(
                        search,
                        stockFilter,
                        fromDate,
                        toDate
                );
            }

            mapper.writeValue(response.getWriter(), rows);

        } catch (Exception e) {
    e.printStackTrace();

    Throwable cause = e;
    while (cause.getCause() != null) {
        cause = cause.getCause();
    }

    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    Map<String, String> error = new LinkedHashMap<>();
    error.put("error", cause.getClass().getSimpleName());
    error.put("message", cause.getMessage());

    response.getWriter().write(
        new ObjectMapper().writeValueAsString(error)
    );
}
    }

    private String parameter(
            HttpServletRequest request,
            String name) {

        String value = request.getParameter(name);

        return value == null ? "" : value.trim();
    }
}