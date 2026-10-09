package com.erpflow.controller;

import com.erpflow.model.InventoryTransaction;
import com.erpflow.service.InventoryTransactionService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
@WebServlet("/api/inventory-transactions")
public class InventoryTransactionServlet extends HttpServlet {

    private final InventoryTransactionService
            inventoryTransactionService =
            new InventoryTransactionService();

    private final ObjectMapper objectMapper =
        new ObjectMapper()
                .registerModule(
                        new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()
                );


    // =========================================================
    // GET ALL INVENTORY TRANSACTIONS
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        try {

            List<InventoryTransaction> transactions =
                    inventoryTransactionService
                            .getAllTransactions();

            response.setContentType(
                    "application/json"
            );

            response.setCharacterEncoding(
                    "UTF-8"
            );

            response.setStatus(
                    HttpServletResponse.SC_OK
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    transactions
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.setContentType(
                    "application/json"
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    java.util.Map.of(
                            "error",
                            "Error fetching inventory transactions"
                    )
            );
        }
    }
}