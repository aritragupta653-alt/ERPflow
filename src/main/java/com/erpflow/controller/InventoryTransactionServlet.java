package com.erpflow.controller;

import com.erpflow.model.InventoryTransaction;
import com.erpflow.service.InventoryTransactionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;


@WebServlet("/inventory-transactions")
public class InventoryTransactionServlet extends HttpServlet {

    private final InventoryTransactionService
            inventoryTransactionService =
            new InventoryTransactionService();


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {


        List<InventoryTransaction> transactions =
                inventoryTransactionService.getAllTransactions();


        request.setAttribute(
                "transactions",
                transactions
        );


        request.getRequestDispatcher(
                "/WEB-INF/views/inventoryTransactions.jsp"
        ).forward(request, response);
    }
}