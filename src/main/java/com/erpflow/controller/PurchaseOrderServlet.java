package com.erpflow.controller;

import com.erpflow.model.Item;
import com.erpflow.model.PurchaseOrder;
import com.erpflow.model.PurchaseOrderItem;
import com.erpflow.model.Supplier;

import com.erpflow.service.ItemService;
import com.erpflow.service.PurchaseOrderService;
import com.erpflow.service.SupplierService;

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


@WebServlet("/purchase-orders")
public class PurchaseOrderServlet extends HttpServlet {

    private final PurchaseOrderService purchaseOrderService =
            new PurchaseOrderService();

    private final SupplierService supplierService =
            new SupplierService();

    private final ItemService itemService =
            new ItemService();


    // DISPLAY PURCHASE ORDERS

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        String action = request.getParameter("action");


    if ("view".equals(action)) {

        int id = Integer.parseInt(
                request.getParameter("id")
        );

        PurchaseOrder purchaseOrder =
                purchaseOrderService
                        .getPurchaseOrderById(id);


        List<PurchaseOrderItem> purchaseOrderItems =
                purchaseOrderService
                        .getPurchaseOrderItems(
                                purchaseOrder
                        );


        request.setAttribute(
                "purchaseOrder",
                purchaseOrder
        );

        request.setAttribute(
                "purchaseOrderItems",
                purchaseOrderItems
        );


        request.getRequestDispatcher(
                "/WEB-INF/views/purchaseOrderDetails.jsp"
        ).forward(request, response);

        return;
    }


        List<PurchaseOrder> purchaseOrders =
                purchaseOrderService
                        .getAllPurchaseOrders();


        List<Supplier> suppliers =
                supplierService
                        .getAllSuppliers();


        List<Item> items =
                itemService
                        .getAllItems();


        request.setAttribute(
                "purchaseOrders",
                purchaseOrders
        );

        request.setAttribute(
                "suppliers",
                suppliers
        );

        request.setAttribute(
                "items",
                items
        );


        request.getRequestDispatcher(
                "/WEB-INF/views/purchaseOrders.jsp"
        ).forward(request, response);
    }


    // CREATE PURCHASE ORDER

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        String action =
            request.getParameter("action");


    // RECEIVE PURCHASE ORDER

    if ("receive".equals(action)) {

        int purchaseOrderId =
                Integer.parseInt(
                        request.getParameter(
                                "purchaseOrderId"
                        )
                );


        purchaseOrderService
                .receivePurchaseOrder(
                        purchaseOrderId
                );


        response.sendRedirect(
                request.getContextPath()
                        + "/purchase-orders"
                        + "?action=view&id="
                        + purchaseOrderId
        );

        return;
    }


        // GET SUPPLIER

        int supplierId =
                Integer.parseInt(
                        request.getParameter("supplierId")
                );


        Supplier supplier =
                supplierService.getSupplierById(
                        supplierId
                );


        // CREATE PURCHASE ORDER

        PurchaseOrder purchaseOrder =
                new PurchaseOrder();

        purchaseOrder.setSupplier(
                supplier
        );

        purchaseOrder.setStatus(
                "CREATED"
        );

        purchaseOrder.setOrderDate(
                LocalDateTime.now()
        );


        // GET MULTIPLE ITEMS

        String[] itemIds =
                request.getParameterValues(
                        "itemId"
                );

        String[] quantities =
                request.getParameterValues(
                        "quantity"
                );

        String[] purchasePrices =
                request.getParameterValues(
                        "purchasePrice"
                );


        List<PurchaseOrderItem>
                purchaseOrderItems =
                new ArrayList<>();


        // CREATE PURCHASE ORDER ITEMS

        for (int i = 0; i < itemIds.length; i++) {


            int itemId =
                    Integer.parseInt(
                            itemIds[i]
                    );


            Item item =
                    itemService.getItemById(
                            itemId
                    );


            PurchaseOrderItem purchaseOrderItem =
                    new PurchaseOrderItem();


            purchaseOrderItem.setItem(
                    item
            );


            purchaseOrderItem.setQuantity(
                    Integer.parseInt(
                            quantities[i]
                    )
            );


            purchaseOrderItem.setPurchasePrice(
                    new BigDecimal(
                            purchasePrices[i]
                    )
            );


            purchaseOrderItems.add(
                    purchaseOrderItem
            );
        }


        // SAVE PURCHASE ORDER

        purchaseOrderService.createPurchaseOrder(
                purchaseOrder,
                purchaseOrderItems
        );


        response.sendRedirect(
                request.getContextPath()
                        + "/purchase-orders"
        );
    }
}