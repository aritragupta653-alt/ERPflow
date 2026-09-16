package com.erpflow.controller;

import com.erpflow.model.Customer;
import com.erpflow.model.Item;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.service.CustomerService;
import com.erpflow.service.ItemService;
import com.erpflow.service.SalesOrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/sales-orders")
public class SalesOrderServlet extends HttpServlet {

    private final SalesOrderService salesOrderService =
            new SalesOrderService();

    private final CustomerService customerService =
            new CustomerService();

    private final ItemService itemService =
            new ItemService();


    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");


        // View Sales Order

        if ("view".equals(action)) {

            int id = Integer.parseInt(
                    request.getParameter("id")
            );

            SalesOrder salesOrder =
                    salesOrderService.getSalesOrderById(id);

            List<SalesOrderItem> salesOrderItems =
                    salesOrderService.getSalesOrderItems(
                            salesOrder
                    );

            request.setAttribute(
                    "salesOrder",
                    salesOrder
            );

            request.setAttribute(
                    "salesOrderItems",
                    salesOrderItems
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/salesOrderDetails.jsp"
            ).forward(request, response);

            return;
        }


        // Sales Order List

        List<SalesOrder> salesOrders =
                salesOrderService.getAllSalesOrders();

        List<Customer> customers =
                customerService.getAllCustomers();

        List<Item> items =
                itemService.getAllItems();


        request.setAttribute(
                "salesOrders",
                salesOrders
        );

        request.setAttribute(
                "customers",
                customers
        );

        request.setAttribute(
                "items",
                items
        );


        request.getRequestDispatcher(
                "/WEB-INF/views/salesOrders.jsp"
        ).forward(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");


        // Complete Sales Order

        /*if ("complete".equals(action)) {

            int salesOrderId = Integer.parseInt(
                    request.getParameter("salesOrderId")
            );

            salesOrderService.completeSalesOrder(
                    salesOrderId
            );

            response.sendRedirect(
                    request.getContextPath()
                            + "/sales-orders"
                            + "?action=view&id="
                            + salesOrderId
            );

            return;
        }*/


        // Create Sales Order

        int customerId = Integer.parseInt(
                request.getParameter("customerId")
        );

        Customer customer =
                customerService.getCustomerById(
                        customerId
                );


        SalesOrder salesOrder = new SalesOrder();

        salesOrder.setCustomer(customer);

        salesOrder.setStatus("CREATED");

        salesOrder.setOrderDate(
                LocalDateTime.now()
        );


        String[] itemIds =
                request.getParameterValues("itemId");

        String[] quantities =
                request.getParameterValues("quantity");

        String[] sellingPrices =
                request.getParameterValues("sellingPrice");


        List<SalesOrderItem> salesOrderItems =
                new ArrayList<>();


        for (int i = 0; i < itemIds.length; i++) {

            int itemId =
                    Integer.parseInt(itemIds[i]);

            Item item =
                    itemService.getItemById(itemId);


            SalesOrderItem salesOrderItem =
                    new SalesOrderItem();

            salesOrderItem.setItem(item);

            salesOrderItem.setQuantity(
                    Integer.parseInt(quantities[i])
            );

            salesOrderItem.setSellingPrice(
                    new BigDecimal(sellingPrices[i])
            );


            salesOrderItems.add(
                    salesOrderItem
            );
        }


        salesOrderService.createSalesOrder(
                salesOrder,
                salesOrderItems
        );


        response.sendRedirect(
                request.getContextPath()
                        + "/sales-orders"
        );
    }
}