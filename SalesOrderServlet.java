package com.erpflow.controller;

import com.erpflow.model.Customer;
import com.erpflow.model.Item;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.service.CustomerService;
import com.erpflow.service.ItemService;
import com.erpflow.service.SalesOrderService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/sales-orders")
public class SalesOrderServlet extends HttpServlet {

    private final SalesOrderService salesOrderService =
            new SalesOrderService();

    private final CustomerService customerService =
            new CustomerService();

    private final ItemService itemService =
            new ItemService();

    private final ObjectMapper objectMapper =
            new ObjectMapper();


    // GET
    // /api/sales-orders
    // /api/sales-orders?id=1

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        String idParameter =
                request.getParameter("id");

        try {

            if (idParameter != null) {

                int id =
                        Integer.parseInt(idParameter);

                SalesOrder salesOrder =
                        salesOrderService
                                .getSalesOrderById(id);

                if (salesOrder == null) {

                    response.setStatus(
                            HttpServletResponse.SC_NOT_FOUND
                    );

                    objectMapper.writeValue(
                            response.getWriter(),
                            java.util.Map.of(
                                    "message",
                                    "Sales order not found"
                            )
                    );

                    return;
                }

                List<SalesOrderItem> items =
                        salesOrderService
                                .getSalesOrderItems(
                                        salesOrder
                                );

                java.util.Map<String, Object> result =
                        new java.util.HashMap<>();

                result.put(
                        "salesOrder",
                        salesOrder
                );

                result.put(
                        "items",
                        items
                );

                objectMapper.writeValue(
                        response.getWriter(),
                        result
                );

                return;
            }


            List<SalesOrder> salesOrders =
                    salesOrderService
                            .getAllSalesOrders();

            objectMapper.writeValue(
                    response.getWriter(),
                    salesOrders
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    java.util.Map.of(
                            "message",
                            e.getMessage()
                    )
            );
        }
    }


    // POST
    // /api/sales-orders

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        try {

            JsonNode body =
                    objectMapper.readTree(
                            request.getReader()
                    );

            int customerId =
                    body.get("customerId")
                            .asInt();

            Customer customer =
                    customerService.getCustomerById(
                            customerId
                    );

            if (customer == null) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                objectMapper.writeValue(
                        response.getWriter(),
                        java.util.Map.of(
                                "message",
                                "Customer not found"
                        )
                );

                return;
            }


            SalesOrder salesOrder =
                    new SalesOrder();

            salesOrder.setCustomer(
                    customer
            );

            salesOrder.setStatus(
                    "CREATED"
            );

            salesOrder.setOrderDate(
                    LocalDateTime.now()
            );


            JsonNode itemsNode =
                    body.get("items");

            if (itemsNode == null ||
                    !itemsNode.isArray() ||
                    itemsNode.isEmpty()) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                objectMapper.writeValue(
                        response.getWriter(),
                        java.util.Map.of(
                                "message",
                                "Sales Order must contain at least one item"
                        )
                );

                return;
            }


            List<SalesOrderItem> salesOrderItems =
                    new ArrayList<>();


            for (JsonNode itemNode : itemsNode) {

                int itemId =
                        itemNode
                                .get("itemId")
                                .asInt();

                int quantity =
                        itemNode
                                .get("quantity")
                                .asInt();

                BigDecimal sellingPrice =
                        new BigDecimal(
                                itemNode
                                        .get("sellingPrice")
                                        .asText()
                        );


                Item item =
                        itemService.getItemById(
                                itemId
                        );

                if (item == null) {

                    throw new RuntimeException(
                            "Item not found: " + itemId
                    );
                }


                SalesOrderItem salesOrderItem =
                        new SalesOrderItem();

                salesOrderItem.setItem(
                        item
                );

                salesOrderItem.setQuantity(
                        quantity
                );

                salesOrderItem.setSellingPrice(
                        sellingPrice
                );

                salesOrderItems.add(
                        salesOrderItem
                );
            }


            salesOrderService.createSalesOrder(
                    salesOrder,
                    salesOrderItems
            );


            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    java.util.Map.of(
                            "message",
                            "Sales order created successfully",
                            "salesOrderId",
                            salesOrder.getId()
                    )
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    java.util.Map.of(
                            "message",
                            e.getMessage()
                    )
            );
        }
    }
}
